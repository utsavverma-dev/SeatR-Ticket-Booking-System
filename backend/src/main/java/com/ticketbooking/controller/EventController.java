package com.ticketbooking.controller;

import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management APIs (Organiser for write, public for read)")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create a new event (Organiser only)")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody EventRequest request,
            Principal principal) {
        EventResponse event = eventService.createEvent(request, principal.getName());
        return new ResponseEntity<>(ApiResponse.success("Event created successfully", event), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an event (Organiser, owner only)")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            Principal principal) {
        EventResponse event = eventService.updateEvent(id, request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Event updated successfully", event));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event (Organiser, owner only)")
    public ResponseEntity<ApiResponse<Void>> deleteEvent(
            @PathVariable Long id,
            Principal principal) {
        eventService.deleteEvent(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Event deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID (Public)")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long id) {
        EventResponse event = eventService.getEvent(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping
    @Operation(summary = "Get all events (Public)")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        List<EventResponse> events = eventService.getAllEvents();
        return ResponseEntity.ok(ApiResponse.success(events));
    }
}
