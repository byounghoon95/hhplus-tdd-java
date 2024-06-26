package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.domain.PointHistoryResponse;
import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<PointHistoryResponse> selectPointHistories(long id) {
        return historyTable.selectAllByUserId(id).stream()
                .map(pointHistory -> new PointHistoryResponse(pointHistory.id(), pointHistory.userId(), pointHistory.amount(),pointHistory.type(),pointHistory.updateMillis()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized PointResponse chargePoint(PointServiceRequest request) {

        if (request.getPoint() <= 0) {
            throw new IllegalArgumentException("0 이하의 포인트를 충전할 수 없습니다");
        }

        UserPoint userPoint = pointTable.selectById(request.getId());
        UserPoint chargedUserPoint = UserPoint.addPoints(userPoint, request.getPoint());

        historyTable.insert(userPoint.id(), request.getPoint(), TransactionType.CHARGE, System.currentTimeMillis());

        return PointResponse.of(pointTable.insertOrUpdate(chargedUserPoint.id(),chargedUserPoint.point()));
    }

    @Override
    public synchronized PointResponse usePoint(PointServiceRequest request) {

        UserPoint userPoint = pointTable.selectById(request.getId());

        if (userPoint.point() - request.getPoint() < 0) {
            throw new IllegalArgumentException("포인트 잔고가 부족합니다.");
        }

        UserPoint usedUserPoint = UserPoint.usePoints(userPoint, request.getPoint());
        historyTable.insert(userPoint.id(), request.getPoint(), TransactionType.USE, System.currentTimeMillis());

        return PointResponse.of(pointTable.insertOrUpdate(usedUserPoint.id(),usedUserPoint.point()));
    }
}
