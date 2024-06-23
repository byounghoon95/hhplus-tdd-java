package io.hhplus.tdd.point.service.domain;

import io.hhplus.tdd.point.UserPoint;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointResponse {
    long id;
    long point;

    @Builder
    private PointResponse(long id, long point) {
        this.id = id;
        this.point = point;
    }

    public static PointResponse of(UserPoint userPoint) {
        return PointResponse.builder()
                .id(userPoint.id())
                .point(userPoint.point())
                .build();
    }
}