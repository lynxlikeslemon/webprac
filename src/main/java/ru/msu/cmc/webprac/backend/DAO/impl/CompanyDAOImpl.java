package ru.msu.cmc.webprac.backend.DAO.impl;

import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprac.backend.DAO.CompanyDAO;
import ru.msu.cmc.webprac.backend.entity.Company;

@Repository
public class CompanyDAOImpl extends BaseDAOImpl<Company, Integer> implements CompanyDAO {
    public CompanyDAOImpl() {
        super(Company.class);
    }
}
