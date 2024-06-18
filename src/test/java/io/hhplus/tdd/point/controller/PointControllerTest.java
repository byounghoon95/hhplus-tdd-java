package io.hhplus.tdd.point.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.service.PointServiceImpl;
import io.hhplus.tdd.point.service.domain.PointResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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


}