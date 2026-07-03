package com.ticketbooking.repository;

import com.ticketbooking.entity.Waitlist;
import com.ticketbooking.entity.enums.SeatCategory;
import com.ticketbooking.entity.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    boolean existsByEventIdAndUserIdAndSeatCategory(Long eventId, Long userId, SeatCategory category);

    @Query("SELECT w FROM Waitlist w WHERE w.event.id = :eventId AND w.seatCategory = :category AND w.status = 'WAITING' ORDER BY w.positionInQueue ASC LIMIT 1")
    Optional<Waitlist> findFirstWaitingCustomer(Long eventId, SeatCategory category);

    List<Waitlist> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<Waitlist> findByEventId(Long eventId);
    
    @Query("SELECT COALESCE(MAX(w.positionInQueue), 0) FROM Waitlist w WHERE w.event.id = :eventId AND w.seatCategory = :category")
    Integer findMaxPosition(Long eventId, SeatCategory category);
}
