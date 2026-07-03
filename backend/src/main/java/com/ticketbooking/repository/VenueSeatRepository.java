package com.ticketbooking.repository;

import com.ticketbooking.entity.VenueSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueSeatRepository extends JpaRepository<VenueSeat, Long> {

    List<VenueSeat> findByVenueId(Long venueId);

    void deleteByVenueId(Long venueId);
}
