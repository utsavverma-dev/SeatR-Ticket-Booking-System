package com.ticketbooking.service;

import com.ticketbooking.dto.response.BookingStatsResponse;
import com.ticketbooking.dto.response.RevenueResponse;
import com.ticketbooking.dto.response.UpcomingEventResponse;
import java.util.List;

public interface DashboardService {
    List<RevenueResponse> getRevenuePerEvent(String email);
    List<BookingStatsResponse> getBookingStatsPerEvent(String email);
    RevenueResponse getOverallRevenue(String email);
    BookingStatsResponse getOverallBookingStats(String email);
    List<UpcomingEventResponse> getUpcomingEvents(String email);
    List<BookingStatsResponse> getPopularEvents(String email);
}
