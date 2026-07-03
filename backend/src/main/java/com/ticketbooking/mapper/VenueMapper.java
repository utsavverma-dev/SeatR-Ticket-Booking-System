package com.ticketbooking.mapper;

import com.ticketbooking.dto.response.SeatInfo;
import com.ticketbooking.dto.response.VenueResponse;
import com.ticketbooking.entity.Venue;
import com.ticketbooking.entity.VenueSeat;

import java.util.List;
import java.util.stream.Collectors;

public final class VenueMapper {

    private VenueMapper() {
    }

    public static VenueResponse toResponse(Venue venue) {
        List<SeatInfo> seatSummary = venue.getSeats().stream()
                .collect(Collectors.groupingBy(VenueSeat::getCategory, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> new SeatInfo(entry.getKey(), entry.getValue()))
                .toList();

        return new VenueResponse(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCity(),
                venue.getState(),
                venue.getCapacity(),
                seatSummary,
                venue.getCreatedAt()
        );
    }
}
