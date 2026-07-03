package com.ticketbooking.service;

import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.EventResponse;

import java.util.List;

public interface EventService {

    EventResponse createEvent(EventRequest request, String organiserEmail);

    EventResponse updateEvent(Long id, EventRequest request, String organiserEmail);

    void deleteEvent(Long id, String organiserEmail);

    EventResponse getEvent(Long id);

    List<EventResponse> getAllEvents();
}
