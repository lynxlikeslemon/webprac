package ru.msu.cmc.webprac.DAOtests;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.DAO.BonusCardDAO;
import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Company;
import org.hibernate.Session;
import java.util.List;
import java.util.stream.Stream;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class BonusCardDAOTest {
    @Autowired
    private BonusCardDAO bonusCardDAO;

    @Autowired
    private SessionFactory sessionFactory;

    private final Client client1 = new Client("Имя", "Фамилия", "Отчество", "+7(777)777-77-77");
    private final Client client2 = new Client("Имя", "Фамилия", "Отчество", "+8(888)888-88-88");

    private final Company company = new Company("Название");

    private final BonusCard bonusCard1 = new BonusCard(123, company, client1, 123.456);
    private final BonusCard bonusCard2 = new BonusCard(456, company, client1, 789.0);


    @BeforeEach
    void fillDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.persist(client1);
            session.persist(client2);
            session.persist(company);
            session.persist(bonusCard1);
            session.persist(bonusCard2);
            session.getTransaction().commit();
        }
    }

    @AfterEach
    @BeforeAll
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM bonus_card").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM client").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM company").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void findByClientTest() {
        List<Integer> client1Bonus = Stream.of(bonusCard1, bonusCard2).map(BonusCard::getId).toList();
        List<Integer> client1result = bonusCardDAO
                .getBonusCardsByClientId(client1.getId())
                .stream()
                .map(BonusCard::getId)
                .toList();

        Assertions.assertEquals(client1Bonus, client1result, "Client1 bonus card list is incorrect");

        List<Integer> client2Bonus = List.of();
        List<Integer> client2result = bonusCardDAO
                .getBonusCardsByClientId(client2.getId())
                .stream()
                .map(BonusCard::getId)
                .toList();

        Assertions.assertEquals(client2Bonus, client2result, "Client2 bonus card list is incorrect");
    }
}
