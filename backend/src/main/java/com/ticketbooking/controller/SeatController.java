package com.ticketbooking.controller;

import com.ticketbooking.dto.SeatHoldRequest;
import com.ticketbooking.dto.SeatHoldResponse;
import com.ticketbooking.dto.ShowSeatResponse;
import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/events/{eventId}/seats")
    public ResponseEntity<ApiResponse<List<ShowSeatResponse>>> getSeatsForEvent(@PathVariable Long eventId) {
        List<ShowSeatResponse> seats = seatService.getSeatsByEventId(eventId);
        return ResponseEntity.ok(ApiResponse.success("Seats retrieved successfully", seats));
    }

    @PostMapping("/seats/hold")
    public ResponseEntity<ApiResponse<SeatHoldResponse>> holdSeats(
            @Valid @RequestBody SeatHoldRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        SeatHoldResponse response = seatService.holdSeats(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Seats held successfully", response));
    }

    @PostMapping("/seats/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @Valid @RequestBody SeatHoldRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        seatService.releaseSeats(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Seats released successfully", null));
    }
}
