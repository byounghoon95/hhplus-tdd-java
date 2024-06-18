package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
}