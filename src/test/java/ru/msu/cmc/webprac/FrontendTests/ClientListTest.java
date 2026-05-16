package ru.msu.cmc.webprac.FrontendTests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.entity.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
public class ClientListTest {
    @LocalServerPort
    private int port;

    @Autowired
    private SessionFactory sessionFactory;

    private static final String clientsTitle = "Клиенты";

    @BeforeEach
    void fillDatabase() {
        cleanDatabase();
        try (Session session = sessionFactory.openSession()) {
            final LocalDate day1 = LocalDate.of(2026, 6, 6);
            final LocalDate day2 = LocalDate.of(2027, 7, 7);

            final Timestamp time1 = Timestamp.valueOf(LocalDateTime.of(day1, LocalTime.NOON));
            final Timestamp time2 = Timestamp.valueOf(LocalDateTime.of(day2, LocalTime.NOON));

            Client client1 = new Client("client1", "b", "c", "+7(777)777-77-77");
            Client client2 = new Client("client2", "b", "c", "+7(123)456-78-90");
            Client client3 = new Client("client3", "b", "c", "+7(000)000-00-00");

            Company company1 = new Company("company1");
            Company company2 = new Company("company2");

            Airport airport1 = new Airport("ABC", "airport1", "city1");
            Airport airport2 = new Airport("DEF", "airport2", "city2");

            Flight flight1 = new Flight("AB-1234", company1, airport2, airport1, time1, time2, BigDecimal.ONE, 3, 0);
            Flight flight2 = new Flight("CD-5678", company2, airport1, airport2, time1, time2, BigDecimal.ONE, 3, 0);
            Flight flight3 = new Flight("EF-9012", company2, airport1, airport2, time1, time2, BigDecimal.ONE, 3, 0);

            Ticket ticket1 = new Ticket(flight1, client1, BigDecimal.ONE, false, time1);
            Ticket ticket2 = new Ticket(flight2, client1, BigDecimal.ONE, false, time1);
            Ticket ticket3 = new Ticket(flight3, client2, BigDecimal.ONE, false, time1);

            session.beginTransaction();
            session.persist(client1);
            session.persist(client2);
            session.persist(client3);
            session.persist(company1);
            session.persist(company2);
            session.persist(airport1);
            session.persist(airport2);
            session.persist(flight1);
            session.persist(flight2);
            session.persist(flight3);
            session.persist(ticket1);
            session.persist(ticket2);
            session.persist(ticket3);
            session.getTransaction().commit();
        }
    }

    @AfterEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM ticket").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM flight").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM bonus_card").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM client").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM company").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM airport").executeUpdate();
            session.getTransaction().commit();
        }
    }

    private void findCompanyClients(WebDriver driver, WebDriverWait wait, String companyName, boolean isSuccess, Set<String> clientNames) {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("clientListLink"))).click();
        wait.until(ExpectedConditions.titleIs(clientsTitle));
        List<WebElement> initialRows = TestHelpers.getTableRows(wait, "clientTable");
        Assertions.assertEquals(3, initialRows.size(), "Not all clients listed initially");
        TestHelpers.fillField(wait, "companyName", companyName);
        String oldUrl = driver.getCurrentUrl();
        TestHelpers.scrollAndClick(driver, wait, "clientSearchButton");
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(oldUrl)));
        List<WebElement> clientRows = TestHelpers.getTableRows(wait, "clientTable");

        if (!isSuccess) {
            Assertions.assertEquals(1, clientRows.size());
            Assertions.assertEquals("Данных о клиентах не найдено", clientRows.getFirst().getText());
            return;
        }

        Set<String> foundClientNames = new HashSet<>();

        Assertions.assertEquals(clientNames.size(), clientRows.size());

        for (WebElement row : clientRows) {
            String name = row.findElements(By.tagName("td")).get(1).getText();
            foundClientNames.add(name);
            Assertions.assertTrue(clientNames.contains(name), String.format("Found person %s that is not a client of %s", name, companyName));
        }

        for (String name : clientNames) {
            Assertions.assertTrue(foundClientNames.contains(name), String.format("Did not find client %s of %s", name, companyName));
        }
    }

    @Test
    public void clientsCompany1() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        driver.get("http://localhost:" + port + '/');
        findCompanyClients(driver, wait, "company1", true, Set.of("client1"));

        driver.quit();
    }

    @Test
    public void clientsCompany2() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        driver.get("http://localhost:" + port + '/');
        findCompanyClients(driver, wait, "company2", true, Set.of("client1", "client2"));

        driver.quit();
    }

    @Test
    public void clientsUnexistingCompany() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        driver.get("http://localhost:" + port + '/');
        findCompanyClients(driver, wait, "company3", false, Set.of());

        driver.quit();
    }
}
