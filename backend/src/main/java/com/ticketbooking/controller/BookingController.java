package com.ticketbooking.controller;

import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.BookingDetailsResponse;
import com.ticketbooking.dto.BookingHistoryResponse;
import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        BookingResponse response = bookingService.createBooking(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking completed successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingHistoryResponse>>> getCustomerBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        List<BookingHistoryResponse> bookings = bookingService.getCustomerBookings(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Bookings retrieved successfully", bookings));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDetailsResponse>> getBookingDetails(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        BookingDetailsResponse details = bookingService.getBookingDetails(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking details retrieved successfully", details));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        bookingService.cancelBooking(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
}
