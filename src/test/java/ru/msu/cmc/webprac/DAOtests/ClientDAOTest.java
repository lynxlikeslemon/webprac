package ru.msu.cmc.webprac.DAOtests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.DAO.ClientDAO;
import ru.msu.cmc.webprac.backend.entity.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class ClientDAOTest {
    @Autowired
    private ClientDAO clientDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private final Client client1 = new Client("Александр", "Александров", "Александрович", "+7(777)777-77-77");
    private final Client client2 = new Client("Борис", "Борисов", "Борисович", "+8(888)888-88-88");
    private final Client client3 = new Client("Валентин", "Валентинов", "Валентинович", "+9(999)999-99-99");
    private final Client client4 = new Client("Борис", "Александров", "Валентинович", "+1(234)567-89-01");

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
            client3,
            flight2.getPrice(),
            null,
            null,
            true,
            new Timestamp(Calendar.getInstance().getTime().getTime()),
            new Timestamp(Calendar.getInstance().getTime().getTime()));

    private final Ticket ticket5 = new Ticket(
            flight1,
            client4,
            flight1.getPrice(),
            false,
            new Timestamp(Calendar.getInstance().getTime().getTime()));

    @BeforeEach
    void fillDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(client1);
            session.persist(client2);
            session.persist(client3);
            session.persist(client4);
            session.persist(airport1);
            session.persist(airport2);
            session.persist(airport3);
            session.persist(company1);
            session.persist(company2);
            session.persist(flight1);
            session.persist(flight2);
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
        Set<Integer> company1Clients = Stream.of(client1, client2, client4).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> company2Clients = Stream.of(client1, client3).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> flight1Passengers = Stream.of(client1, client2, client4).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> flight2Passengers = Stream.of(client1, client3).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> company1ClientsPaid = Stream.of(client2).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> flight2PassengersUnpaid = Set.of();
        Set<Integer> aLastName = Stream.of(client1, client4).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> bFirstName = Stream.of(client2, client4).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> vFathersName = Stream.of(client3, client4).map(Client::getId).collect(Collectors.toSet());
        Set<Integer> fullMatchName = Set.of(client1.getId());
        Set<Integer> fullMatchInfo = Set.of(client1.getId());
        Set<Integer> phone = Set.of(client1.getId());

        ClientDAO.Filter company1ClientsFilter = ClientDAO.getFilterBuilder().companyName(company1.getName()).build();
        ClientDAO.Filter company2ClientsFilter = ClientDAO.getFilterBuilder().companyName(company2.getName()).build();
        ClientDAO.Filter flight1PassengersFilter = ClientDAO.getFilterBuilder().flightId(flight1.getId()).build();
        ClientDAO.Filter flight2PassengersFilter = ClientDAO.getFilterBuilder().flightId(flight2.getId()).build();
        ClientDAO.Filter company1ClientsPaidFilter = ClientDAO.getFilterBuilder()
                .companyName(company1.getName())
                .ticketPaid(true)
                .build();
        ClientDAO.Filter flight2PassengersUnpaidFilter = ClientDAO.getFilterBuilder().flightId(flight2.getId()).ticketPaid(false).build();
        ClientDAO.Filter aLastNameFilter = ClientDAO.getFilterBuilder().lastName("Александров").build();
        ClientDAO.Filter bFirstNameFilter = ClientDAO.getFilterBuilder().firstName("Борис").build();
        ClientDAO.Filter vFathersNameFilter = ClientDAO.getFilterBuilder().fathersName("Валентинович").build();
        ClientDAO.Filter fullMatchNameFilter = ClientDAO.getFilterBuilder()
                .lastName("Александров")
                .firstName("Александр")
                .fathersName("Александрович")
                .build();
        ClientDAO.Filter fullMatchInfoFilter = ClientDAO.getFilterBuilder()
                .lastName("Александров")
                .firstName("Александр")
                .fathersName("Александрович")
                .companyName(company1.getName())
                .flightId(flight1.getId())
                .ticketPaid(false)
                .phoneNumber(client1.getPhoneNumber())
                .build();
        ClientDAO.Filter phoneFilter = ClientDAO.getFilterBuilder().phoneNumber(client1.getPhoneNumber()).build();

        Map<ClientDAO.Filter, Set<Integer>> correctResults = new HashMap<>(Map.of(
                company1ClientsFilter, company1Clients,
                company2ClientsFilter, company2Clients,
                flight1PassengersFilter, flight1Passengers,
                flight2PassengersFilter, flight2Passengers,
                company1ClientsPaidFilter, company1ClientsPaid,
                flight2PassengersUnpaidFilter, flight2PassengersUnpaid,
                aLastNameFilter, aLastName,
                bFirstNameFilter, bFirstName,
                vFathersNameFilter, vFathersName,
                fullMatchNameFilter, fullMatchName
        ));
        correctResults.put(fullMatchInfoFilter, fullMatchInfo);
        correctResults.put(phoneFilter, phone);

        for (Map.Entry<ClientDAO.Filter, Set<Integer>> entry : correctResults.entrySet()) {
            Set<Integer> correct = entry.getValue();
            ClientDAO.Filter filter = entry.getKey();
            Set<Integer> result = clientDAO.getClientList(filter).stream().map(Client::getId).collect(Collectors.toSet());
            Assertions.assertEquals(correct, result, String.format("Failure for filter:\n" + filter.toString() + "\n"));
        }

        Assertions.assertEquals(client4.getId(), clientDAO.getByPhoneNumber(client4.getPhoneNumber()).getId());
    }
}
