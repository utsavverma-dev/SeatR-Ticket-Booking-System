package com.ticketbooking.repository;

import com.ticketbooking.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganiserId(Long organiserId);

    @Query("SELECT e FROM Event e JOIN FETCH e.venue JOIN FETCH e.organiser JOIN FETCH e.prices WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(Long id);

    @Query("SELECT e FROM Event e JOIN FETCH e.venue JOIN FETCH e.organiser")
    List<Event> findAllWithDetails();
}
