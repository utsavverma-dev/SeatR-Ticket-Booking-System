package com.ticketbooking.service;

import com.ticketbooking.dto.request.SeatRequest;
import com.ticketbooking.dto.request.VenueRequest;
import com.ticketbooking.dto.response.VenueResponse;
import com.ticketbooking.entity.Venue;
import com.ticketbooking.entity.VenueSeat;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.exception.DuplicateResourceException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.repository.VenueRepository;
import com.ticketbooking.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueServiceImpl venueService;

    private Venue testVenue;
    private VenueRequest testRequest;

    @BeforeEach
    void setUp() {
        List<SeatRequest> seatRequests = List.of(
                new SeatRequest(SeatCategory.PREMIUM, "A", 5),
                new SeatRequest(SeatCategory.STANDARD, "B", 10)
        );
        testRequest = new VenueRequest("Test Theater", "123 Main St", "Mumbai", "MH", seatRequests);

        testVenue = Venue.builder()
                .id(1L)
                .name("Test Theater")
                .address("123 Main St")
                .city("Mumbai")
                .state("MH")
                .capacity(15)
                .seats(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();

        // Add test seats
        for (int i = 1; i <= 5; i++) {
            testVenue.getSeats().add(VenueSeat.builder()
                    .id((long) i)
                    .venue(testVenue)
                    .category(SeatCategory.PREMIUM)
                    .seatLabel("A" + i)
                    .rowNumber("A")
                    .seatNumber(i)
                    .build());
        }
        for (int i = 1; i <= 10; i++) {
            testVenue.getSeats().add(VenueSeat.builder()
                    .id((long) (i + 5))
                    .venue(testVenue)
                    .category(SeatCategory.STANDARD)
                    .seatLabel("B" + i)
                    .rowNumber("B")
                    .seatNumber(i)
                    .build());
        }
    }

    @Test
    @DisplayName("Should create venue successfully")
    void createVenue_withValidRequest_shouldReturnVenueResponse() {
        when(venueRepository.existsByName(testRequest.name())).thenReturn(false);
        when(venueRepository.save(any(Venue.class))).thenReturn(testVenue);

        VenueResponse response = venueService.createVenue(testRequest);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test Theater");
        assertThat(response.capacity()).isEqualTo(15);
        assertThat(response.seatSummary()).hasSize(2);

        verify(venueRepository).save(any(Venue.class));
    }

    @Test
    @DisplayName("Should throw exception for duplicate venue name")
    void createVenue_withDuplicateName_shouldThrowDuplicateResourceException() {
        when(venueRepository.existsByName(testRequest.name())).thenReturn(true);

        assertThatThrownBy(() -> venueService.createVenue(testRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Test Theater");

        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    @DisplayName("Should get venue by ID")
    void getVenue_withValidId_shouldReturnVenueResponse() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));

        VenueResponse response = venueService.getVenue(1L);

        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("Test Theater");
    }

    @Test
    @DisplayName("Should throw exception when venue not found")
    void getVenue_withInvalidId_shouldThrowResourceNotFoundException() {
        when(venueRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.getVenue(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("Should get all venues")
    void getAllVenues_shouldReturnList() {
        when(venueRepository.findAll()).thenReturn(List.of(testVenue));

        List<VenueResponse> responses = venueService.getAllVenues();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Test Theater");
    }

    @Test
    @DisplayName("Should delete venue successfully")
    void deleteVenue_withValidId_shouldDeleteVenue() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));

        venueService.deleteVenue(1L);

        verify(venueRepository).delete(testVenue);
    }

    @Test
    @DisplayName("Should update venue successfully")
    void updateVenue_withValidRequest_shouldReturnUpdatedVenue() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));
        when(venueRepository.existsByNameAndIdNot("Test Theater", 1L)).thenReturn(false);
        when(venueRepository.save(any(Venue.class))).thenReturn(testVenue);

        VenueResponse response = venueService.updateVenue(1L, testRequest);

        assertThat(response).isNotNull();
        verify(venueRepository).save(any(Venue.class));
    }
}
