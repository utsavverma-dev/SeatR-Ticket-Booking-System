package com.ticketbooking.dto.response;

import java.math.BigDecimal;

public record RevenueResponse(
    Long eventId,
    String eventTitle,
    BigDecimal totalRevenue
) {}
