package ru.msu.cmc.webprac.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "ticket")
public class Ticket implements BaseEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, name = "ticket_id")
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    @NonNull
    Flight flight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    @NonNull
    Client client;

    @Column(nullable = false, name = "price")
    @NonNull
    Double price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_card_used")
    BonusCard bonusCardUsed;

    @Column(name = "bonus_amount_used")
    Double bonusAmountUsed;

    @Column(nullable = false, name = "is_paid_for")
    @NonNull
    Boolean isPaidFor;

    @Column(nullable = false, name = "booking_time")
    @NonNull
    Timestamp bookingTime;

    @Column(name = "payment_time")
    Timestamp paymentTime;
}
