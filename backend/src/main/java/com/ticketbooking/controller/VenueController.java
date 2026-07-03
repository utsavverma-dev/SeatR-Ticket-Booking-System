package com.ticketbooking.controller;

import com.ticketbooking.dto.request.VenueRequest;
import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.response.VenueResponse;
import com.ticketbooking.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Tag(name = "Venues", description = "Venue management APIs (Admin only for write operations)")
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    @Operation(summary = "Create a new venue (Admin only)")
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody VenueRequest request) {
        VenueResponse venue = venueService.createVenue(request);
        return new ResponseEntity<>(ApiResponse.success("Venue created successfully", venue), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing venue (Admin only)")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {
        VenueResponse venue = venueService.updateVenue(id, request);
        return ResponseEntity.ok(ApiResponse.success("Venue updated successfully", venue));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a venue (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.ok(ApiResponse.success("Venue deleted successfully", null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue by ID")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable Long id) {
        VenueResponse venue = venueService.getVenue(id);
        return ResponseEntity.ok(ApiResponse.success(venue));
    }

    @GetMapping
    @Operation(summary = "Get all venues")
    public ResponseEntity<ApiResponse<List<VenueResponse>>> getAllVenues() {
        List<VenueResponse> venues = venueService.getAllVenues();
        return ResponseEntity.ok(ApiResponse.success(venues));
    }
}
