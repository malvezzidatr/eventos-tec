package com.eventostec.api.controller;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventRequestDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.domain.event.PaginatedResponse;
import com.eventostec.api.services.EventService;



@RestController
@RequestMapping("/api")
public class EventController {
    @Autowired
    private EventService eventService;

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @PostMapping(value = "event", consumes = "multipart/form-data")
    public ResponseEntity<Event> create(@RequestParam("title") String title,
                                        @RequestParam("description") String description,
                                        @RequestParam("eventUrl") String eventUrl,
                                        @RequestParam(value = "image", required = false) MultipartFile image,
                                        @RequestParam("date") Long date,
                                        @RequestParam("remote") Boolean remote,
                                        @RequestParam("city") String city,
                                        @RequestParam("uf") String uf) throws IOException {
        EventRequestDTO eventRequestDTO = new EventRequestDTO(title, description, date, city, uf, remote, eventUrl, image);
        logger.info("Start - EventController - Create - title: {}", title);
        Event newEvent = this.eventService.createEvent(eventRequestDTO);
        logger.info("End - EventController - Create - title: {}", title);
        return ResponseEntity.ok(newEvent);
    }

    @GetMapping("event")
    public ResponseEntity<PaginatedResponse<EventResponseDTO>> getEvents(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<EventResponseDTO> allEvents = this.eventService.getUpcomingEvents(page, size);

        return ResponseEntity.ok(allEvents);
    }

    @GetMapping("event/filter")
    public ResponseEntity<PaginatedResponse<EventResponseDTO>> filterEvents(@RequestParam(defaultValue = "0") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(required = false) String title,
                                                               @RequestParam(required = false) String city,
                                                               @RequestParam(required = false) String uf,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        PaginatedResponse<EventResponseDTO> events = eventService.getFilteredEvents(page, size, title, city, uf, startDate, endDate);

        return ResponseEntity.ok(events);
    }
    
    @GetMapping("event/{eventId}")
    public ResponseEntity<EventDetailsDTO> getEventDetails(@PathVariable UUID eventId) {
        EventDetailsDTO eventDetails = this.eventService.getEventDetails(eventId);

        return ResponseEntity.ok(eventDetails);
    } 

    @GetMapping("event/history")
    public ResponseEntity<PaginatedResponse<EventResponseDTO>> getOlderEvents(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        PaginatedResponse<EventResponseDTO> olderEvents = this.eventService.getOlderEvents(page, size);

        return ResponseEntity.ok(olderEvents);
    }
    
}
