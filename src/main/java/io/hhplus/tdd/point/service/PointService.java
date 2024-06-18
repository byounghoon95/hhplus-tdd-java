package io.hhplus.tdd.point.service;

import io.hhplus.tdd.point.service.domain.PointResponse;

public interface PointService {
    PointResponse selectPoint(long id);
}
