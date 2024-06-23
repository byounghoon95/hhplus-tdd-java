package io.hhplus.tdd.point.service.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointServiceRequest {
    private long id;
    private long point;

    @Builder
    private PointServiceRequest(long id, long point) {
        this.id = id;
        this.point = point;
    }
}
