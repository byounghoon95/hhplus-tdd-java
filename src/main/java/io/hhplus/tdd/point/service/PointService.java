package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;

public interface PointService {
    PointResponse selectPoint(long id);
    PointResponse chargePoint(PointServiceRequest request);
    PointResponse usePoint(PointServiceRequest request);
}
