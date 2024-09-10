package com.eventostec.api.services;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.coupon.CouponRequestDTO;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.repositories.CouponRepository;
import com.eventostec.api.repositories.EventRepository;

@ExtendWith(MockitoExtension.class)
public class CouponServiceTests {
    @InjectMocks
    private CouponService couponService;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private EventRepository eventRepository;

    @Test
    void shouldAddACouponToSomeEvent() {
        UUID eventId = UUID.randomUUID();
        Event event = new Event(eventId,
                                "Test",
                                "Test",
                                "www.test.com.br",
                                "www.test.com.br",
                                false,
                                new Date(),
                                new Address());

        when(eventRepository.findById(any())).thenReturn(Optional.of(event));

        Coupon coupon = new Coupon(UUID.randomUUID(),
                                   "TEST20",
                                   20,
                                   new Date(),
                                   event);
        CouponRequestDTO couponRequestDTO = new CouponRequestDTO("TEST20",
                                                                20,
                                                                1L);

        when(couponRepository.save(any())).thenReturn(coupon);

        Coupon responseCoupon = couponService.addCouponToEvent(eventId, couponRequestDTO);
        assertEquals(coupon.getCode(), responseCoupon.getCode());
        assertEquals(coupon.getDiscount(), responseCoupon.getDiscount());
        assertEquals(coupon.getEvent(), responseCoupon.getEvent());
    }

    @Test
    void shouldThrowExceptionWhenEventNotFound() {
        UUID eventId = UUID.randomUUID();
        CouponRequestDTO couponRequestDTO = new CouponRequestDTO("TEST20", 20, 1L);

        when(eventRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            couponService.addCouponToEvent(eventId, couponRequestDTO);
        }, "Event not found");
    }

    @Test
    void shouldReturnCouponsForEventAfterSpecificDate() {
        UUID eventId = UUID.randomUUID();
        Date queryDate = new Date();

        Event event = new Event(eventId, 
                                "Test Event", 
                                "Description", 
                                "www.testevent.com", 
                                "www.testevent.com", 
                                false, 
                                new Date(), 
                                new Address());

        Coupon coupon1 = new Coupon(UUID.randomUUID(), "TEST10", 10, new Date(queryDate.getTime() + 100000), event);
        Coupon coupon2 = new Coupon(UUID.randomUUID(), "TEST20", 20, new Date(queryDate.getTime() + 200000), event);
        
        List<Coupon> coupons = Arrays.asList(coupon1, coupon2);

        when(couponRepository.findByEventIdAndValidAfter(any(UUID.class), any(Date.class))).thenReturn(coupons);

        List<Coupon> resultCoupons = couponService.consultCoupons(eventId, queryDate);

        assertEquals(2, resultCoupons.size());
        assertEquals(coupons, resultCoupons);
    }
}
