package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.domain.PointResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    private UserPointTable pointTable;

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
}