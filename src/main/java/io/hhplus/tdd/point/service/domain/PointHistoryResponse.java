package io.hhplus.tdd.point.service.domain;

import io.hhplus.tdd.point.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponse {
    long id;
    long userId;
    long point;
    TransactionType type;
    long updateMillis;
}