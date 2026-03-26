package ru.msu.cmc.webprac.backend.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Company implements BaseEntity<Integer> {
    @Id
    @Column(nullable = false, name = "company_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    Integer id;

    @Column(nullable = false, name = "company_name")
    @NonNull
    String name;
}
