package ru.msu.cmc.webprac.DAOtests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.DAO.FlightDAO;
import ru.msu.cmc.webprac.backend.entity.Airport;
import ru.msu.cmc.webprac.backend.entity.Company;
import ru.msu.cmc.webprac.backend.entity.Flight;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class FlightDAOTest {
    @Autowired
    private FlightDAO flightDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private final Company company1 = new Company("Компания");
    private final Company company2 = new Company("Кампания");

    private final Airport airport1 = new Airport("ABC", "airport1", "city1");
    private final Airport airport2 = new Airport("DEF", "airport2", "city1");
    private final Airport airport3 = new Airport("GEH", "airport3", "city2");

    private final LocalDate day1 = LocalDate.of(2026, 6, 6);
    private final LocalDate day2 = LocalDate.of(2027, 7, 7);
    private final LocalDate day3 = LocalDate.of(2028, 8, 8);

    private final Timestamp time1 = new Timestamp(day1.toEpochSecond(LocalTime.NOON, ZoneOffset.UTC));
    private final Timestamp time2 = new Timestamp(day2.toEpochSecond(LocalTime.NOON, ZoneOffset.UTC));
    private final Timestamp time3 = new Timestamp(day3.toEpochSecond(LocalTime.NOON, ZoneOffset.UTC));

    private final Flight flight1 = new Flight(
            "FL0000",
            company1,
            airport3,
            airport1,
            time1,
            time2,
            1024.0,
            25,
            24);

    private final Flight flight2 = new Flight(
            "FL0001",
            company2,
            airport3,
            airport2,
            time1,
            time2,
            2048.0,
            25,
            25);

    private final Flight flight3 = new Flight(
            "FL0002",
            company1,
            airport3,
            airport1,
            time2,
            time3,
            1024.0,
            25,
            25);

    private final Flight flight4 = new Flight(
            "FL0003",
            company2,
            airport2,
            airport3,
            time2,
            time3,
            2048.0,
            25,
            23);

    @BeforeEach
    void fillDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(airport1);
            session.persist(airport2);
            session.persist(airport3);
            session.persist(company1);
            session.persist(company2);
            session.persist(flight1);
            session.persist(flight2);
            session.persist(flight3);
            session.persist(flight4);
            session.getTransaction().commit();
        }
    }

    @AfterEach
    @BeforeAll
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM flight").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM company").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM airport").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void filterFindTest() {
        Set<String> city1DepartureFlights = Set.of(flight4.getId());
        Set<String> city1ArrivalFlights = Set.of(flight1.getId(), flight2.getId(), flight3.getId());
        Set<String> city2DepartureFlights = Set.of(flight1.getId(), flight2.getId(), flight3.getId());
        Set<String> city2ArrivalFlights = Set.of(flight4.getId());
        Set<String> departure2026Flights = Set.of(flight1.getId(), flight2.getId());
        Set<String> arrival2028Flights = Set.of(flight3.getId(), flight4.getId());
        Set<String> max1024PurchasableFlights = Set.of(flight1.getId());
        Set<String> company1Flights = Set.of(flight1.getId(), flight3.getId());
        Set<String> fullMatch = Set.of(flight1.getId());
        Set<String> FromCity1ToCity2PurchasableMax1024Flights = Set.of();

        FlightDAO.Filter city1DepartureFlightsFilter = FlightDAO.getFilterBuilder().departureCity("city1").build();
        FlightDAO.Filter city2DepartureFlightsFilter = FlightDAO.getFilterBuilder().departureCity("city2").build();
        FlightDAO.Filter city1ArrivalFlightsFilter = FlightDAO.getFilterBuilder().arrivalCity("city1").build();
        FlightDAO.Filter city2ArrivalFlightsFilter = FlightDAO.getFilterBuilder().arrivalCity("city2").build();
        FlightDAO.Filter departure2026FlightsFilter = FlightDAO.getFilterBuilder().departureDate(day1).build();
        FlightDAO.Filter arrival2028FlightsFilter = FlightDAO.getFilterBuilder().arrivalDate(day3).build();
        FlightDAO.Filter company1FlightsFilter = FlightDAO.getFilterBuilder().companyName(company1.getName()).build();
        FlightDAO.Filter max1024PurchasableFlightsFilter = FlightDAO.getFilterBuilder().maxPrice(1024.0).purchasable(true).build();
        FlightDAO.Filter fullMatchFilter = FlightDAO.getFilterBuilder()
                .maxPrice(flight1.getPrice())
                .departureDate(day1)
                .departureCity(flight1.getDepartureAirport().getCity())
                .arrivalDate(day2)
                .arrivalCity(flight1.getArrivalAirport().getCity())
                .purchasable(flight1.getPlacesTaken() < flight1.getPlacesTotal())
                .companyName(flight1.getCompany().getName())
                .build();

        FlightDAO.Filter FromCity1ToCity2PurchasableMax1024FlightsFilter = FlightDAO.getFilterBuilder()
                .departureCity("city1")
                .arrivalCity("city2")
                .maxPrice(1024.0)
                .purchasable(true)
                .build();

        Map<FlightDAO.Filter, Set<String>> correctResults = new HashMap<>(Map.of(
                city1DepartureFlightsFilter, city1DepartureFlights,
                city2DepartureFlightsFilter, city2DepartureFlights,
                city1ArrivalFlightsFilter, city1ArrivalFlights,
                city2ArrivalFlightsFilter, city2ArrivalFlights,
                departure2026FlightsFilter, departure2026Flights,
                arrival2028FlightsFilter, arrival2028Flights,
                max1024PurchasableFlightsFilter, max1024PurchasableFlights,
                company1FlightsFilter, company1Flights,
                fullMatchFilter, fullMatch,
                FromCity1ToCity2PurchasableMax1024FlightsFilter, FromCity1ToCity2PurchasableMax1024Flights
        ));

        for (Map.Entry<FlightDAO.Filter, Set<String>> entry : correctResults.entrySet()) {
            Set<String> correct = entry.getValue();
            FlightDAO.Filter filter = entry.getKey();
            Set<String> result = flightDAO.getFlightList(filter).stream().map(Flight::getId).collect(Collectors.toSet());
            Assertions.assertEquals(correct, result, String.format("Failure for filter:\n" + filter.toString() + "\n"));
        }
    }
}
