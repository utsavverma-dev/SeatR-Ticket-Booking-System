package com.ticketbooking.service.impl;

import com.ticketbooking.dto.response.BookingStatsResponse;
import com.ticketbooking.dto.response.RevenueResponse;
import com.ticketbooking.dto.response.UpcomingEventResponse;
import com.ticketbooking.entity.Event;
import com.ticketbooking.entity.User;
import com.ticketbooking.entity.enums.BookingStatus;
import com.ticketbooking.entity.enums.RoleName;
import com.ticketbooking.exception.ResourceNotFoundException;
import com.ticketbooking.repository.BookingRepository;
import com.ticketbooking.repository.EventRepository;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.repository.WaitlistRepository;
import com.ticketbooking.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final WaitlistRepository waitlistRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RevenueResponse> getRevenuePerEvent(String email) {
        User user = getUser(email);
        List<Event> events = getEventsForUser(user);
        
        List<RevenueResponse> responses = new ArrayList<>();
        for (Event event : events) {
            BigDecimal totalRevenue = bookingRepository.findByEventId(event.getId()).stream()
                    .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                    .map(b -> b.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            responses.add(new RevenueResponse(event.getId(), event.getTitle(), totalRevenue));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatsResponse> getBookingStatsPerEvent(String email) {
        User user = getUser(email);
        List<Event> events = getEventsForUser(user);
        
        List<BookingStatsResponse> responses = new ArrayList<>();
        for (Event event : events) {
            var bookings = bookingRepository.findByEventId(event.getId());
            int totalBookings = 0;
            int totalCancellations = 0;
            int seatsSold = 0;

            for (var booking : bookings) {
                if (booking.getStatus() == BookingStatus.CONFIRMED) {
                    totalBookings++;
                    seatsSold += booking.getSeats().size();
                } else if (booking.getStatus() == BookingStatus.CANCELLED) {
                    totalCancellations++;
                }
            }

            int waitlistCount = waitlistRepository.findByEventId(event.getId()).size();
            
            int capacity = event.getVenue() != null && event.getVenue().getCapacity() > 0 ? event.getVenue().getCapacity() : 1;
            double occupancy = (double) seatsSold / capacity * 100.0;
            // Cap at 100% just in case
            occupancy = Math.min(occupancy, 100.0);

            responses.add(new BookingStatsResponse(
                    event.getId(),
                    event.getTitle(),
                    totalBookings,
                    totalCancellations,
                    seatsSold,
                    waitlistCount,
                    occupancy
            ));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueResponse getOverallRevenue(String email) {
        List<RevenueResponse> eventRevenues = getRevenuePerEvent(email);
        BigDecimal total = eventRevenues.stream()
                .map(RevenueResponse::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RevenueResponse(null, "Total Overall", total);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingStatsResponse getOverallBookingStats(String email) {
        List<BookingStatsResponse> stats = getBookingStatsPerEvent(email);
        int totalBookings = stats.stream().mapToInt(BookingStatsResponse::totalBookings).sum();
        int totalCancellations = stats.stream().mapToInt(BookingStatsResponse::totalCancellations).sum();
        int seatsSold = stats.stream().mapToInt(BookingStatsResponse::seatsSold).sum();
        int waitlistCount = stats.stream().mapToInt(BookingStatsResponse::waitlistCount).sum();
        
        double avgOccupancy = stats.isEmpty() ? 0.0 : stats.stream().mapToDouble(BookingStatsResponse::seatOccupancyPercentage).average().orElse(0.0);

        return new BookingStatsResponse(null, "Total Overall", totalBookings, totalCancellations, seatsSold, waitlistCount, avgOccupancy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UpcomingEventResponse> getUpcomingEvents(String email) {
        User user = getUser(email);
        List<Event> events = getEventsForUser(user);
        
        LocalDateTime now = LocalDateTime.now();
        
        return events.stream()
            .filter(e -> {
                LocalDateTime eventDT = LocalDateTime.of(e.getEventDate(), e.getEventTime());
                return eventDT.isAfter(now);
            })
            .sorted(Comparator.comparing(e -> LocalDateTime.of(e.getEventDate(), e.getEventTime())))
            .limit(5)
            .map(e -> new UpcomingEventResponse(
                e.getId(), 
                e.getTitle(), 
                LocalDateTime.of(e.getEventDate(), e.getEventTime()), 
                e.getVenue().getName()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingStatsResponse> getPopularEvents(String email) {
        List<BookingStatsResponse> allStats = getBookingStatsPerEvent(email);
        
        return allStats.stream()
            .sorted(Comparator.comparing(BookingStatsResponse::totalBookings).reversed())
            .limit(5)
            .collect(Collectors.toList());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private List<Event> getEventsForUser(User user) {
        if (user.getRole().getName() == RoleName.ADMIN) {
            return eventRepository.findAll();
        } else {
            return eventRepository.findByOrganiserId(user.getId());
        }
    }
}
