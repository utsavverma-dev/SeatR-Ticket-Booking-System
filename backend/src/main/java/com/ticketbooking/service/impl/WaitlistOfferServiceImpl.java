package com.ticketbooking.service.impl;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.entity.ShowSeat;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.Waitlist;
import com.ticketbooking.entity.WaitlistOffer;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.entity.enums.SeatStatus;
import com.ticketbooking.entity.enums.WaitlistOfferStatus;
import com.ticketbooking.entity.enums.WaitlistStatus;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.exception.UnauthorizedException;
import com.ticketbooking.exception.ValidationException;
import com.ticketbooking.repository.ShowSeatRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.WaitlistOfferRepository;
import com.ticketbooking.repository.WaitlistRepository;
import com.ticketbooking.service.BookingService;
import com.ticketbooking.service.EmailService;
import com.ticketbooking.service.WaitlistOfferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WaitlistOfferServiceImpl implements WaitlistOfferService {

    private final WaitlistRepository waitlistRepository;
    private final WaitlistOfferRepository waitlistOfferRepository;
    private final ShowSeatRepository showSeatRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final BookingService bookingService;
    private final SimpMessagingTemplate messagingTemplate;

    public WaitlistOfferServiceImpl(WaitlistRepository waitlistRepository,
                                    WaitlistOfferRepository waitlistOfferRepository,
                                    ShowSeatRepository showSeatRepository,
                                    EmailService emailService,
                                    UserRepository userRepository,
                                    @Lazy BookingService bookingService,
                                    SimpMessagingTemplate messagingTemplate) {
        this.waitlistRepository = waitlistRepository;
        this.waitlistOfferRepository = waitlistOfferRepository;
        this.showSeatRepository = showSeatRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.bookingService = bookingService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public void generateOfferForReleasedSeats(Long eventId, SeatCategory category, List<Long> seatIds) {
        Optional<Waitlist> nextCustomerOpt = waitlistRepository.findFirstWaitingCustomer(eventId, category);

        List<ShowSeat> seats = showSeatRepository.findAllById(seatIds);

        if (nextCustomerOpt.isPresent()) {
            Waitlist nextCustomer = nextCustomerOpt.get();
            
            // Waitlist status remains WAITING, we track offer status in WaitlistOfferStatus

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);

            WaitlistOffer offer = WaitlistOffer.builder()
                    .waitlist(nextCustomer)
                    .token(token)
                    .expiryTime(expiry)
                    .status(WaitlistOfferStatus.PENDING)
                    .build();

            for (ShowSeat seat : seats) {
                offer.addSeat(seat);
                seat.setStatus(SeatStatus.HELD);
                seat.setHeldBy(nextCustomer.getUser());
                seat.setHoldExpiresAt(expiry);
            }

            waitlistOfferRepository.save(offer);
            showSeatRepository.saveAll(seats);
            
            seats.forEach(seat -> {
                messagingTemplate.convertAndSend("/topic/events/" + eventId + "/seats",
                    "{\"seatId\":" + seat.getId() + ",\"status\":\"HELD\"}");
            });

            emailService.sendWaitlistOfferEmail(offer, nextCustomer.getUser().getEmail(), nextCustomer.getUser().getFirstName());
            log.info("Generated Waitlist Offer {} for User {}", token, nextCustomer.getUser().getEmail());
        } else {
            // No one on waitlist, just mark seats as AVAILABLE
            for (ShowSeat seat : seats) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setHeldBy(null);
                seat.setHoldExpiresAt(null);
            }
            showSeatRepository.saveAll(seats);
            
            seats.forEach(seat -> {
                messagingTemplate.convertAndSend("/topic/events/" + eventId + "/seats",
                    "{\"seatId\":" + seat.getId() + ",\"status\":\"AVAILABLE\"}");
            });
            log.info("No waitlist found for event {}, category {}. Seats released to pool.", eventId, category);
        }
    }

    @Override
    @Transactional
    public BookingResponse acceptOffer(String token, String email) {
        WaitlistOffer offer = waitlistOfferRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid offer token"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!offer.getWaitlist().getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This offer does not belong to you.");
        }

        if (offer.getStatus() != WaitlistOfferStatus.PENDING) {
            throw new ValidationException("This offer is no longer valid (Status: " + offer.getStatus() + ").");
        }

        if (offer.getExpiryTime().isBefore(LocalDateTime.now())) {
            offer.setStatus(WaitlistOfferStatus.EXPIRED);
            offer.getWaitlist().setStatus(WaitlistStatus.EXPIRED);
            waitlistOfferRepository.save(offer);
            throw new ValidationException("This offer has expired.");
        }

        // Process Acceptance
        List<ShowSeat> seats = offer.getSeats();
        List<Long> seatIds = seats.stream().map(ShowSeat::getId).collect(Collectors.toList());

        for (ShowSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.HELD || seat.getHeldBy() == null || !user.getId().equals(seat.getHeldBy().getId())) {
                throw new ValidationException("Seats are no longer available for this offer.");
            }
        }

        // Update Offer Status
        offer.setStatus(WaitlistOfferStatus.ACCEPTED);
        offer.getWaitlist().setStatus(WaitlistStatus.COMPLETED);
        waitlistOfferRepository.save(offer);

        // BookingService createBooking handles moving from HELD -> BOOKED
        BookingRequest request = new BookingRequest(offer.getWaitlist().getEvent().getId(), seatIds);
        return bookingService.createBooking(request, email);
    }

    @Override
    @Transactional
    public void processExpiredOffers() {
        List<WaitlistOffer> expiredOffers = waitlistOfferRepository.findByStatusAndExpiryTimeBefore(WaitlistOfferStatus.PENDING, LocalDateTime.now());
        
        for (WaitlistOffer offer : expiredOffers) {
            offer.setStatus(WaitlistOfferStatus.EXPIRED);
            offer.getWaitlist().setStatus(WaitlistStatus.EXPIRED);
            waitlistOfferRepository.save(offer);
            
            log.info("Waitlist Offer {} expired for User {}", offer.getToken(), offer.getWaitlist().getUser().getEmail());

            List<ShowSeat> seats = offer.getSeats();
            List<Long> seatIds = seats.stream().map(ShowSeat::getId).collect(Collectors.toList());

            // Since generateOfferForReleasedSeats expects them to be in the database and re-processes them, we should actually
            // clear the held state first so it can process them as fresh released seats.
            for (ShowSeat seat : seats) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setHeldBy(null);
                seat.setHoldExpiresAt(null);
            }
            showSeatRepository.saveAll(seats);

            // Check if there are other waiting customers for these same seats
            generateOfferForReleasedSeats(offer.getWaitlist().getEvent().getId(), offer.getWaitlist().getSeatCategory(), seatIds);
        }
    }
}
