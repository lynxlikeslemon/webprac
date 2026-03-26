package ru.msu.cmc.webprac.backend.DAO.impl;

import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprac.backend.DAO.AirportDAO;
import ru.msu.cmc.webprac.backend.entity.Airport;

import java.util.Collection;
import java.util.List;

@Repository
public class AirportDAOImpl extends BaseDAOImpl<Airport, String> implements AirportDAO {
    public AirportDAOImpl() {
        super(Airport.class);
    }
}
