package com.ticketbooking.dto.request;

import com.ticketbooking.entity.enums.SeatCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EventPriceRequest(
        @NotNull(message = "Category is required")
        SeatCategory category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price
) {
}
