package com.ticketbooking.service.impl;

import com.ticketbooking.dto.request.WaitlistRequest;
import com.ticketbooking.dto.response.WaitlistResponse;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.Waitlist;
import com.ticketbooking.entity.enums.WaitlistStatus;
import com.ticketbooking.exception.DuplicateResourceException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.exception.UnauthorizedException;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.WaitlistRepository;
import com.ticketbooking.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final com.ticketbooking.repository.BookingRepository bookingRepository;

    @Override
    @Transactional
    public WaitlistResponse joinWaitlist(WaitlistRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findByIdWithDetails(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() == com.ticketbooking.entity.enums.EventStatus.CANCELLED) {
            throw new com.ticketbooking.exception.BadRequestException("Cannot join waitlist for a cancelled event.");
        }

        if (event.getEventDate().isBefore(java.time.LocalDate.now()) ||
            (event.getEventDate().isEqual(java.time.LocalDate.now()) && event.getEventTime().isBefore(java.time.LocalTime.now()))) {
            throw new com.ticketbooking.exception.BadRequestException("Cannot join waitlist for a past event.");
        }

        boolean validCategory = event.getPrices().stream()
                .anyMatch(ep -> ep.getCategory() == request.seatCategory());
        if (!validCategory) {
            throw new com.ticketbooking.exception.BadRequestException("Invalid seat category for this event.");
        }

        if (waitlistRepository.existsByEventIdAndUserIdAndSeatCategory(event.getId(), user.getId(), request.seatCategory())) {
            throw new DuplicateResourceException("You are already on the waitlist for this event and category.");
        }

        boolean hasBooking = bookingRepository.findByCustomerIdWithDetails(user.getId()).stream()
                .filter(b -> b.getEvent().getId().equals(event.getId()) && b.getStatus() == com.ticketbooking.entity.enums.BookingStatus.CONFIRMED)
                .flatMap(b -> b.getSeats().stream())
                .anyMatch(bs -> bs.getShowSeat().getVenueSeat().getCategory() == request.seatCategory());

        if (hasBooking) {
            throw new DuplicateResourceException("You already have a booking for this event and category.");
        }

        Integer maxPosition = waitlistRepository.findMaxPosition(event.getId(), request.seatCategory());

        Waitlist waitlist = Waitlist.builder()
                .event(event)
                .user(user)
                .seatCategory(request.seatCategory())
                .positionInQueue(maxPosition + 1)
                .status(WaitlistStatus.WAITING)
                .build();

        waitlist = waitlistRepository.save(waitlist);
        log.info("User {} joined waitlist for Event {}, Category {}", email, event.getId(), request.seatCategory());

        return mapToResponse(waitlist);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaitlistResponse> getUserWaitlist(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return waitlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void leaveWaitlist(Long waitlistId, String email) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new ResourceNotFoundException("Waitlist entry not found"));

        if (!waitlist.getUser().getEmail().equals(email)) {
            throw new UnauthorizedException("You can only leave your own waitlist entries.");
        }

        waitlist.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(waitlist);
        log.info("User {} left waitlist {}", email, waitlistId);
    }

    private WaitlistResponse mapToResponse(Waitlist waitlist) {
        return new WaitlistResponse(
                waitlist.getId(),
                waitlist.getEvent().getId(),
                waitlist.getEvent().getTitle(),
                waitlist.getSeatCategory(),
                waitlist.getPositionInQueue(),
                waitlist.getStatus(),
                waitlist.getCreatedAt()
        );
    }
}
