package com.eventostec.api.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.coupon.Coupon;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.domain.event.PaginatedResponse;
import com.eventostec.api.repositories.EventRepository;

@TestPropertySource("classpath:application-test.properties")
public class EventServiceTests {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CouponService couponService;

    @Mock
    private AddressService addressService;

    @Mock
    private AmazonS3 s3Client;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(eventService, "bucketName", "bucket");
    }

    @Test
    void createEventShouldCreateEventWithRealImage() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream("images/image-test.jpg");

        assertNotNull(imageStream);

        MultipartFile mockFile = new MockMultipartFile(
            "image", 
            "imagem-test.jpg", 
            "image/jpeg", 
            imageStream
        );

        EventRequestDTO requestDTO = new EventRequestDTO(
            "Event test",
            "Description test",
            System.currentTimeMillis(),
            "São Paulo",
            "SP",
            false,
            "www.test.com.br",
            mockFile
        );

        String s3Url = "https://s3.amazonaws.com/bucket/image-test.jpg";
        when(s3Client.putObject(anyString(), anyString(), any(File.class))).thenReturn(null);
        when(s3Client.getUrl(anyString(), anyString())).thenReturn(new URL(s3Url));

        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Event createdEvent = eventService.createEvent(requestDTO);

        assertNotNull(createdEvent);
        assertEquals(requestDTO.title(), createdEvent.getTitle());
        assertEquals(requestDTO.description(), createdEvent.getDescription());
        assertEquals(s3Url, createdEvent.getImgUrl());

        verify(s3Client).putObject(anyString(), anyString(), any(File.class));
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createEventShouldCreateEventWithoutImage() throws IOException {
        EventRequestDTO requestDTO = new EventRequestDTO(
            "Event test",
            "Description test",
            System.currentTimeMillis(),
            "São Paulo",
            "SP",
            false,
            "www.test.com.br",
            null
        );

        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Event createdEvent = eventService.createEvent(requestDTO);

        assertNotNull(createdEvent);
        assertEquals(requestDTO.title(), createdEvent.getTitle());
        assertEquals(requestDTO.description(), createdEvent.getDescription());
        assertNull(createdEvent.getImgUrl());
    }

    @Test
    void createEventShouldCreateEventInSomewhere() throws IOException {
        EventRequestDTO requestDTO = new EventRequestDTO(
            "Event test",
            "Description test",
            System.currentTimeMillis(),
            "São Paulo",
            "SP",
            true,
            "www.test.com.br",
            null
        );

        when(eventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Event createdEvent = eventService.createEvent(requestDTO);

        assertNotNull(createdEvent);
        assertEquals(requestDTO.title(), createdEvent.getTitle());
        assertEquals(requestDTO.description(), createdEvent.getDescription());
        assertNull(createdEvent.getImgUrl());
    }

    @Test
    public void testGetUpcomingEvents() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .description("Event Description")
                .data(new Date())
                .address(Address.builder().city("City").uf("UF").build())
                .remote(true)
                .eventUrl("http://example.com/")
                .imgUrl("http://example.com/image.jpg")
                .build();

        List<Event> eventList = Arrays.asList(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(eventList, pageable, eventList.size());

        when(eventRepository.findUpComingEvents(any(Date.class), eq(pageable))).thenReturn(eventPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getUpcomingEvents(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        EventResponseDTO dto = response.getContent().get(0);
        assertEquals(eventId, dto.id());
        assertEquals("Test Event", dto.title());
        assertEquals("Event Description", dto.description());
        assertNotNull(dto.date());
        assertEquals("City", dto.city());
        assertEquals("UF", dto.uf());
        assertTrue(dto.remote());
        assertEquals("http://example.com/", dto.eventUrl());
        assertEquals("http://example.com/image.jpg", dto.imgUrl());
        assertEquals(1, response.getTotalPage());
    }

    @Test
    public void testGetUpcomingEventsWithEventsButNoAddress() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(eventRepository.findUpComingEvents(any(Date.class), eq(pageable))).thenReturn(emptyPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getUpcomingEvents(0, 10);

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalPage());
    }

    @Test
    public void testGetUpcomingEventsEvents() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .description("Event Description")
                .data(new Date())
                .address(null)
                .remote(true)
                .eventUrl("http://example.com/")
                .imgUrl("http://example.com/image.jpg")
                .build();

        List<Event> eventList = Arrays.asList(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(eventList, pageable, eventList.size());

        when(eventRepository.findUpComingEvents(any(Date.class), eq(pageable))).thenReturn(eventPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getUpcomingEvents(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        EventResponseDTO dto = response.getContent().get(0);
        assertEquals(eventId, dto.id());
        assertEquals("Test Event", dto.title());
        assertEquals("Event Description", dto.description());
        assertNotNull(dto.date());
        assertEquals("", dto.city());
        assertEquals("", dto.uf());
        assertTrue(dto.remote());
        assertEquals("http://example.com/", dto.eventUrl());
        assertEquals("http://example.com/image.jpg", dto.imgUrl());
        assertEquals(1, response.getTotalPage());
    }

    @Test
    public void testGetOlderEvents() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .description("Event Description")
                .data(new Date())
                .address(Address.builder().city("City").uf("UF").build())
                .remote(false)
                .eventUrl("http://example.com/")
                .imgUrl("http://example.com/image.jpg")
                .build();

        List<Event> eventList = Arrays.asList(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(eventList, pageable, eventList.size());

        when(eventRepository.findOlderEvents(any(Date.class), eq(pageable))).thenReturn(eventPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getOlderEvents(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        EventResponseDTO dto = response.getContent().get(0);
        assertEquals(eventId, dto.id());
        assertEquals("Test Event", dto.title());
        assertEquals("Event Description", dto.description());
        assertNotNull(dto.date());
        assertEquals("City", dto.city());
        assertEquals("UF", dto.uf());
        assertFalse(dto.remote());
        assertEquals("http://example.com/", dto.eventUrl());
        assertEquals("http://example.com/image.jpg", dto.imgUrl());
        assertEquals(1, response.getTotalPage());
    }

    @Test
    public void testGetOlderEventsWithoutAddress() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .title("Test Event")
                .description("Event Description")
                .data(new Date())
                .address(null)
                .remote(true)
                .eventUrl("http://example.com/")
                .imgUrl("http://example.com/image.jpg")
                .build();

        List<Event> eventList = Arrays.asList(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(eventList, pageable, eventList.size());

        when(eventRepository.findOlderEvents(any(Date.class), eq(pageable))).thenReturn(eventPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getOlderEvents(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        EventResponseDTO dto = response.getContent().get(0);
        assertEquals(eventId, dto.id());
        assertEquals("Test Event", dto.title());
        assertEquals("Event Description", dto.description());
        assertNotNull(dto.date());
        assertEquals("", dto.city());
        assertEquals("", dto.uf());
        assertTrue(dto.remote());
        assertEquals("http://example.com/", dto.eventUrl());
        assertEquals("http://example.com/image.jpg", dto.imgUrl());
        assertEquals(1, response.getTotalPage());
    }

    @Test
    public void getFilteredEventsShouldReturnEventsWhenSuccessful() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                            .id(eventId)
                            .title("Event test")
                            .description("Event test")
                            .data(new Date())
                            .address(Address.builder().city("São Paulo").uf("SP").build())
                            .remote(false)
                            .eventUrl("www.test.com.br")
                            .imgUrl("www.test.com.br")
                            .build();

        List<Event> eventList = List.of(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(eventList, pageable, eventList.size());

        when(eventRepository.findFilteredEvents(anyString(), anyString(), anyString(), any(), any(), eq(pageable)))
            .thenReturn(eventPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getFilteredEvents(0, 10, "Event test", "São Paulo", "SP", new Date(0), new Date());
        
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        EventResponseDTO dto = response.getContent().get(0);
        assertEquals(eventId, dto.id());
        assertEquals("Event test", dto.title());
        assertEquals("São Paulo", dto.city());
        assertEquals("SP", dto.uf());
    }

    @Test
    public void getFilteredEventsShouldHandleNullFilters() throws Exception {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                            .id(eventId)
                            .title("Event test")
                            .description("Event test")
                            .data(new Date())
                            .address(null)
                            .remote(false)
                            .eventUrl("www.test.com.br")
                            .imgUrl("www.test.com.br")
                            .build();

        List<Event> eventList = List.of(event);
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> eventPage = new PageImpl<>(eventList, pageable, eventList.size());

        when(eventRepository.findFilteredEvents(anyString(), anyString(), anyString(), any(), any(), eq(pageable)))
            .thenReturn(eventPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getFilteredEvents(0, 10, null, null, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        EventResponseDTO dto = response.getContent().get(0);
        assertEquals(eventId, dto.id());
        assertEquals("Event test", dto.title());
        assertEquals("", dto.city());
        assertEquals("", dto.uf());
    }

    @Test
    public void getFilteredEventsShouldReturnEmptyWhenNoResults() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Event> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(eventRepository.findFilteredEvents("Nonexistent Title", null, null, new Date(0), new Date(), pageable))
            .thenReturn(emptyPage);

        PaginatedResponse<EventResponseDTO> response = eventService.getFilteredEvents(0, 10, "", "", "NC", new Date(0), new Date());

        assertNotNull(response);
        assertTrue(response.getContent().isEmpty());
        assertEquals(0, response.getTotalPage());
    }

    @Test void getEventsDetailsShouldReturnEventWhenSuccessful() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                            .id(eventId)
                            .title("Event test")
                            .description("Event test")
                            .data(new Date())
                            .address(Address.builder().city("São Paulo").uf("SP").build())
                            .remote(false)
                            .eventUrl("www.test.com.br")
                            .imgUrl("www.test.com.br")
                            .build();
        List<Coupon> coupons = Collections.singletonList(new Coupon(UUID.randomUUID(), "TEST20", 20, new Date(), event));
        System.out.println(coupons);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(couponService.consultCoupons(any(), any())).thenReturn(coupons);

        EventDetailsDTO response = eventService.getEventDetails(eventId);

        assertNotNull(response);
        assertEquals(eventId, response.id());
        assertEquals("Event test", response.title());
        assertEquals("São Paulo", response.city());
        assertEquals("SP", response.uf());

        assertNotNull(response.coupons());
        assertEquals(1, response.coupons().size());
        EventDetailsDTO.CouponDTO couponDTO = response.coupons().get(0);
        assertEquals("TEST20", couponDTO.code());
        assertEquals(20, couponDTO.discount());
        assertNotNull(couponDTO.validUntil());
    }

    @Test void getEventsDetailsShouldReturnNullAddressWhenSuccessful() {
        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                            .id(eventId)
                            .title("Event test")
                            .description("Event test")
                            .data(new Date())
                            .address(null)
                            .remote(false)
                            .eventUrl("www.test.com.br")
                            .imgUrl("www.test.com.br")
                            .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventDetailsDTO response = eventService.getEventDetails(eventId);

        assertNotNull(response);
        assertEquals(eventId, response.id());
        assertEquals("Event test", response.title());
        assertEquals("", response.city());
        assertEquals("", response.uf());
    }
}
