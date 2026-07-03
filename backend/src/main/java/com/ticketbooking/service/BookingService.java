package com.ticketbooking.service;

import com.ticketbooking.dto.BookingDetailsResponse;
import com.ticketbooking.dto.BookingHistoryResponse;
import com.ticketbooking.dto.BookingRequest;
import com.ticketbooking.dto.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request, String customerEmail);

    void cancelBooking(Long id, String customerEmail);

    List<BookingHistoryResponse> getCustomerBookings(String customerEmail);

    BookingDetailsResponse getBookingDetails(Long id, String customerEmail);
}
