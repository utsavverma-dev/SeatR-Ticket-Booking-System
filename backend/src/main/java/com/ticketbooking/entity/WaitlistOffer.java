package com.ticketbooking.entity;

import com.ticketbooking.entity.enums.WaitlistOfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "waitlist_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistOffer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "waitlist_id", nullable = false)
    private Waitlist waitlist;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "waitlist_offer_seats",
        joinColumns = @JoinColumn(name = "offer_id"),
        inverseJoinColumns = @JoinColumn(name = "show_seat_id")
    )
    @Builder.Default
    private List<ShowSeat> seats = new ArrayList<>();
    
    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistOfferStatus status;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public void addSeat(ShowSeat seat) {
        seats.add(seat);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
