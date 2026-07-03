package com.ticketbooking.service.impl;

import com.ticketbooking.dto.BookingDetailsResponse;
import com.ticketbooking.dto.BookingHistoryResponse;
import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.entity.*;
import com.ticketbooking.entity.enums.BookingStatus;
import com.ticketbooking.entity.enums.SeatStatus;
import com.ticketbooking.exception.BadRequestException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.mapper.BookingMapper;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.ShowSeatRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.service.BookingService;
import com.ticketbooking.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final int MAX_SEATS_PER_BOOKING = 10;

    private final BookingRepository bookingRepository;
    private final ShowSeatRepository showSeatRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.ticketbooking.service.WaitlistOfferService waitlistOfferService;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, String customerEmail) {
        validateBookingRequest(request);

        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        Event event = eventRepository.findByIdWithDetails(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", request.eventId()));

        if (event.getEventDate().isBefore(LocalDateTime.now().toLocalDate()) ||
            (event.getEventDate().isEqual(LocalDateTime.now().toLocalDate()) && event.getEventTime().isBefore(LocalDateTime.now().toLocalTime()))) {
            throw new BadRequestException("Cannot book seats for a past event");
        }
        if (event.getStatus() == com.ticketbooking.entity.enums.EventStatus.CANCELLED) {
            throw new BadRequestException("Cannot book seats for a cancelled event");
        }

        List<ShowSeat> showSeats = showSeatRepository.findByIdsAndEventId(request.seatIds(), request.eventId());
        if (showSeats.size() != request.seatIds().size()) {
            throw new BadRequestException("One or more seats not found for this event");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<BookingSeat> bookingSeats = new ArrayList<>();

        for (ShowSeat seat : showSeats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new BadRequestException("Seat " + seat.getVenueSeat().getSeatLabel() + " is already booked");
            }
            if (seat.getStatus() != SeatStatus.HELD || seat.getHeldBy() == null || !seat.getHeldBy().getId().equals(customer.getId())) {
                throw new BadRequestException("Seat " + seat.getVenueSeat().getSeatLabel() + " must be held by you before booking");
            }

            seat.setStatus(SeatStatus.BOOKED);
            seat.setHoldExpiresAt(null);
            
            showSeatRepository.save(seat);

            BigDecimal price = getPriceForCategory(event, seat.getVenueSeat().getCategory().name());
            totalAmount = totalAmount.add(price);

            BookingSeat bookingSeat = BookingSeat.builder()
                    .showSeat(seat)
                    .price(price)
                    .build();
            bookingSeats.add(bookingSeat);
        }

        String bookingReference = generateBookingReference();

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .customer(customer)
                .event(event)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.CONFIRMED)
                .totalAmount(totalAmount)
                .build();

        bookingSeats.forEach(booking::addSeat);

        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking created successfully: {} for user {}", bookingReference, customerEmail);

        broadcastSeatUpdates(request.eventId(), showSeats, "BOOKED");

        // Send Email asynchronously (Assuming EmailService handles exceptions so it won't rollback TX)
        new Thread(() -> emailService.sendBookingConfirmation(savedBooking)).start();

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    @Transactional
    public void cancelBooking(Long id, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        Booking booking = bookingRepository.findByIdAndCustomerIdWithDetails(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }

        if (booking.getEvent().getEventDate().isBefore(LocalDateTime.now().toLocalDate()) ||
            (booking.getEvent().getEventDate().isEqual(LocalDateTime.now().toLocalDate()) && booking.getEvent().getEventTime().isBefore(LocalDateTime.now().toLocalTime()))) {
            throw new BadRequestException("Cannot cancel a booking for a past event");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking cancelled: {} by user {}", booking.getBookingReference(), customerEmail);

        // Group seats by category
        Map<com.ticketbooking.entity.enums.SeatCategory, List<Long>> seatsByCategory = booking.getSeats().stream()
                .map(BookingSeat::getShowSeat)
                .collect(Collectors.groupingBy(
                        seat -> seat.getVenueSeat().getCategory(),
                        Collectors.mapping(ShowSeat::getId, Collectors.toList())
                ));

        // For each category, trigger waitlist reallocation
        for (Map.Entry<com.ticketbooking.entity.enums.SeatCategory, List<Long>> entry : seatsByCategory.entrySet()) {
            // Note: WaitlistOfferService will handle seat status update (OFFERED or AVAILABLE) and broadcast.
            // But we must first detach them from the booking or just pass the IDs? 
            // The waitlist service will update the seats. However, we should clear the heldBy here.
            List<ShowSeat> seats = showSeatRepository.findAllById(entry.getValue());
            seats.forEach(s -> {
                s.setHeldBy(null);
                s.setHoldExpiresAt(null);
            });
            showSeatRepository.saveAll(seats);

            waitlistOfferService.generateOfferForReleasedSeats(booking.getEvent().getId(), entry.getKey(), entry.getValue());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingHistoryResponse> getCustomerBookings(String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        List<Booking> bookings = bookingRepository.findByCustomerIdWithDetails(customer.getId());
        return bookings.stream()
                .map(bookingMapper::toHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDetailsResponse getBookingDetails(Long id, String customerEmail) {
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", customerEmail));

        Booking booking = bookingRepository.findByIdAndCustomerIdWithDetails(id, customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

        return bookingMapper.toDetailsResponse(booking);
    }

    private void validateBookingRequest(BookingRequest request) {
        if (request.seatIds() == null || request.seatIds().isEmpty()) {
            throw new BadRequestException("Minimum 1 seat required");
        }
        if (request.seatIds().size() > MAX_SEATS_PER_BOOKING) {
            throw new BadRequestException("Maximum " + MAX_SEATS_PER_BOOKING + " seats allowed per booking");
        }
        long uniqueSeatCount = request.seatIds().stream().distinct().count();
        if (uniqueSeatCount != request.seatIds().size()) {
            throw new BadRequestException("Duplicate seat IDs provided");
        }
    }

    private String generateBookingReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "BK-" + datePart + "-" + randomPart;
    }

    private BigDecimal getPriceForCategory(Event event, String category) {
        return event.getPrices().stream()
                .filter(ep -> ep.getCategory().name().equals(category))
                .map(EventPrice::getPrice)
                .findFirst()
                .orElse(BigDecimal.ZERO);
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
