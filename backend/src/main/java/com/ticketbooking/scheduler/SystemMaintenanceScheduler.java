package com.ticketbooking.scheduler;

import com.ticketbooking.service.SeatService;
import com.ticketbooking.service.WaitlistOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMaintenanceScheduler {

    private final SeatService seatService;
    private final WaitlistOfferService waitlistOfferService;

    @Scheduled(fixedDelay = 60000)
    public void runSystemMaintenance() {
        log.info("Running System Maintenance Scheduler...");
        
        // 1. Process expired waitlist offers
        waitlistOfferService.processExpiredOffers();
        
        // 2. Release expired generic holds (like from initial seat selection)
        seatService.releaseExpiredHolds();
        
        log.info("System Maintenance complete.");
    }
}
