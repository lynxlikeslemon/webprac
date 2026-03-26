package ru.msu.cmc.webprac.backend.DAO.impl;

import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.entity.*;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FlightDAOImpl extends BaseDAOImpl<Flight, String> implements FlightDAO {
    public FlightDAOImpl() {
        super(Flight.class);
    }


    private List<Predicate> parseFilter(Filter filter, CriteriaQuery<Flight> query, CriteriaBuilder builder) {
        Root<Flight> queryRoot = query.from(Flight.class);
        List<Predicate> result = new ArrayList<>();

        if (filter.getArrivalDate() != null) {
            result.add(buildDateEq(queryRoot.get("arrivalTime"), builder, filter.getArrivalDate()));
        }

        if (filter.getDepartureDate() != null) {
            result.add(buildDateEq(queryRoot.get("departureTime"), builder, filter.getDepartureDate()));
        }

        if (filter.getMaxPrice() != null) {
            result.add(builder.lessThanOrEqualTo(queryRoot.get("price"), filter.getMaxPrice()));
        }

        if (filter.getPurchasable() != null) {
            result.add(builder.lessThan(queryRoot.get("placesTaken"), queryRoot.get("placesTotal")));
        }

        if (filter.getDepartureCity() != null) {
            Join<Flight, Airport> airportJoin = queryRoot.join("departureAirport");
            result.add(buildCaseInsensitive(airportJoin.get("city"), builder, filter.getDepartureCity()));
        }

        if (filter.getArrivalCity() != null) {
            Join<Flight, Airport> airportJoin = queryRoot.join("arrivalAirport");
            result.add(buildCaseInsensitive(airportJoin.get("city"), builder, filter.getArrivalCity()));
        }

        if (filter.getCompanyName() != null) {
            Join<Flight, Company> companyJoin = queryRoot.join("company");
            result.add(buildCaseInsensitive(companyJoin.get("name"), builder, filter.getCompanyName()));
        }

        query.select(queryRoot);

        return result;
    }

    @Override
    public List<Flight> getFlightList(Filter filter) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Flight> query = builder.createQuery(Flight.class);
            List<Predicate> predicates = parseFilter(filter, query, builder);
            query.where(predicates);

            return session.createQuery(query).getResultList();
        }
    }
}
