package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.domain.PointHistoryResponse;
import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private UserPointTable pointTable;

    @Mock
    private PointHistoryTable historyTable;

    @InjectMocks
    private PointServiceImpl pointService;

    // 포인트 조회가 정상적으로 되는지 확인
    @Test
    @DisplayName("유저 아이디로 포인트를 조회한다")
    void selectPoint() {
        // given
        long id = 1L;
        long point = 500L;

        UserPoint userPoint = new UserPoint(id, point, System.currentTimeMillis());

        // when
        when(pointTable.selectById(id)).thenReturn(userPoint);
        PointResponse response = pointService.selectPoint(id);

        // then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getPoint()).isEqualTo(userPoint.point());
    }

    // 포인트 조회가 정상적으로 되는지 확인
    @Test
    @DisplayName("유저 아이디로 포인트를 조회한다")
    void selectPointHistories() {
        // given
        long id = 1L;

        PointHistory history1 = new PointHistory(1L, 1L, 500L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory history2 = new PointHistory(2L, 1L, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistory history3 = new PointHistory(3L, 1L, 300L, TransactionType.USE, System.currentTimeMillis());

        List<PointHistory> historyList = List.of(history1, history2, history3);

        // when
        when(historyTable.selectAllByUserId(id)).thenReturn(historyList);
        List<PointHistoryResponse> response = pointService.selectPointHistories(id);

        // then
        assertThat(response).hasSize(3)
                .extracting("userId", "point", "type")
                .containsExactlyInAnyOrder(
                        tuple(1L, 500L, TransactionType.CHARGE),
                        tuple(1L, 1000L, TransactionType.CHARGE),
                        tuple(1L, 300L, TransactionType.USE)
                );
        assertThat(response.get(0)).isInstanceOf(PointHistoryResponse.class);
    }

    // 포인트 충전 시 충전이 정상 처리 되는지 확인하기 위함
    @Test
    @DisplayName("포인트 충전 시 이전 포인트에서 충전 금액만큼 포인트가 추가된다")
    void chargePoint() {
        // given
        long id = 1L;
        long initialPoint = 500L;
        long chargePoint = 1000L;

        PointServiceRequest request = PointServiceRequest.builder()
                .id(id)
                .point(chargePoint)
                .build();

        UserPoint userPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());
        UserPoint chargedUserPoint = new UserPoint(id, initialPoint + chargePoint, System.currentTimeMillis());

        // when
        when(pointTable.selectById(id)).thenReturn(userPoint);
        when(pointTable.insertOrUpdate(anyLong(),anyLong())).thenReturn(chargedUserPoint);

        PointResponse response = pointService.chargePoint(request);

        // then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getPoint()).isEqualTo(initialPoint + chargePoint);
    }

    // 클라이언트에서 포인트를 잘못 입력한 경우 에러 발생 검증
    @Test
    @DisplayName("0이하의 포인트 충전 시 충전이 실패한다")
    void chargePoint_fail_under_0point() {
        // given
        long id = 1L;
        long chargePoint = 0L;

        PointServiceRequest request = PointServiceRequest.builder()
                .id(id)
                .point(chargePoint)
                .build();

        // when then
        assertThatThrownBy(() -> pointService.chargePoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("0 이하의 포인트를 충전할 수 없습니다");
    }

    // 포인트 충전 로직 동시성 처리를 검증
    @Test
    @DisplayName("여러 쓰레드에서 충전 요청이 들어올 때 충전 로직을 검증한다")
    void chargePoint_concurrent_10_request() throws InterruptedException {
        // given
        long id = 1L;
        long initialPoint = 500L;
        long chargePoint = 1000L;

        PointServiceRequest request = PointServiceRequest.builder()
                .id(id)
                .point(chargePoint)
                .build();

        // when
        AtomicReference<UserPoint> userPoint = new AtomicReference<>(new UserPoint(id, initialPoint, System.currentTimeMillis()));

        when(pointTable.selectById(id)).thenAnswer(invocation -> userPoint.get());
        when(pointTable.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long updatedId = invocation.getArgument(0);
            long updatedPoint = invocation.getArgument(1);
            UserPoint updatedUserPoint = new UserPoint(updatedId, updatedPoint, System.currentTimeMillis());
            userPoint.set(updatedUserPoint);
            return updatedUserPoint;
        });

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch (threadCount);

        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                pointService.chargePoint(request);
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        UserPoint finalUserPoint = pointTable.selectById(id);

        // then
        assertThat(finalUserPoint.id()).isEqualTo(id);
        assertThat(finalUserPoint.point()).isEqualTo(initialPoint + (chargePoint * threadCount));
    }

    // 포인트 사용 시 정상 차감 되는지 확인하기 위함
    @Test
    @DisplayName("포인트 사용 시 이전 포인트에서 사용 금액만큼 포인트가 차감된다")
    void usePoint() {
        // given
        long id = 1L;
        long initialPoint = 400L;
        long usePoint = 399L;

        PointServiceRequest request = PointServiceRequest.builder()
                .id(id)
                .point(usePoint)
                .build();

        UserPoint userPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());
        UserPoint usedUserPoint = new UserPoint(id, initialPoint - usePoint, System.currentTimeMillis());

        // when
        when(pointTable.selectById(id)).thenReturn(userPoint);
        when(pointTable.insertOrUpdate(anyLong(),anyLong())).thenReturn(usedUserPoint);

        PointResponse response = pointService.usePoint(request);

        // then
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getPoint()).isEqualTo(userPoint.point() - usePoint);
    }

    // 포인트 잔고가 없는 경우를 검증
    @Test
    @DisplayName("현재 포인트보다 사용하려는 포인트가 더 많으면 에러 발생")
    void usePoint_fail_not_enough_point() {
        // given
        long id = 1L;
        long initialPoint = 400L;
        long usePoint = 401L;

        PointServiceRequest request = PointServiceRequest.builder()
                .id(id)
                .point(usePoint)
                .build();

        UserPoint userPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());

        when(pointTable.selectById(id)).thenReturn(userPoint);

        // when then
        assertThatThrownBy(() -> pointService.usePoint(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("포인트 잔고가 부족합니다.");
    }

    // 포인트 사용 로직 동시성 처리를 검증
    @Test
    @DisplayName("여러 쓰레드에서 사용 요청이 들어올 때 사용 로직을 검증한다")
    void usePoint_concurrent_10_request() throws InterruptedException {
        // given
        long id = 1L;
        long initialPoint = 10000L;
        long usePoint = 500L;

        PointServiceRequest request = PointServiceRequest.builder()
                .id(id)
                .point(usePoint)
                .build();

        // when
        AtomicReference<UserPoint> userPoint = new AtomicReference<>(new UserPoint(id, initialPoint, System.currentTimeMillis()));

        when(pointTable.selectById(id)).thenAnswer(invocation -> userPoint.get());
        when(pointTable.insertOrUpdate(anyLong(), anyLong())).thenAnswer(invocation -> {
            long updatedId = invocation.getArgument(0);
            long updatedPoint = invocation.getArgument(1);
            UserPoint updatedUserPoint = new UserPoint(updatedId, updatedPoint, System.currentTimeMillis());
            userPoint.set(updatedUserPoint);
            return updatedUserPoint;
        });

        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch (threadCount);

        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                pointService.usePoint(request);
                latch.countDown();
            });
        }

        latch.await();
        executorService.shutdown();

        UserPoint finalUserPoint = pointTable.selectById(id);

        // then
        assertThat(finalUserPoint.id()).isEqualTo(id);
        assertThat(finalUserPoint.point()).isEqualTo(initialPoint - (usePoint * threadCount));
    }
}