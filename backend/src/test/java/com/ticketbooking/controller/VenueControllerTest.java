package com.ticketbooking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketbooking.dto.request.SeatRequest;
import com.ticketbooking.dto.request.VenueRequest;
import com.ticketbooking.dto.response.SeatInfo;
import com.ticketbooking.dto.response.VenueResponse;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.exception.GlobalExceptionHandler;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@WebMvcTest(
    value = VenueController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {com.ticketbooking.security.JwtAuthenticationFilter.class, com.ticketbooking.config.SecurityConfig.class}
    )
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VenueService venueService;

    private VenueResponse createTestVenueResponse() {
        return new VenueResponse(
                1L, "Test Theater", "123 Main St", "Mumbai", "MH", 15,
                List.of(new SeatInfo(SeatCategory.PREMIUM, 5), new SeatInfo(SeatCategory.STANDARD, 10)),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should create venue successfully")
    void createVenue_withValidRequest_shouldReturn201() throws Exception {
        VenueRequest request = new VenueRequest("Test Theater", "123 Main St", "Mumbai", "MH",
                List.of(new SeatRequest(SeatCategory.PREMIUM, "A", 5)));

        when(venueService.createVenue(any(VenueRequest.class))).thenReturn(createTestVenueResponse());

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Theater"));
    }

    @Test
    @DisplayName("Should return 400 for invalid venue request")
    void createVenue_withInvalidRequest_shouldReturn400() throws Exception {
        String invalidRequest = "{\"name\": \"\"}";

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get all venues")
    void getAllVenues_shouldReturn200() throws Exception {
        when(venueService.getAllVenues()).thenReturn(List.of(createTestVenueResponse()));

        mockMvc.perform(get("/api/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Test Theater"));
    }

    @Test
    @DisplayName("Should get venue by ID")
    void getVenue_withValidId_shouldReturn200() throws Exception {
        when(venueService.getVenue(1L)).thenReturn(createTestVenueResponse());

        mockMvc.perform(get("/api/venues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Test Theater"));
    }

    @Test
    @DisplayName("Should return 404 for non-existent venue")
    void getVenue_withInvalidId_shouldReturn404() throws Exception {
        when(venueService.getVenue(999L))
                .thenThrow(new ResourceNotFoundException("Venue", "id", 999L));

        mockMvc.perform(get("/api/venues/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Should update venue successfully")
    void updateVenue_withValidRequest_shouldReturn200() throws Exception {
        VenueRequest request = new VenueRequest("Updated Theater", "456 Oak St", "Delhi", "DL",
                List.of(new SeatRequest(SeatCategory.PREMIUM, "A", 10)));

        when(venueService.updateVenue(eq(1L), any(VenueRequest.class))).thenReturn(createTestVenueResponse());

        mockMvc.perform(put("/api/venues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Should delete venue successfully")
    void deleteVenue_withValidId_shouldReturn200() throws Exception {
        doNothing().when(venueService).deleteVenue(1L);

        mockMvc.perform(delete("/api/venues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
