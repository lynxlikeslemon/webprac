package ru.msu.cmc.webprac.backend.entity;

import lombok.*;
import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "airport")
public class Airport implements BaseEntity<String> {
    @Id
    @Column(nullable = false, name = "airport_id")
    @NonNull
    private String id;

    @Column(nullable = false, name = "airport_name")
    @NonNull
    private String name;

    @Column(nullable = false, name = "city")
    @NonNull
    private String city;
}
