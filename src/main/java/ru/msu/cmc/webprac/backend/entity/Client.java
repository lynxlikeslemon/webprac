package ru.msu.cmc.webprac.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Client implements BaseEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, name = "client_id")
    Integer id;

    @Column(nullable = false, name = "first_name")
    @NonNull
    String firstName;

    @Column(nullable = false, name = "last_name")
    @NonNull
    String lastName;

    @Column(nullable = false, name = "fathers_name")
    @NonNull
    String fathersName;

    @Column(nullable = false, name = "phone_number")
    @NonNull
    String phoneNumber;

    @Column(name = "email")
    String email;

    @Column(name = "address")
    String address;
}
