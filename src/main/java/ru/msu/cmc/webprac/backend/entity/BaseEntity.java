package ru.msu.cmc.webprac.backend.entity;

public interface BaseEntity<IDType> {
    IDType getId();
    void setId(IDType id);
}
