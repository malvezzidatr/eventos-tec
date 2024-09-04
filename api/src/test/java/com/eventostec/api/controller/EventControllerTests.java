package com.eventostec.api.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eventostec.api.domain.event.EventDetailsDTO;
import com.eventostec.api.domain.event.EventResponseDTO;
import com.eventostec.api.domain.event.PaginatedResponse;
import com.eventostec.api.services.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EventController.class)
public class EventControllerTests {
    
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getEventsShouldReturnUpcomingEventsWhenSuccessful() throws Exception {
        List<EventResponseDTO> eventResponseDTOList = new ArrayList<>();
        EventResponseDTO eventResponseDTO = new EventResponseDTO(UUID.randomUUID(),
                                                                "Event test",
                                                                "Event test",
                                                                new Date(),
                                                                "São Paulo",
                                                                "SP",
                                                                false,
                                                                "www.test.com.br",
                                                                "www.test.com.br");
        eventResponseDTOList.add(eventResponseDTO);

        when(eventService.getUpcomingEvents(0, 10)).thenReturn(new PaginatedResponse<>(eventResponseDTOList, 1));

        mockMvc.perform(get("/api/event")
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").isNotEmpty());
    };

    @Test
    public void filterEventsReturnAllEventsWithoutFilterWhenSuccessful() throws Exception {
        List<EventResponseDTO> eventResponseDTOList = new ArrayList<>();
        EventResponseDTO eventResponseDTO = new EventResponseDTO(UUID.randomUUID(),
                                                                "Event test",
                                                                "Event test",
                                                                new Date(),
                                                                "São Paulo",
                                                                "SP",
                                                                false,
                                                                "www.test.com.br",
                                                                "www.test.com.br");
        eventResponseDTOList.add(eventResponseDTO);

        when(eventService.getFilteredEvents(0, 10, null, null, null, null, null))
            .thenReturn(new PaginatedResponse<>(eventResponseDTOList, 1));

        mockMvc.perform(get("/api/event/filter")
            .param("page", "0")
            .param("size", "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title", is("Event test")))
            .andExpect(jsonPath("$.content[0].city", is("São Paulo")))
            .andExpect(jsonPath("$.content[0].uf", is("SP")));
    };

    @Test
    public void getEventDetailsReturnWhenSuccessfull() throws Exception {
        UUID eventId = UUID.randomUUID();
        EventDetailsDTO eventDetailsDTO = new EventDetailsDTO(eventId,
                                                            "Event test",
                                                            "Description Test",
                                                            new Date(),
                                                            "Sampa",
                                                            "SP",
                                                            "www.test.com.br",
                                                            "www.test.com.br",
                                                            null);

        when(eventService.getEventDetails(eventId)).thenReturn(eventDetailsDTO);

        mockMvc.perform(get("/api/event/{eventId}", eventId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("Event test")))
            .andExpect(jsonPath("$.city", is("Sampa")))
            .andExpect(jsonPath("$.uf", is("SP")));
    };
}
