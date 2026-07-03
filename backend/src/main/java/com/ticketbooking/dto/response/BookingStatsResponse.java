package com.ticketbooking.dto.response;

public record BookingStatsResponse(
    Long eventId,
    String eventTitle,
    Integer totalBookings,
    Integer totalCancellations,
    Integer seatsSold,
    Integer waitlistCount,
    Double seatOccupancyPercentage
) {}
