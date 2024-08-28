package com.eventostec.api.controller;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.eventostec.api.domain.event.Event;
import com.eventostec.api.domain.event.EventRequestDTO;
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
}
