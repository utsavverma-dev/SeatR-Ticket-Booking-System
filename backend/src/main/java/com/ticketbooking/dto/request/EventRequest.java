package com.ticketbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Venue ID is required")
        Long venueId,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future")
        LocalDate eventDate,

        @NotNull(message = "Event time is required")
        LocalTime eventTime,

        @NotEmpty(message = "At least one price category is required")
        @Valid
        List<EventPriceRequest> prices
) {
}
