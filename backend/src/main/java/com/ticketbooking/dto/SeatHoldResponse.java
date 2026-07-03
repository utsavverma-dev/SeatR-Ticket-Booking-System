package com.ticketbooking.dto;

import java.util.List;

public record SeatHoldResponse(
    Long eventId,
    List<Long> heldSeatIds,
    String holdExpiresAt
) {
}
