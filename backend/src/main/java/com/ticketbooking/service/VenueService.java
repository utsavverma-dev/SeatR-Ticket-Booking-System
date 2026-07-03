package com.ticketbooking.service;

import com.ticketbooking.dto.request.VenueRequest;
import com.ticketbooking.dto.response.VenueResponse;

import java.util.List;

public interface VenueService {

    VenueResponse createVenue(VenueRequest request);

    VenueResponse updateVenue(Long id, VenueRequest request);

    void deleteVenue(Long id);

    VenueResponse getVenue(Long id);

    List<VenueResponse> getAllVenues();
}
