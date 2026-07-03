package com.ticketbooking.service;

import com.ticketbooking.dto.request.EventPriceRequest;
import com.ticketbooking.dto.request.EventRequest;
import com.ticketbooking.dto.response.EventResponse;
import com.ticketbooking.entity.*;
import com.ticketbooking.entity.enums.EventStatus;
import com.ticketbooking.entity.enums.RoleName;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.exception.UnauthorizedException;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.VenueRepository;
import com.ticketbooking.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.ticketbooking.repository.VenueSeatRepository venueSeatRepository;

    @Mock
    private com.ticketbooking.repository.ShowSeatRepository showSeatRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private Venue testVenue;
    private User testOrganiser;
    private Event testEvent;
    private EventRequest testRequest;

    @BeforeEach
    void setUp() {
        testVenue = Venue.builder()
                .id(1L)
                .name("Test Theater")
                .address("123 Main St")
                .city("Mumbai")
                .state("MH")
                .capacity(100)
                .seats(new ArrayList<>())
                .build();

        Role organiserRole = new Role(2L, RoleName.ORGANISER);
        testOrganiser = User.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .password("encoded")
                .role(organiserRole)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Test Concert")
                .description("A great concert")
                .venue(testVenue)
                .organiser(testOrganiser)
                .eventDate(LocalDate.now().plusDays(30))
                .eventTime(LocalTime.of(19, 0))
                .status(EventStatus.UPCOMING)
                .prices(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        testEvent.getPrices().add(EventPrice.builder()
                .id(1L)
                .event(testEvent)
                .category(SeatCategory.PREMIUM)
                .price(new BigDecimal("500.00"))
                .build());

        List<EventPriceRequest> priceRequests = List.of(
                new EventPriceRequest(SeatCategory.PREMIUM, new BigDecimal("500.00"))
        );

        testRequest = new EventRequest(
                "Test Concert",
                "A great concert",
                1L,
                LocalDate.now().plusDays(30),
                LocalTime.of(19, 0),
                priceRequests
        );
    }

    @Test
    @DisplayName("Should create event successfully")
    void createEvent_withValidRequest_shouldReturnEventResponse() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testOrganiser));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(venueSeatRepository.findByVenueId(1L)).thenReturn(new java.util.ArrayList<>());
        when(showSeatRepository.saveAll(anyList())).thenReturn(new java.util.ArrayList<>());

        EventResponse response = eventService.createEvent(testRequest, "jane@example.com");

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Concert");
        assertThat(response.venueName()).isEqualTo("Test Theater");
        assertThat(response.organiserName()).isEqualTo("Jane Smith");
        assertThat(response.status()).isEqualTo(EventStatus.UPCOMING);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw exception when venue not found during event creation")
    void createEvent_withInvalidVenue_shouldThrowResourceNotFoundException() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.createEvent(testRequest, "jane@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue");

        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Should get event by ID")
    void getEvent_withValidId_shouldReturnEventResponse() {
        when(eventRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testEvent));

        EventResponse response = eventService.getEvent(1L);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Concert");
    }

    @Test
    @DisplayName("Should throw exception when event not found")
    void getEvent_withInvalidId_shouldThrowResourceNotFoundException() {
        when(eventRepository.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEvent(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all events")
    void getAllEvents_shouldReturnList() {
        when(eventRepository.findAllWithDetails()).thenReturn(List.of(testEvent));

        List<EventResponse> responses = eventService.getAllEvents();

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should delete event when user is owner")
    void deleteEvent_asOwner_shouldDeleteEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        eventService.deleteEvent(1L, "jane@example.com");

        verify(eventRepository).delete(testEvent);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to delete event")
    void deleteEvent_asNonOwner_shouldThrowUnauthorizedException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThatThrownBy(() -> eventService.deleteEvent(1L, "other@example.com"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("your own events");

        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    @DisplayName("Should update event when user is owner")
    void updateEvent_asOwner_shouldReturnUpdatedEvent() {
        when(eventRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testEvent));
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        EventResponse response = eventService.updateEvent(1L, testRequest, "jane@example.com");

        assertThat(response).isNotNull();
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to update event")
    void updateEvent_asNonOwner_shouldThrowUnauthorizedException() {
        when(eventRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(testEvent));

        assertThatThrownBy(() -> eventService.updateEvent(1L, testRequest, "other@example.com"))
                .isInstanceOf(UnauthorizedException.class);

        verify(eventRepository, never()).save(any(Event.class));
    }
}
