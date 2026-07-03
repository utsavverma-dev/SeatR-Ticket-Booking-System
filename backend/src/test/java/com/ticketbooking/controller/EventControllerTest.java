package com.ticketbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticketbooking.dto.request.EventPriceRequest;
import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.EventPriceResponse;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.entity.enums.EventStatus;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.exception.GlobalExceptionHandler;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
    value = EventController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.ticketbooking.security.JwtAuthenticationFilter.class, com.ticketbooking.config.SecurityConfig.class}
    )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventResponse testEventResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testEventResponse = new EventResponse(
                1L, "Test Concert", "A great concert",
                "Test Theater", 1L,
                "Jane Smith", 1L,
                LocalDate.now().plusDays(30), LocalTime.of(19, 0),
                EventStatus.UPCOMING,
                List.of(new EventPriceResponse(SeatCategory.PREMIUM, new BigDecimal("500.00"))),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should get all events")
    void getAllEvents_shouldReturn200() throws Exception {
        when(eventService.getAllEvents()).thenReturn(List.of(testEventResponse));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Test Concert"));
    }

    @Test
    @DisplayName("Should get event by ID")
    void getEvent_withValidId_shouldReturn200() throws Exception {
        when(eventService.getEvent(1L)).thenReturn(testEventResponse);

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Concert"))
                .andExpect(jsonPath("$.data.venueName").value("Test Theater"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent event")
    void getEvent_withInvalidId_shouldReturn404() throws Exception {
        when(eventService.getEvent(999L))
                .thenThrow(new ResourceNotFoundException("Event", "id", 999L));

        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should create event successfully")
    void createEvent_withValidRequest_shouldReturn201() throws Exception {
        EventRequest request = new EventRequest(
                "Test Concert", "A great concert", 1L,
                LocalDate.now().plusDays(30), LocalTime.of(19, 0),
                List.of(new EventPriceRequest(SeatCategory.PREMIUM, new BigDecimal("500.00")))
        );

        when(eventService.createEvent(any(EventRequest.class), anyString())).thenReturn(testEventResponse);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(() -> "jane@example.com"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Concert"));
    }

    @Test
    @DisplayName("Should delete event successfully")
    void deleteEvent_withValidId_shouldReturn200() throws Exception {
        mockMvc.perform(delete("/api/events/1")
                        .principal(() -> "jane@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
