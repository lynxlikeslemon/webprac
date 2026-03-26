package ru.msu.cmc.webprac.backend.DAO;

import ru.msu.cmc.webprac.backend.entity.BaseEntity;

import java.util.Collection;

public interface BaseDAO<T extends BaseEntity<IDType>, IDType> {
    T getById(IDType id);
    Collection<T> getAll();

    void save(T entity);
    void saveAll(Collection<T> entities);

    void delete(T entity);
    void deleteById(IDType id);

    void update(T entity);
}
