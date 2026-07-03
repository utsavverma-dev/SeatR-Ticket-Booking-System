package com.ticketbooking.service.impl;

import com.ticketbooking.dto.request.SeatRequest;
import com.ticketbooking.dto.request.VenueRequest;
import com.ticketbooking.dto.response.VenueResponse;
import com.ticketbooking.entity.Venue;
import com.ticketbooking.entity.VenueSeat;
import com.ticketbooking.exception.DuplicateResourceException;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.mapper.VenueMapper;
import com.ticketbooking.repository.VenueRepository;
import com.ticketbooking.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);

    private final VenueRepository venueRepository;

    @Override
    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        if (venueRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Venue", "name", request.name());
        }

        Venue venue = Venue.builder()
                .name(request.name())
                .address(request.address())
                .city(request.city())
                .state(request.state())
                .capacity(0)
                .build();

        List<VenueSeat> seats = generateSeats(request.seats(), venue);
        venue.setSeats(seats);
        venue.setCapacity(seats.size());

        Venue savedVenue = venueRepository.save(venue);
        log.info("Venue created: {} (ID: {})", savedVenue.getName(), savedVenue.getId());

        return VenueMapper.toResponse(savedVenue);
    }

    @Override
    @Transactional
    public VenueResponse updateVenue(Long id, VenueRequest request) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        if (venueRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateResourceException("Venue", "name", request.name());
        }

        venue.setName(request.name());
        venue.setAddress(request.address());
        venue.setCity(request.city());
        venue.setState(request.state());

        // Clear existing seats and regenerate
        venue.getSeats().clear();
        List<VenueSeat> newSeats = generateSeats(request.seats(), venue);
        venue.getSeats().addAll(newSeats);
        venue.setCapacity(newSeats.size());

        Venue updatedVenue = venueRepository.save(venue);
        log.info("Venue updated: {} (ID: {})", updatedVenue.getName(), updatedVenue.getId());

        return VenueMapper.toResponse(updatedVenue);
    }

    @Override
    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        venueRepository.delete(venue);
        log.info("Venue deleted: {} (ID: {})", venue.getName(), id);
    }

    @Override
    @Transactional(readOnly = true)
    public VenueResponse getVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue", "id", id));

        return VenueMapper.toResponse(venue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(VenueMapper::toResponse)
                .toList();
    }

    private List<VenueSeat> generateSeats(List<SeatRequest> seatRequests, Venue venue) {
        List<VenueSeat> seats = new ArrayList<>();

        for (SeatRequest seatRequest : seatRequests) {
            for (int i = 1; i <= seatRequest.seatCount(); i++) {
                String seatLabel = seatRequest.rowPrefix() + i;
                VenueSeat seat = VenueSeat.builder()
                        .venue(venue)
                        .category(seatRequest.category())
                        .seatLabel(seatLabel)
                        .rowNumber(seatRequest.rowPrefix())
                        .seatNumber(i)
                        .build();
                seats.add(seat);
            }
        }

        return seats;
    }
}
