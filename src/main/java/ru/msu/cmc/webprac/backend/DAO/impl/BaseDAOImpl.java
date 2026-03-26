package ru.msu.cmc.webprac.backend.DAO.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.stereotype.Repository;
import org.hibernate.SessionFactory;
import ru.msu.cmc.webprac.backend.DAO.BaseDAO;
import ru.msu.cmc.webprac.backend.entity.BaseEntity;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

@Repository
public abstract class BaseDAOImpl<T extends BaseEntity<IDType>, IDType> implements BaseDAO<T, IDType> {
    protected SessionFactory sessionFactory;
    private final Class<T> entityClass;

    public BaseDAOImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Autowired
    public void setSessionFactory(LocalSessionFactoryBean sessionFactory) {
        this.sessionFactory = sessionFactory.getObject();
    }

    @Override
    public T getById(IDType id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(entityClass, id);
        }
    }

    @Override
    public Collection<T> getAll() {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<T> query = builder.createQuery(entityClass);
            query.from(entityClass);
            return session.createQuery(query).getResultList();
        }
    }

    @Override
    public void save(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public void saveAll(Collection<T> entities) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            entities.forEach(session::persist);
            session.getTransaction().commit();
        }
    }

    @Override
    public void delete(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.remove(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public void deleteById(IDType id) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            T entity = getById(id);
            session.remove(entity);
            session.getTransaction().commit();
        }
    }

    @Override
    public void update(T entity) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.merge(entity);
            session.getTransaction().commit();
        }
    }

    protected Predicate buildCaseInsensitive(Path<String> path, CriteriaBuilder builder, String arg) {
        String lowerCase = arg.toLowerCase();
        return builder.equal(builder.lower(path), lowerCase);
    }


    protected Predicate buildDateEq(Path<Timestamp> path, CriteriaBuilder builder, LocalDate date) {
        LocalTime startOfDay = LocalTime.MIN;
        LocalTime endOfDay = LocalTime.MAX;
        long startOfDate = date.toEpochSecond(startOfDay, ZoneOffset.UTC);
        long endOfDate = date.toEpochSecond(endOfDay, ZoneOffset.UTC);
        return builder.between(path, new Timestamp(startOfDate), new Timestamp(endOfDate));
    }
}
