package com.ticketbooking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookingRequest(
    @NotNull(message = "Event ID is required")
    Long eventId,

    @NotEmpty(message = "At least one seat ID must be provided")
    List<Long> seatIds
) {
}
