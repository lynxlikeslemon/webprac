package ru.msu.cmc.webprac.backend.DAO.impl;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.entity.Company;
import ru.msu.cmc.webprac.backend.entity.Flight;
import ru.msu.cmc.webprac.backend.entity.Ticket;
import ru.msu.cmc.webprac.backend.entity.Client;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ClientDAOImpl extends BaseDAOImpl<Client, Integer> implements ClientDAO {
    public ClientDAOImpl() {
        super(Client.class);
    }

    @Override
    public Client getByPhoneNumber(String phoneNumber) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Client> query = builder.createQuery(Client.class);
            Root<Client> queryRoot = query.from(Client.class);
            Predicate predicate = builder.equal(queryRoot.get("phoneNumber"), phoneNumber);
            query.select(queryRoot).where(predicate);

            return session.createQuery(query).getSingleResult();
        }
    }

    private List<Predicate> parseFilter(Filter filter, CriteriaQuery<Client> query, CriteriaBuilder builder) {
        Root<Client> queryRoot = query.from(Client.class);
        List<Predicate> result = new ArrayList<>();
        Path<String> firstName = queryRoot.get("firstName");
        Path<String> lastName = queryRoot.get("lastName");
        Path<String> fathersName = queryRoot.get("fathersName");
        Path<String> phone = queryRoot.get("phoneNumber");

        if (filter.getFlightId() != null
                || filter.getCompanyName() != null
                || filter.getTicketPaid() != null) {
            Root<Ticket> ticketRoot = query.from(Ticket.class);
            Join<Ticket, Client> clientJoin = ticketRoot.join("client");
            firstName = clientJoin.get("firstName");
            lastName = clientJoin.get("lastName");
            fathersName = clientJoin.get("fathersName");
            phone = clientJoin.get("phoneNumber");

            query.select(clientJoin).distinct(true);

            if (filter.getCompanyName() != null || filter.getFlightId() != null) {
                Join<Ticket, Flight> flightJoin = ticketRoot.join("flight");

                if (filter.getCompanyName() != null) {
                    Join<Flight, Company> companyJoin = flightJoin.join("company");
                    result.add(buildCaseInsensitive(companyJoin.get("name"), builder, filter.getCompanyName()));
                }

                if (filter.getFlightId() != null) {
                    result.add(buildCaseInsensitive(flightJoin.get("id"), builder, filter.getFlightId()));
                }
            }

            if (filter.getTicketPaid() != null) {
                result.add(builder.equal(ticketRoot.get("isPaidFor"), filter.getTicketPaid()));
            }
        } else {
            query.select(queryRoot);
        }

        if (filter.getFirstName() != null) {
            result.add(buildCaseInsensitive(firstName, builder, filter.getFirstName()));
        }

        if (filter.getLastName() != null) {
            result.add(buildCaseInsensitive(lastName, builder, filter.getLastName()));
        }

        if (filter.getFathersName() != null) {
            result.add(buildCaseInsensitive(fathersName, builder, filter.getFathersName()));
        }

        if (filter.getPhoneNumber() != null) {
            result.add(builder.equal(phone, filter.getPhoneNumber()));
        }

        return result;
    }

    @Override
    public List<Client> getClientList(Filter filter) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Client> query = builder.createQuery(Client.class);
            List<Predicate> predicates = parseFilter(filter, query, builder);
            query.where(predicates);

            return session.createQuery(query).getResultList();
        }
    }
}
