package ru.msu.cmc.webprac.DAOtests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.DAO.TicketDAO;
import ru.msu.cmc.webprac.backend.entity.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class TicketDAOTest {
    @Autowired
    private TicketDAO ticketDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private final Client client1 = new Client("Александр", "Александров", "Александрович", "+7(777)777-77-77");
    private final Client client2 = new Client("Борис", "Борисов", "Борисович", "+8(888)888-88-88");

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
            airport2,
            airport3,
            time2,
            time3,
            1024.0,
            25,
            25);


    private final Ticket ticket1 = new Ticket(
            flight1,
            client1,
            flight1.getPrice(),
            false,
            new Timestamp(Calendar.getInstance().getTime().getTime()));

    private final Ticket ticket2 = new Ticket(
            null,
            flight2,
            client1,
            flight2.getPrice(),
            null,
            null,
            true,
            new Timestamp(Calendar.getInstance().getTime().getTime()),
            new Timestamp(Calendar.getInstance().getTime().getTime()));

    private final Ticket ticket3 = new Ticket(
            null,
            flight1,
            client2,
            flight1.getPrice(),
            null,
            null,
            true,
            new Timestamp(Calendar.getInstance().getTime().getTime()),
            new Timestamp(Calendar.getInstance().getTime().getTime()));

    private final Ticket ticket4 = new Ticket(
            null,
            flight2,
            client2,
            flight2.getPrice(),
            null,
            null,
            true,
            new Timestamp(Calendar.getInstance().getTime().getTime()),
            new Timestamp(Calendar.getInstance().getTime().getTime()));

    private final Ticket ticket5 = new Ticket(
            flight3,
            client2,
            flight3.getPrice(),
            false,
            new Timestamp(Calendar.getInstance().getTime().getTime()));


    @BeforeEach
    void fillDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(client1);
            session.persist(client2);
            session.persist(airport1);
            session.persist(airport2);
            session.persist(airport3);
            session.persist(company1);
            session.persist(company2);
            session.persist(flight1);
            session.persist(flight2);
            session.persist(flight3);
            session.persist(ticket1);
            session.persist(ticket2);
            session.persist(ticket3);
            session.persist(ticket4);
            session.persist(ticket5);
            session.getTransaction().commit();
        }
    }

    @AfterEach
    @BeforeAll
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM ticket").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM flight").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM company").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM client").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM airport").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void filterFindTest() {
        Set<Integer> paidFor = Set.of(ticket2.getId(), ticket3.getId(), ticket4.getId());
        Set<Integer> client1Departure26 = Set.of(ticket1.getId(), ticket2.getId());
        Set<Integer> client1DepartureCity2 = Set.of(ticket1.getId(), ticket2.getId());
        Set<Integer> client2ArrivalCity2 = Set.of(ticket5.getId());
        Set<Integer> client1UnpaidFor = Set.of(ticket1.getId());
        Set<Integer> fullMatch = Set.of(ticket1.getId());
        Set<Integer> departureOn28 = Set.of();

        TicketDAO.Filter paidForFilter = TicketDAO.getFilterBuilder().isPaidFor(true).build();
        TicketDAO.Filter client1Departure26Filter = TicketDAO.getFilterBuilder()
                .clientId(client1.getId())
                .departureDate(day1)
                .build();
        TicketDAO.Filter client1DepartureCity2Filter = TicketDAO.getFilterBuilder()
                .clientId(client1.getId())
                .departureCity("city2")
                .build();
        TicketDAO.Filter client2ArrivalCity2Filter = TicketDAO.getFilterBuilder()
                .clientId(client2.getId())
                .arrivalCity("city2")
                .build();
        TicketDAO.Filter client1UnpaidForFilter = TicketDAO.getFilterBuilder()
                .clientId(client1.getId())
                .isPaidFor(false)
                .build();
        TicketDAO.Filter fullMatchFilter = TicketDAO.getFilterBuilder()
                .clientId(ticket1.getClient().getId())
                .departureDate(day1)
                .departureCity(ticket1.getFlight().getDepartureAirport().getCity())
                .arrivalCity(ticket1.getFlight().getArrivalAirport().getCity())
                .isPaidFor(ticket1.getIsPaidFor())
                .build();
        TicketDAO.Filter departureOn28Filter = TicketDAO.getFilterBuilder().departureDate(day3).build();

        Map<TicketDAO.Filter, Set<Integer>> correctResults = new HashMap<>(Map.of(
                paidForFilter, paidFor,
                client1Departure26Filter, client1Departure26,
                client1DepartureCity2Filter, client1DepartureCity2,
                client2ArrivalCity2Filter, client2ArrivalCity2,
                client1UnpaidForFilter, client1UnpaidFor,
                fullMatchFilter, fullMatch,
                departureOn28Filter, departureOn28
        ));

        for (Map.Entry<TicketDAO.Filter, Set<Integer>> entry : correctResults.entrySet()) {
            Set<Integer> correct = entry.getValue();
            TicketDAO.Filter filter = entry.getKey();
            Set<Integer> result = ticketDAO.getTicketList(filter).stream().map(Ticket::getId).collect(Collectors.toSet());
            Assertions.assertEquals(correct, result, String.format("Failure for filter:\n" + filter.toString() + "\n"));
        }
    }
}
