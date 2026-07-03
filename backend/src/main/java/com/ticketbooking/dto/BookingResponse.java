package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
    Long id,
    String bookingReference,
    Long eventId,
    String eventTitle,
    LocalDateTime bookingTime,
    String status,
    BigDecimal totalAmount,
    List<String> bookedSeats
) {
}
