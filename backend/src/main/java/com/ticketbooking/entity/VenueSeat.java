package com.ticketbooking.entity;

import com.ticketbooking.entity.enums.SeatCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue_seats",
       uniqueConstraints = @UniqueConstraint(columnNames = {"venue_id", "seat_label"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SeatCategory category;

    @Column(name = "seat_label", nullable = false, length = 10)
    private String seatLabel;

    @Column(name = "row_number", nullable = false, length = 5)
    private String rowNumber;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;
}
