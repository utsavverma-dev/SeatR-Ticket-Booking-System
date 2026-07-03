package com.ticketbooking.controller;

import com.ticketbooking.dto.request.WaitlistRequest;
import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.dto.response.WaitlistResponse;
import com.ticketbooking.service.WaitlistOfferService;
import com.ticketbooking.service.WaitlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;
    private final WaitlistOfferService waitlistOfferService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<WaitlistResponse>> joinWaitlist(
            @Valid @RequestBody WaitlistRequest request,
            Principal principal) {
        WaitlistResponse response = waitlistService.joinWaitlist(request, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully joined the waitlist", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<WaitlistResponse>>> getUserWaitlist(Principal principal) {
        List<WaitlistResponse> responses = waitlistService.getUserWaitlist(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("User waitlist retrieved successfully", responses));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> leaveWaitlist(
            @PathVariable Long id,
            Principal principal) {
        waitlistService.leaveWaitlist(id, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Successfully left the waitlist", null));
    }

    @PostMapping("/offers/{token}/accept")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<BookingResponse>> acceptOffer(
            @PathVariable String token,
            Principal principal) {
        BookingResponse booking = waitlistOfferService.acceptOffer(token, principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Offer accepted and booking created successfully", booking));
    }
}
