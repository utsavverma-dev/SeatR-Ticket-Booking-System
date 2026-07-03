package com.ticketbooking.repository;

import com.ticketbooking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.event e JOIN FETCH e.venue JOIN FETCH b.customer c WHERE c.id = :customerId ORDER BY b.bookingTime DESC")
    List<Booking> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    List<Booking> findByEventId(Long eventId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.event e JOIN FETCH e.venue JOIN FETCH b.customer c JOIN FETCH b.seats s JOIN FETCH s.showSeat ss JOIN FETCH ss.venueSeat WHERE b.id = :id AND c.id = :customerId")
    Optional<Booking> findByIdAndCustomerIdWithDetails(@Param("id") Long id, @Param("customerId") Long customerId);
}
