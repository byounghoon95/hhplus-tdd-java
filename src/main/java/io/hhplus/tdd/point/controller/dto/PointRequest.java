package io.hhplus.tdd.point.controller.dto;

import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PointRequest {
    long id;
    long point;

    @Builder
    private PointRequest(long id, long point) {
        this.id = id;
        this.point = point;
    }

    public void updateId(long id) {
        this.id = id;
    }

    public PointServiceRequest toServiceRequest() {
        return PointServiceRequest.builder()
                .id(id)
                .point(point)
                .build();
    }
}