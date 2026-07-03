package com.ticketbooking.dto.response;

import com.ticketbooking.entity.enums.SeatCategory;

import java.math.BigDecimal;

public record EventPriceResponse(
        SeatCategory category,
        BigDecimal price
) {
}
