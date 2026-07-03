package com.ticketbooking.controller;

import com.ticketbooking.dto.response.ApiResponse;
import com.ticketbooking.dto.response.BookingStatsResponse;
import com.ticketbooking.dto.response.RevenueResponse;
import com.ticketbooking.dto.response.UpcomingEventResponse;
import com.ticketbooking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<RevenueResponse>>> getRevenuePerEvent(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Revenue per event retrieved", dashboardService.getRevenuePerEvent(principal.getName())));
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingStatsResponse>>> getBookingStatsPerEvent(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Booking stats retrieved", dashboardService.getBookingStatsPerEvent(principal.getName())));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingStatsResponse>> getOverallStatistics(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Overall stats retrieved", dashboardService.getOverallBookingStats(principal.getName())));
    }

    @GetMapping("/upcoming-events")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<UpcomingEventResponse>>> getUpcomingEvents(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Upcoming events retrieved", dashboardService.getUpcomingEvents(principal.getName())));
    }

    @GetMapping("/popular-events")
    @PreAuthorize("hasAnyRole('ORGANISER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingStatsResponse>>> getPopularEvents(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Popular events retrieved", dashboardService.getPopularEvents(principal.getName())));
    }
}
