package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointServiceImpl implements PointService{

    private final UserPointTable pointTable;
    private final PointHistoryTable historyTable;

    @Override
    public PointResponse selectPoint(long id) {
        return PointResponse.of(pointTable.selectById(id));
    }

    @Override
    public PointResponse chargePoint(PointServiceRequest request) {

        if (request.getPoint() <= 0) {
            throw new IllegalArgumentException("0 이하의 포인트를 충전할 수 없습니다");
        }

        UserPoint userPoint = pointTable.selectById(request.getId());
        UserPoint chargedUserPoint = UserPoint.addPoints(userPoint, request.getPoint());

        historyTable.insert(userPoint.id(), request.getPoint(), TransactionType.CHARGE, System.currentTimeMillis());

        return PointResponse.of(pointTable.insertOrUpdate(chargedUserPoint.id(),chargedUserPoint.point()));
    }
}
