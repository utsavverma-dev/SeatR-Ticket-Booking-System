package com.ticketbooking.dto.response;

import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.entity.enums.WaitlistStatus;

import java.time.LocalDateTime;

public record WaitlistResponse(
    Long id,
    Long eventId,
    String eventTitle,
    SeatCategory seatCategory,
    Integer positionInQueue,
    WaitlistStatus status,
    LocalDateTime createdAt
) {}
