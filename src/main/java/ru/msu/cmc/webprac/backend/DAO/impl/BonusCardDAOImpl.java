package ru.msu.cmc.webprac.backend.DAO.impl;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprac.backend.DAO.BonusCardDAO;
import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Client;

import java.util.List;

@Repository
public class BonusCardDAOImpl extends BaseDAOImpl<BonusCard, Integer> implements BonusCardDAO {
    public BonusCardDAOImpl() {
        super(BonusCard.class);
    }

    @Override
    public List<BonusCard> getBonusCardsByClientId(Integer clientId) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<BonusCard> query = builder.createQuery(BonusCard.class);
            Root<BonusCard> queryRoot = query.from(BonusCard.class);
            Join<BonusCard, Client> clientJoin = queryRoot.join("client");
            Predicate predicate = builder.equal(clientJoin.get("id"), clientId);
            query.select(queryRoot).where(predicate);

            return session.createQuery(query).getResultList();
        }
    }
}
