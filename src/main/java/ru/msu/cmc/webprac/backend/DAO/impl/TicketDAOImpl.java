package ru.msu.cmc.webprac.backend.DAO.impl;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprac.backend.DAO.TicketDAO;
import ru.msu.cmc.webprac.backend.entity.Airport;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Flight;
import ru.msu.cmc.webprac.backend.entity.Ticket;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TicketDAOImpl extends BaseDAOImpl<Ticket, Integer> implements TicketDAO {
    public TicketDAOImpl() {
        super(Ticket.class);
    }

    private List<Predicate> parseFilter(Filter filter, CriteriaQuery<Ticket> query, CriteriaBuilder builder) {
        Root<Ticket> queryRoot = query.from(Ticket.class);
        List<Predicate> result = new ArrayList<>();

        if (filter.getIsPaidFor() != null) {
            result.add(builder.equal(queryRoot.get("isPaidFor"), filter.getIsPaidFor()));
        }

        if (filter.getClientId() != null) {
            Join<Ticket, Client> clientJoin = queryRoot.join("client");
            result.add(builder.equal(clientJoin.get("id"), filter.getClientId()));
        }

        if (filter.getDepartureDate() != null
                || filter.getDepartureCity() != null
                || filter.getArrivalCity() != null) {
            Join<Ticket, Flight> flightJoin = queryRoot.join("flight");

            if (filter.getDepartureCity() != null) {
                Join<Ticket, Airport> airportJoin = flightJoin.join("departureAirport");
                result.add(buildCaseInsensitive(airportJoin.get("city"), builder, filter.getDepartureCity()));
            }

            if (filter.getArrivalCity() != null) {
                Join<Ticket, Airport> airportJoin = flightJoin.join("arrivalAirport");
                result.add(buildCaseInsensitive(airportJoin.get("city"), builder, filter.getArrivalCity()));
            }

            if (filter.getDepartureDate() != null) {
                result.add(buildDateEq(flightJoin.get("departureTime"), builder, filter.getDepartureDate()));
            }
        }

        query.select(queryRoot);

        return result;
    }

    @Override
    public List<Ticket> getTicketList(Filter filter) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Ticket> query = builder.createQuery(Ticket.class);
            List<Predicate> predicates = parseFilter(filter, query, builder);
            query.where(predicates);

            return session.createQuery(query).getResultList();
        }
    }
}
