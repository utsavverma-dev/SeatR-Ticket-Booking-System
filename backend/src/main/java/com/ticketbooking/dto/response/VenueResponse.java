package com.ticketbooking.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record VenueResponse(
        Long id,
        String name,
        String address,
        String city,
        String state,
        int capacity,
        List<SeatInfo> seatSummary,
        LocalDateTime createdAt
) {
}
