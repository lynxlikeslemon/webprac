package ru.msu.cmc.webprac.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    @NonNull
    Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_airport_id")
    @NonNull
    Airport departureAirport;

    @ManyToOne(fetch = FetchType.LAZY)
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
    Double price;

    @Column(nullable = false, name = "places_total")
    @NonNull
    Integer placesTotal;

    @Column(nullable = false, name = "places_taken")
    @NonNull
    Integer placesTaken;
}
