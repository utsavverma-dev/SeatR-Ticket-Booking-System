package com.ticketbooking.service.impl;

import com.ticketbooking.dto.SeatHoldRequest;
import com.ticketbooking.dto.SeatHoldResponse;
import com.ticketbooking.dto.ShowSeatResponse;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.ShowSeat;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.enums.SeatStatus;
import com.ticketbooking.exception.BadRequestException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.exception.UnauthorizedException;
import com.ticketbooking.mapper.SeatMapper;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.ShowSeatRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private static final Logger log = LoggerFactory.getLogger(SeatServiceImpl.class);
    private static final int HOLD_TTL_MINUTES = 10;
    private static final int MAX_SEATS_PER_BOOKING = 10;

    private final ShowSeatRepository showSeatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final SeatMapper seatMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<ShowSeatResponse> getSeatsByEventId(Long eventId) {
        Event event = eventRepository.findByIdWithDetails(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        List<ShowSeat> seats = showSeatRepository.findByEventIdWithDetails(eventId);
        return seats.stream()
                .map(seat -> seatMapper.toResponse(seat, event.getPrices()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SeatHoldResponse holdSeats(SeatHoldRequest request, String customerEmail) {
        try {
            return attemptHoldSeats(request, customerEmail);
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking failure while holding seats. Retrying once...");
            try {
                return attemptHoldSeats(request, customerEmail);
            } catch (ObjectOptimisticLockingFailureException ex) {
                log.error("Optimistic locking failure retry failed. Seats already held/booked by another user.");
                throw new ResponseStatusException(HttpStatus.CONFLICT, "One or more selected seats were just taken by someone else.");
            }
        }
    }

    private SeatHoldResponse attemptHoldSeats(SeatHoldRequest request, String customerEmail) {
        validateHoldRequest(request);

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.eventId()));

        if (event.getEventDate().isBefore(LocalDateTime.now().toLocalDate()) ||
            (event.getEventDate().isEqual(LocalDateTime.now().toLocalDate()) && event.getEventTime().isBefore(LocalDateTime.now().toLocalTime()))) {
            throw new BadRequestException("Cannot hold seats for a past event");
        }
        if (event.getStatus() == com.ticketbooking.entity.enums.EventStatus.CANCELLED) {
            throw new BadRequestException("Cannot hold seats for a cancelled event");
        }

        List<ShowSeat> seats = showSeatRepository.findByIdsAndEventId(request.seatIds(), request.eventId());
        if (seats.size() != request.seatIds().size()) {
            throw new BadRequestException("One or more seats not found for this event");
        }

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(HOLD_TTL_MINUTES);

        for (ShowSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new BadRequestException("Seat " + seat.getVenueSeat().getSeatLabel() + " is already booked");
            }
            if (seat.getStatus() == SeatStatus.HELD) {
                if (seat.getHeldBy() != null && seat.getHeldBy().getId().equals(customer.getId())) {
                    // Refresh TTL if already held by same user
                    seat.setHoldExpiresAt(expiresAt);
                } else if (seat.getHoldExpiresAt() != null && seat.getHoldExpiresAt().isAfter(LocalDateTime.now())) {
                    throw new BadRequestException("Seat " + seat.getVenueSeat().getSeatLabel() + " is currently held by someone else");
                }
            }

            seat.setStatus(SeatStatus.HELD);
            seat.setHeldBy(customer);
            seat.setHoldExpiresAt(expiresAt);
            
            showSeatRepository.save(seat); // Saves trigger version increment
        }

        log.info("User {} held {} seats for event {}. Expires at {}", customerEmail, seats.size(), request.eventId(), expiresAt);
        
        broadcastSeatUpdates(request.eventId(), seats, "HELD");

        return new SeatHoldResponse(
                request.eventId(),
                seats.stream().map(ShowSeat::getId).collect(Collectors.toList()),
                expiresAt.toString()
        );
    }

    @Override
    @Transactional
    public void releaseSeats(SeatHoldRequest request, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        List<ShowSeat> seats = showSeatRepository.findByIdsAndEventId(request.seatIds(), request.eventId());

        for (ShowSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.HELD) {
                if (seat.getHeldBy() != null && !seat.getHeldBy().getId().equals(customer.getId())) {
                    throw new UnauthorizedException("Cannot release seats held by another user");
                }
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setHeldBy(null);
                seat.setHoldExpiresAt(null);
                showSeatRepository.save(seat);
            }
        }
        log.info("User {} released {} seats for event {}", customerEmail, seats.size(), request.eventId());
        broadcastSeatUpdates(request.eventId(), seats, "AVAILABLE");
    }

    @Override
    @Transactional
    public void releaseExpiredHolds() {
        List<ShowSeat> expiredSeats = showSeatRepository.findByStatusAndHoldExpiresAtBefore(SeatStatus.HELD, LocalDateTime.now());
        
        if (expiredSeats.isEmpty()) {
            return;
        }

        // Group by event for broadcasting
        Map<Long, List<ShowSeat>> eventSeatsMap = expiredSeats.stream()
                .collect(Collectors.groupingBy(s -> s.getEvent().getId()));

        for (ShowSeat seat : expiredSeats) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHeldBy(null);
            seat.setHoldExpiresAt(null);
            showSeatRepository.save(seat);
        }

        log.info("Scheduler released {} expired held seats", expiredSeats.size());

        eventSeatsMap.forEach((eventId, seats) -> {
            broadcastSeatUpdates(eventId, seats, "AVAILABLE");
        });
    }

    private void validateHoldRequest(SeatHoldRequest request) {
        if (request.seatIds() == null || request.seatIds().isEmpty()) {
            throw new BadRequestException("Minimum 1 seat required");
        }
        if (request.seatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw new BadRequestException("Maximum " + MAX_SEATS_PER_BOOKING + " seats allowed per transaction");
        }
        long uniqueSeatCount = request.seatIds().stream().distinct().count();
        if (uniqueSeatCount != request.seatIds().size()) {
            throw new BadRequestException("Duplicate seat IDs provided");
        }
    }

    private void broadcastSeatUpdates(Long eventId, List<ShowSeat> seats, String status) {
        String topic = "/topic/events/" + eventId + "/seats";
        for (ShowSeat seat : seats) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("seatId", seat.getId());
            payload.put("status", status);
            messagingTemplate.convertAndSend(topic, payload);
        }
    }
}
