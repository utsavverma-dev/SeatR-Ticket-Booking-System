package com.ticketbooking.mapper;

import com.ticketbooking.dto.BookingDetailsResponse;
import com.ticketbooking.dto.BookingHistoryResponse;
import com.ticketbooking.dto.BookingResponse;
import com.ticketbooking.entity.Booking;
import com.ticketbooking.entity.BookingSeat;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        if (booking == null) return null;

        List<String> bookedSeats = booking.getSeats().stream()
                .map(bs -> bs.getShowSeat().getVenueSeat().getSeatLabel())
                .collect(Collectors.toList());

        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getEvent().getId(),
                booking.getEvent().getTitle(),
                booking.getBookingTime(),
                booking.getStatus().name(),
                booking.getTotalAmount(),
                bookedSeats
        );
    }

    public BookingHistoryResponse toHistoryResponse(Booking booking) {
        if (booking == null) return null;
        
        LocalDateTime eventDate = LocalDateTime.of(
            booking.getEvent().getEventDate(),
            booking.getEvent().getEventTime()
        );

        return new BookingHistoryResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getEvent().getTitle(),
                booking.getEvent().getVenue().getName(),
                eventDate,
                booking.getBookingTime(),
                booking.getStatus().name(),
                booking.getTotalAmount()
        );
    }

    public BookingDetailsResponse toDetailsResponse(Booking booking) {
        if (booking == null) return null;

        LocalDateTime eventDate = LocalDateTime.of(
            booking.getEvent().getEventDate(),
            booking.getEvent().getEventTime()
        );

        List<BookingDetailsResponse.BookingSeatInfo> seatInfos = booking.getSeats().stream()
                .map(bs -> new BookingDetailsResponse.BookingSeatInfo(
                        bs.getShowSeat().getVenueSeat().getSeatLabel(),
                        bs.getShowSeat().getVenueSeat().getCategory().name(),
                        bs.getPrice()
                ))
                .collect(Collectors.toList());

        return new BookingDetailsResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName(),
                booking.getCustomer().getEmail(),
                booking.getEvent().getTitle(),
                booking.getEvent().getVenue().getName(),
                eventDate,
                booking.getBookingTime(),
                booking.getStatus().name(),
                booking.getTotalAmount(),
                seatInfos
        );
    }
}
