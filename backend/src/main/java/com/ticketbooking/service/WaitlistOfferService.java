package com.ticketbooking.service;

public interface WaitlistOfferService {
    void generateOfferForReleasedSeats(Long eventId, com.ticketbooking.entity.enums.SeatCategory category, java.util.List<Long> seatIds);
    com.ticketbooking.dto.BookingResponse acceptOffer(String token, String email);
    void processExpiredOffers();
}
