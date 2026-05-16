package ru.msu.cmc.webprac.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "flight_id")
    @NonNull
    Flight flight;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id")
    @NonNull
    Client client;

    @Column(nullable = false, name = "price")
    @NonNull
    BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bonus_card_used")
    BonusCard bonusCardUsed;

    @Column(name = "bonus_amount_used")
    BigDecimal bonusAmountUsed;

    @Column(nullable = false, name = "is_paid_for")
    @NonNull
    Boolean isPaidFor;

    @Column(nullable = false, name = "booking_time")
    @NonNull
    Timestamp bookingTime;

    @Column(name = "payment_time")
    Timestamp paymentTime;

    public LocalDate getBookingLocalDate() {
        return bookingTime.toLocalDateTime().toLocalDate();
    }

    public LocalDate getPaymentLocalDate() {
        if (paymentTime == null) {
            return null;
        }
        return paymentTime.toLocalDateTime().toLocalDate();
    }

    public LocalTime getBookingLocalTime() {
        return bookingTime.toLocalDateTime().toLocalTime();
    }
    public LocalTime getPaymentLocalTime() {
        if (paymentTime == null) {
            return null;
        }
        return paymentTime.toLocalDateTime().toLocalTime();
    }
}
