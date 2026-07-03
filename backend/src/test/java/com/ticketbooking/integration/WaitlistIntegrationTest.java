package com.ticketbooking.integration;

import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.dto.request.WaitlistRequest;
import com.ticketbooking.dto.response.WaitlistResponse;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.Role;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.Venue;
import com.ticketbooking.entity.WaitlistOffer;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.entity.enums.SeatStatus;
import com.ticketbooking.entity.enums.WaitlistOfferStatus;
import com.ticketbooking.entity.enums.WaitlistStatus;
import com.ticketbooking.repository.*;
import com.ticketbooking.service.BookingService;
import com.ticketbooking.service.WaitlistOfferService;
import com.ticketbooking.service.WaitlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class WaitlistIntegrationTest {

    @Autowired private WaitlistService waitlistService;
    @Autowired private WaitlistOfferService waitlistOfferService;
    @Autowired private BookingService bookingService;
    @Autowired private WaitlistRepository waitlistRepository;
    @Autowired private WaitlistOfferRepository waitlistOfferRepository;
    @Autowired private ShowSeatRepository showSeatRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private User userA;
    private User userB;
    private User userC;
    private Event event;
    private Long seatId;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role(null, com.ticketbooking.entity.enums.RoleName.CUSTOMER);
        Role adminRole = new Role(null, com.ticketbooking.entity.enums.RoleName.ADMIN);
        roleRepository.saveAll(List.of(customerRole, adminRole));

        userA = User.builder().email("customera@test.com").firstName("A").lastName("A").password("pass").role(customerRole).build();
        userB = User.builder().email("customerb@test.com").firstName("B").lastName("B").password("pass").role(customerRole).build();
        userC = User.builder().email("customerc@test.com").firstName("C").lastName("C").password("pass").role(customerRole).build();
        User admin = User.builder().email("admin@test.com").firstName("Admin").lastName("Admin").password("pass").role(adminRole).build();
        userRepository.saveAll(List.of(userA, userB, userC, admin));

        Venue venue = Venue.builder().name("Test Venue").address("123 Main").city("City").capacity(100).build();
        venueRepository.save(venue);

        com.ticketbooking.entity.VenueSeat venueSeat = com.ticketbooking.entity.VenueSeat.builder().venue(venue).rowNumber("A").seatNumber(1).seatLabel("A1").category(SeatCategory.PREMIUM).build();
        venue.getSeats().add(venueSeat);
        venue = venueRepository.save(venue);
        venueSeat = venue.getSeats().get(0);

        event = Event.builder()
            .title("Test Event")
            .venue(venue)
            .organiser(admin)
            .eventDate(java.time.LocalDate.now().plusDays(1))
            .eventTime(java.time.LocalTime.of(20, 0))
            .status(com.ticketbooking.entity.enums.EventStatus.UPCOMING)
            .build();
        
        com.ticketbooking.entity.EventPrice eventPrice = com.ticketbooking.entity.EventPrice.builder().event(event).category(SeatCategory.PREMIUM).price(java.math.BigDecimal.valueOf(100)).build();
        event.getPrices().add(eventPrice);
        eventRepository.save(event);

        com.ticketbooking.entity.ShowSeat showSeat = com.ticketbooking.entity.ShowSeat.builder().event(event).venueSeat(venueSeat).status(SeatStatus.AVAILABLE).build();
        showSeat = showSeatRepository.save(showSeat);
        seatId = showSeat.getId();
    }

    @Test
    void scenario1_A_books_B_waitlists_A_cancels_B_accepts() {
        // A books
        showSeatRepository.findById(seatId).ifPresent(s -> {
            s.setStatus(SeatStatus.HELD);
            s.setHeldBy(userA);
            showSeatRepository.save(s);
        });
        BookingResponse bookingA = bookingService.createBooking(new BookingRequest(event.getId(), List.of(seatId)), userA.getEmail());
        
        // B joins waitlist
        waitlistService.joinWaitlist(new WaitlistRequest(event.getId(), SeatCategory.PREMIUM), userB.getEmail());
        
        // A cancels
        bookingService.cancelBooking(bookingA.id(), userA.getEmail());

        // Verify Offer generated for B
        List<WaitlistOffer> offers = waitlistOfferRepository.findAll();
        assertThat(offers).hasSize(1);
        WaitlistOffer offerForB = offers.get(0);
        assertThat(offerForB.getWaitlist().getUser().getId()).isEqualTo(userB.getId());
        assertThat(offerForB.getStatus()).isEqualTo(WaitlistOfferStatus.PENDING);
        assertThat(offerForB.getSeats().get(0).getStatus()).isEqualTo(SeatStatus.HELD);
        assertThat(offerForB.getSeats().get(0).getHeldBy().getId()).isEqualTo(userB.getId());

        // B accepts offer
        BookingResponse bookingB = waitlistOfferService.acceptOffer(offerForB.getToken(), userB.getEmail());
        assertThat(bookingB).isNotNull();
        
        // Verify Seat is BOOKED
        assertThat(showSeatRepository.findById(seatId).get().getStatus()).isEqualTo(SeatStatus.BOOKED);
    }

    @Test
    void scenario2_A_books_B_waitlists_C_waitlists_A_cancels_B_expires_C_gets_offer() {
        // A books
        showSeatRepository.findById(seatId).ifPresent(s -> {
            s.setStatus(SeatStatus.HELD);
            s.setHeldBy(userA);
            showSeatRepository.save(s);
        });
        BookingResponse bookingA = bookingService.createBooking(new BookingRequest(event.getId(), List.of(seatId)), userA.getEmail());
        
        // B and C join waitlist
        waitlistService.joinWaitlist(new WaitlistRequest(event.getId(), SeatCategory.PREMIUM), userB.getEmail());
        waitlistService.joinWaitlist(new WaitlistRequest(event.getId(), SeatCategory.PREMIUM), userC.getEmail());
        
        // A cancels
        bookingService.cancelBooking(bookingA.id(), userA.getEmail());

        WaitlistOffer offerForB = waitlistOfferRepository.findAll().get(0);
        
        // Force offer expiry for B
        offerForB.setExpiryTime(java.time.LocalDateTime.now().minusMinutes(1));
        waitlistOfferRepository.save(offerForB);
        
        waitlistOfferService.processExpiredOffers();
        
        // Verify C gets offer
        List<WaitlistOffer> offers = waitlistOfferRepository.findAll();
        assertThat(offers).hasSize(2);
        WaitlistOffer offerForC = offers.stream().filter(o -> o.getStatus() == WaitlistOfferStatus.PENDING).findFirst().get();
        assertThat(offerForC.getWaitlist().getUser().getId()).isEqualTo(userC.getId());
        assertThat(offerForC.getSeats().get(0).getHeldBy().getId()).isEqualTo(userC.getId());
    }

    @Test
    void scenario3_Offer_expires_No_remaining_users() {
        // A books
        showSeatRepository.findById(seatId).ifPresent(s -> {
            s.setStatus(SeatStatus.HELD);
            s.setHeldBy(userA);
            showSeatRepository.save(s);
        });
        BookingResponse bookingA = bookingService.createBooking(new BookingRequest(event.getId(), List.of(seatId)), userA.getEmail());
        
        // B joins waitlist (nobody else)
        waitlistService.joinWaitlist(new WaitlistRequest(event.getId(), SeatCategory.PREMIUM), userB.getEmail());
        
        // A cancels
        bookingService.cancelBooking(bookingA.id(), userA.getEmail());
        
        WaitlistOffer offerForB = waitlistOfferRepository.findAll().get(0);
        offerForB.setExpiryTime(java.time.LocalDateTime.now().minusMinutes(1));
        waitlistOfferRepository.save(offerForB);
        
        waitlistOfferService.processExpiredOffers();
        
        // Verify Seat returns to AVAILABLE
        assertThat(showSeatRepository.findById(seatId).get().getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(showSeatRepository.findById(seatId).get().getHeldBy()).isNull();
    }

    @Test
    void scenario4_User_joins_twice_409_Conflict() {
        waitlistService.joinWaitlist(new WaitlistRequest(event.getId(), SeatCategory.PREMIUM), userB.getEmail());
        
        assertThrows(com.ticketbooking.exception.DuplicateResourceException.class, () -> {
            waitlistService.joinWaitlist(new WaitlistRequest(event.getId(), SeatCategory.PREMIUM), userB.getEmail());
        });
    }
}
