package com.ticketbooking.repository;

import com.ticketbooking.entity.WaitlistOffer;
import com.ticketbooking.entity.enums.WaitlistOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistOfferRepository extends JpaRepository<WaitlistOffer, Long> {

    Optional<WaitlistOffer> findByToken(String token);

    List<WaitlistOffer> findByStatusAndExpiryTimeBefore(WaitlistOfferStatus status, LocalDateTime now);
}
