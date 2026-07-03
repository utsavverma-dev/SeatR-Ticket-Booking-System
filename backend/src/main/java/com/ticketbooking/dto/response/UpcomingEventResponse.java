package com.ticketbooking.dto.response;

import java.time.LocalDateTime;

public record UpcomingEventResponse(
    Long eventId,
    String title,
    LocalDateTime eventDateTime,
    String venueName
) {}
