package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.controller.dto.PointRequest;
import io.hhplus.tdd.point.service.PointServiceImpl;
import io.hhplus.tdd.point.service.domain.PointHistoryResponse;
import io.hhplus.tdd.point.service.domain.PointResponse;
import io.hhplus.tdd.point.service.domain.PointServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class PointControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PointServiceImpl pointService;

    // 유저의 포인트를 조회하는 기능
    @Test
    @DisplayName("특정 유저의 포인트를 조회한다")
    void point() throws Exception {
        // given
        long id = 1L;
        long amount = 100L;

        UserPoint userPoint = new UserPoint(id, amount, 0);

        // when
        when(pointService.selectPoint(anyLong())).thenReturn(PointResponse.of(userPoint));

        // then
        mockMvc.perform(get("/point/{id}", id))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(amount));
    }

    // 유저의 포인트 히스토리를 조회하는 기능
    @Test
    @DisplayName("특정 유저의 포인트 히스토리를 조회한다")
    void history() throws Exception {
        // given
        long id = 1L;

        PointHistoryResponse history1 = new PointHistoryResponse(1L, 1L, 500L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistoryResponse history2 = new PointHistoryResponse(2L, 1L, 1000L, TransactionType.CHARGE, System.currentTimeMillis());
        PointHistoryResponse history3 = new PointHistoryResponse(3L, 1L, 300L, TransactionType.USE, System.currentTimeMillis());

        List<PointHistoryResponse> historyList = List.of(history1, history2, history3);

        // when
        when(pointService.selectPointHistories(id)).thenReturn(historyList);

        // then
        mockMvc.perform(get("/point/{id}/histories", id))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].point").value(500L))
                .andExpect(jsonPath("$[0].type").value("CHARGE"))
                .andExpect(jsonPath("$[2].userId").value(1L))
                .andExpect(jsonPath("$[2].point").value(300L))
                .andExpect(jsonPath("$[2].type").value("USE"));
    }

    // 유저의 포인트 충전 시 충전된 값을 반환하는지 확인하기 위한 테스트
    @Test
    @DisplayName("특정 유저의 포인트를 충전한다")
    void charge() throws Exception {
        // given
        long id = 1L;
        long initialPoint = 1000L;
        long chargePoint = 500L;

        PointRequest request = PointRequest.builder()
                .id(id)
                .point(chargePoint)
                .build();

        UserPoint initialUserPoint = new UserPoint(id, initialPoint, System.currentTimeMillis());
        UserPoint updatedUserPoint = new UserPoint(id, initialPoint + chargePoint, System.currentTimeMillis());

        when(pointService.chargePoint(any(PointServiceRequest.class))).thenReturn(PointResponse.of(updatedUserPoint));

        // when then
        mockMvc.perform(patch("/point/{id}/charge", id)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(initialUserPoint.point() + chargePoint));
    }

    // 유저의 포인트 사용 후 포인터가 잘 감소 되는지 확인하기 위함
    @Test
    @DisplayName("특정 유저의 포인트를 사용한다")
    void use() throws Exception {
        // given
        long id = 1L;
        long initialPoint = 1000L;
        long usePoint = 400L;

        PointRequest request = PointRequest.builder()
                .id(id)
                .point(usePoint)
                .build();

        UserPoint useUserPoint = new UserPoint(id, initialPoint - usePoint, System.currentTimeMillis());
        when(pointService.usePoint(any(PointServiceRequest.class))).thenReturn(PointResponse.of(useUserPoint));

        // when then
        mockMvc.perform(patch("/point/{id}/use", id)
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.point").value(initialPoint - usePoint));
    }
}