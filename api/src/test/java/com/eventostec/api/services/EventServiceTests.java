package com.eventostec.api.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.eventostec.api.domain.address.Address;
import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.domain.event.PaginatedResponse;
import com.eventostec.api.repositories.EventRepository;

public class EventServiceTests {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
}
