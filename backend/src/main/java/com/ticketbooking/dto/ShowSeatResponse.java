package com.ticketbooking.dto;

import java.math.BigDecimal;

public record ShowSeatResponse(
    Long id,
    Long eventId,
    String seatLabel,
    String rowNumber,
    int seatNumber,
    String category,
    BigDecimal price,
    String status
) {
}
