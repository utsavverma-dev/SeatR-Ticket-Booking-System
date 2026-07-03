package com.ticketbooking.dto.request;

import com.ticketbooking.entity.enums.SeatCategory;
import jakarta.validation.constraints.NotNull;

public record WaitlistRequest(
    @NotNull(message = "Event ID is required")
    Long eventId,
    
    @NotNull(message = "Seat category is required")
    SeatCategory seatCategory
) {}
