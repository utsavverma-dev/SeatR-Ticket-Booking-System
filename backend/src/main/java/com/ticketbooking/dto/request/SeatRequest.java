package com.ticketbooking.dto.request;

import com.ticketbooking.entity.enums.SeatCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SeatRequest(
        @NotNull(message = "Seat category is required")
        SeatCategory category,

        @NotBlank(message = "Row prefix is required")
        String rowPrefix,

        @Min(value = 1, message = "Seat count must be at least 1")
        int seatCount
) {
}
