package com.ticketbooking.dto.response;

import com.ticketbooking.entity.enums.EventStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record EventResponse(
        Long id,
        String title,
        String description,
        String venueName,
        Long venueId,
        String organiserName,
        Long organiserId,
        LocalDate eventDate,
        LocalTime eventTime,
        EventStatus status,
        List<EventPriceResponse> prices,
        LocalDateTime createdAt
) {
}
