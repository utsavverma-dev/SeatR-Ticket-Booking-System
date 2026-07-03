package com.ticketbooking.service.impl;

import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.EventPrice;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.Venue;
import com.ticketbooking.entity.enums.EventStatus;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.exception.UnauthorizedException;
import com.ticketbooking.mapper.EventMapper;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.VenueRepository;
import com.ticketbooking.service.EventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final com.ticketbooking.repository.VenueSeatRepository venueSeatRepository;
    private final com.ticketbooking.repository.ShowSeatRepository showSeatRepository;

    @Override
    @Transactional
    public EventResponse createEvent(EventRequest request, String organiserEmail) {
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", request.venueId()));

        User organiser = userRepository.findByEmail(organiserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", organiserEmail));

        Event event = Event.builder()
                .title(request.title())
                .description(request.description())
                .venue(venue)
                .organiser(organiser)
                .eventDate(request.eventDate())
                .eventTime(request.eventTime())
                .status(EventStatus.UPCOMING)
                .build();

        List<EventPrice> prices = request.prices().stream()
                .map(priceReq -> EventPrice.builder()
                        .event(event)
                        .category(priceReq.category())
                        .price(priceReq.price())
                        .build())
                .toList();

        event.setPrices(new java.util.ArrayList<>(prices));

        Event savedEvent = eventRepository.save(event);

        // Seed ShowSeats
        List<com.ticketbooking.entity.VenueSeat> venueSeats = venueSeatRepository.findByVenueId(venue.getId());
        List<com.ticketbooking.entity.ShowSeat> showSeats = venueSeats.stream()
                .map(vs -> com.ticketbooking.entity.ShowSeat.builder()
                        .event(savedEvent)
                        .venueSeat(vs)
                        .status(com.ticketbooking.entity.enums.SeatStatus.AVAILABLE)
                        .version(0L)
                        .build())
                .toList();
        showSeatRepository.saveAll(showSeats);

        log.info("Event created: {} (ID: {}) by organiser: {} with {} seats", savedEvent.getTitle(), savedEvent.getId(), organiserEmail, showSeats.size());

        return EventMapper.toResponse(savedEvent);
    }

    @Override
    @Transactional
    public EventResponse updateEvent(Long id, EventRequest request, String organiserEmail) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        verifyOwnership(event, organiserEmail);

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", request.venueId()));

        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setVenue(venue);
        event.setEventDate(request.eventDate());
        event.setEventTime(request.eventTime());

        // Clear and recreate prices
        event.getPrices().clear();
        request.prices().forEach(priceReq -> {
            EventPrice price = EventPrice.builder()
                    .event(event)
                    .category(priceReq.category())
                    .price(priceReq.price())
                    .build();
            event.getPrices().add(price);
        });

        Event updatedEvent = eventRepository.save(event);
        log.info("Event updated: {} (ID: {})", updatedEvent.getTitle(), updatedEvent.getId());

        return EventMapper.toResponse(updatedEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id, String organiserEmail) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        verifyOwnership(event, organiserEmail);

        eventRepository.delete(event);
        log.info("Event deleted: {} (ID: {})", event.getTitle(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long id) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", id));

        return EventMapper.toResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAllWithDetails().stream()
                .map(EventMapper::toResponse)
                .toList();
    }

    private void verifyOwnership(Event event, String organiserEmail) {
        if (!event.getOrganiser().getEmail().equals(organiserEmail)) {
            throw new UnauthorizedException("You can only modify your own events");
        }
    }
}
