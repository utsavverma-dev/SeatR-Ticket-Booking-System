package com.ticketbooking.repository;

import com.ticketbooking.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Query("SELECT s FROM ShowSeat s JOIN FETCH s.venueSeat vs JOIN FETCH s.event e WHERE s.event.id = :eventId")
    List<ShowSeat> findByEventIdWithDetails(@Param("eventId") Long eventId);

    @Query("SELECT s FROM ShowSeat s WHERE s.event.id = :eventId AND s.id IN :seatIds")
    List<ShowSeat> findByIdsAndEventId(@Param("seatIds") List<Long> seatIds, @Param("eventId") Long eventId);

    List<ShowSeat> findByStatusAndHoldExpiresAtBefore(com.ticketbooking.entity.enums.SeatStatus status, LocalDateTime now);
}
