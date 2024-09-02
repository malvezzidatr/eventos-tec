package com.eventostec.api.controller;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.coupon.CouponRequestDTO;
import com.eventostec.api.services.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CouponController.class)
public class CouponControllerTests {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CouponService couponService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void addCouponToEventShouldReturnCouponWhenSuccessful() throws Exception {
        CouponRequestDTO couponRequestDTO = new CouponRequestDTO("TEST20", 20, 1718203231L);
        UUID eventId = UUID.randomUUID();
        Coupon coupon = new Coupon();
        coupon.setCode("TEST20");
        coupon.setId(eventId);
        coupon.setDiscount(20);

        when(couponService.addCouponToEvent(eventId, couponRequestDTO)).thenReturn(coupon);

        mockMvc.perform(post("/api/coupon/event/{eventId}", eventId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(couponRequestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(coupon.getId().toString()))
            .andExpect(jsonPath("$.code").value(coupon.getCode()))
            .andExpect(jsonPath("$.discount").value(coupon.getDiscount()));
    }
}
