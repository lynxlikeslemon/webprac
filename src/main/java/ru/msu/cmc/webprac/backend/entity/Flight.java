package ru.msu.cmc.webprac.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flight")
public class Flight implements BaseEntity<String> {
    @Id
    @Column(nullable = false, name = "flight_id")
    @NonNull
    String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    @NonNull
    Company company;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departure_airport_id")
    @NonNull
    Airport departureAirport;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrival_airport_id")
    @NonNull
    Airport arrivalAirport;

    @Column(nullable = false, name = "departure_time")
    @NonNull
    Timestamp departureTime;

    @Column(nullable = false, name = "arrival_time")
    @NonNull
    Timestamp arrivalTime;

    @Column(nullable = false, name = "price")
    @NonNull
    BigDecimal price;

    @Column(nullable = false, name = "places_total")
    @NonNull
    Integer placesTotal;

    @Column(nullable = false, name = "places_taken")
    @NonNull
    Integer placesTaken;

    public LocalDate getDepartureLocalDate() {
        return departureTime.toLocalDateTime().toLocalDate();
    }

    public LocalDate getArrivalLocalDate() {
        return arrivalTime.toLocalDateTime().toLocalDate();
    }

    public LocalTime getDepartureLocalTime() {
        return departureTime.toLocalDateTime().toLocalTime();
    }

    public LocalTime getArrivalLocalTime() {
        return arrivalTime.toLocalDateTime().toLocalTime();
    }

    public String getDuration() {
        Duration duration = Duration.between(departureTime.toLocalDateTime(), arrivalTime.toLocalDateTime());
        long sec = duration.getSeconds();
        return String.format("%d:%02d:%02d", sec / 3600, (sec % 3600) / 60, (sec % 60));
    }
}
