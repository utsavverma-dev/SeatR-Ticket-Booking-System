package com.ticketbooking.mapper;

import com.ticketbooking.dto.response.EventPriceResponse;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.entity.Event;

import java.util.List;

public final class EventMapper {

    private EventMapper() {
    }

    public static EventResponse toResponse(Event event) {
        List<EventPriceResponse> prices = event.getPrices().stream()
                .map(price -> new EventPriceResponse(price.getCategory(), price.getPrice()))
                .toList();

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getVenue().getName(),
                event.getVenue().getId(),
                event.getOrganiser().getFirstName() + " " + event.getOrganiser().getLastName(),
                event.getOrganiser().getId(),
                event.getEventDate(),
                event.getEventTime(),
                event.getStatus(),
                prices,
                event.getCreatedAt()
        );
    }
}
