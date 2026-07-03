package com.ticketbooking.dto.response;

import com.ticketbooking.entity.enums.SeatCategory;

public record SeatInfo(
        SeatCategory category,
        long count
) {
}
