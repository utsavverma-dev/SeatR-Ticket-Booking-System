package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingHistoryResponse(
    Long id,
    String bookingReference,
    String eventTitle,
    String venueName,
    LocalDateTime eventDate,
    LocalDateTime bookingTime,
    String status,
    BigDecimal totalAmount
) {
}
