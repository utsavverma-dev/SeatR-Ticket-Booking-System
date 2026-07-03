package com.ticketbooking.mapper;

import com.ticketbooking.dto.ShowSeatResponse;
import com.ticketbooking.entity.EventPrice;
import com.ticketbooking.entity.ShowSeat;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class SeatMapper {

    public ShowSeatResponse toResponse(ShowSeat showSeat, List<EventPrice> eventPrices) {
        if (showSeat == null) return null;
        
        String category = showSeat.getVenueSeat().getCategory().name();
        BigDecimal price = eventPrices.stream()
                .filter(ep -> ep.getCategory().name().equals(category))
                .map(EventPrice::getPrice)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        return new ShowSeatResponse(
                showSeat.getId(),
                showSeat.getEvent().getId(),
                showSeat.getVenueSeat().getSeatLabel(),
                showSeat.getVenueSeat().getRowNumber(),
                showSeat.getVenueSeat().getSeatNumber(),
                category,
                price,
                showSeat.getStatus().name()
        );
    }
}
