package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PointServiceThreadTest {

    @Autowired
    private UserPointTable pointTable;
    @Autowired
    private PointServiceImpl pointService;

    // 포인트 로직 동시성 처리를 검증
    @Test
    @DisplayName("여러 쓰레드에서 충전 및 사용 요청이 들어올 때 로직을 검증한다")
    void point_concurrent_request() throws InterruptedException {
        // given
        long id = 1L;
        long initialPoint = 1500L;
        long chargePoint = 1000L;
        long usePoint = 500L;

        PointServiceRequest initRequest = PointServiceRequest.builder()
                .id(id)
                .point(initialPoint)
                .build();

        PointServiceRequest chargeRequest = PointServiceRequest.builder()
                .id(id)
                .point(chargePoint)
                .build();

        PointServiceRequest useRequest = PointServiceRequest.builder()
                .id(id)
                .point(usePoint)
                .build();

        int threadCount = 1;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch (threadCount);
        executorService.execute(() -> {
            pointService.chargePoint(initRequest);
            latch.countDown();
        });

        latch.await();
        executorService.shutdown();

        // when
        int threadCount2 = 5;
        ExecutorService executorService2 = Executors.newFixedThreadPool(threadCount2);
        CountDownLatch latch2 = new CountDownLatch (threadCount2);

        executorService2.execute(() -> {
            pointService.chargePoint(chargeRequest);
            latch2.countDown();
        });

        executorService2.execute(() -> {
            pointService.usePoint(useRequest);
            latch2.countDown();
        });

        executorService2.execute(() -> {
            pointService.chargePoint(chargeRequest);
            latch2.countDown();
        });

        executorService2.execute(() -> {
            pointService.usePoint(useRequest);
            latch2.countDown();
        });

        executorService2.execute(() -> {
            pointService.chargePoint(chargeRequest);
            latch2.countDown();
        });

        latch2.await();
        executorService2.shutdown();

        UserPoint userPoint = pointTable.selectById(id);

        // then
        assertThat(userPoint.id()).isEqualTo(id);
        assertThat(userPoint.point()).isEqualTo(initialPoint + (chargePoint * 3) - (usePoint * 2));
    }

}
