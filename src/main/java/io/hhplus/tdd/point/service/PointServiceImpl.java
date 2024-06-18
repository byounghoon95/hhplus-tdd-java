package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.service.domain.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService{

    private final UserPointTable pointTable;

    @Override
    public PointResponse selectPoint(long id) {
        return PointResponse.of(pointTable.selectById(id));
    }
}
