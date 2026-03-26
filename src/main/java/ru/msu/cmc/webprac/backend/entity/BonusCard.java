package ru.msu.cmc.webprac.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bonus_card")
public class BonusCard implements BaseEntity<Integer> {
    @Id
    @Column(nullable = false, name = "bonus_id")
    @NonNull
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "company_id")
    @NonNull
    Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "client_id")
    @NonNull
    Client client;

    @Column(nullable = false, name = "amount")
    @NonNull
    Double amount;
}
