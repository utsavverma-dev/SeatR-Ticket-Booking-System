package com.ticketbooking.repository;

import com.ticketbooking.entity.EventPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventPriceRepository extends JpaRepository<EventPrice, Long> {

    List<EventPrice> findByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
