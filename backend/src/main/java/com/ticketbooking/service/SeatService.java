package com.ticketbooking.service;

import com.ticketbooking.dto.SeatHoldRequest;
import com.ticketbooking.dto.SeatHoldResponse;
import com.ticketbooking.dto.ShowSeatResponse;

import java.util.List;

public interface SeatService {

    List<ShowSeatResponse> getSeatsByEventId(Long eventId);

    SeatHoldResponse holdSeats(SeatHoldRequest request, String customerEmail);

    void releaseSeats(SeatHoldRequest request, String customerEmail);
    
    void releaseExpiredHolds();
}
