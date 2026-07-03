package com.ticketbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record BookingDetailsResponse(
    Long id,
    String bookingReference,
    String customerName,
    String customerEmail,
    String eventTitle,
    String venueName,
    LocalDateTime eventDate,
    LocalDateTime bookingTime,
    String status,
    BigDecimal totalAmount,
    List<BookingSeatInfo> seats
) {
    public record BookingSeatInfo(
        String seatLabel,
        String category,
        BigDecimal price
    ) {}
}
