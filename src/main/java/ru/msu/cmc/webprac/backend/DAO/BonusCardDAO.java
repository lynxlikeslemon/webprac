package ru.msu.cmc.webprac.backend.DAO;

import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Client;

import java.util.List;

public interface BonusCardDAO extends BaseDAO<BonusCard, Integer> {
    List<BonusCard> getBonusCardsByClientId(Integer clientId);
}
