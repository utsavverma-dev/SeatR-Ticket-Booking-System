package com.ticketbooking.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VenueRequest(
        @NotBlank(message = "Venue name is required")
        String name,

        @NotBlank(message = "Address is required")
        String address,

        @NotBlank(message = "City is required")
        String city,

        String state,

        @NotEmpty(message = "At least one seat configuration is required")
        @Valid
        List<SeatRequest> seats
) {
}
