package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.service.domain.PointHistoryResponse;
import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;

import java.util.List;

public interface PointService {
    PointResponse selectPoint(long id);
    List<PointHistoryResponse> selectPointHistories(long id);
    PointResponse chargePoint(PointServiceRequest request);
    PointResponse usePoint(PointServiceRequest request);
}
