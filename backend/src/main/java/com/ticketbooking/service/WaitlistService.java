package com.ticketbooking.service;

import com.ticketbooking.dto.request.WaitlistRequest;
import com.ticketbooking.dto.response.WaitlistResponse;

import java.util.List;

public interface WaitlistService {
    WaitlistResponse joinWaitlist(WaitlistRequest request, String email);
    List<WaitlistResponse> getUserWaitlist(String email);
    void leaveWaitlist(Long waitlistId, String email);
}
