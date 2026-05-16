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
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
public class TicketPurchaseTest {
    @LocalServerPort
    private int port;

    @Autowired
    private SessionFactory sessionFactory;

    private static final String clientPageTitle = "Клиент";
    private static final String registerTitle = "Новый клиент";
    private static final String flightPageTitle = "Рейс";

    private static final String ticketPageTitle = "Билет";
    private static final String ticketListPageTitle = "Билеты";

    private static final String bookingTitle = "Бронирование";
    private static final String paymentPageTitle = "Оплата";

    private static final String paymentSuccessTitle = "Оплата успешна";
    private static final String paymentErrorTitle = "Ошибка при оплате";
    private static final String errorTitle = "Ошибка";

    @BeforeEach
    void fillDatabase() {
        cleanDatabase();
        try (Session session = sessionFactory.openSession()) {
            final LocalDate day1 = LocalDate.of(2026, 6, 6);
            final LocalDate day2 = LocalDate.of(2027, 7, 7);

            final Timestamp time1 = Timestamp.valueOf(LocalDateTime.of(day1, LocalTime.NOON));
            final Timestamp time2 = Timestamp.valueOf(LocalDateTime.of(day2, LocalTime.NOON));

            Client client = new Client("a", "b", "c", "+7(777)777-77-77");
            Company company1 = new Company("company1");
            Airport airport1 = new Airport("ABC", "airport1", "city1");
            Airport airport2 = new Airport("DEF", "airport2", "city2");
            BonusCard bonusCard = new BonusCard(1, company1, client, BigDecimal.valueOf(2));

            Flight flight1 = new Flight("AB-1234", company1, airport2, airport1, time1, time2, BigDecimal.ONE, 1, 0);
            Flight flight2 = new Flight("CD-5678", company1, airport1, airport2, time1, time2, BigDecimal.ONE, 1, 1);

            session.beginTransaction();
            session.persist(client);
            session.persist(company1);
            session.persist(bonusCard);
            session.persist(airport1);
            session.persist(airport2);
            session.persist(flight1);
            session.persist(flight2);
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

    private void bookTicket(String departureCity,
                            WebDriver driver,
                            WebDriverWait wait,
                            String phoneNumber,
                            List<String> knownPhoneNumbers,
                            boolean isSuccess) {
        driver.get("http://localhost:" + port + '/');
        TestHelpers.fillField(wait, "departureCity", departureCity);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("flightSearchSubmit"))).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flightTable")));
        List<WebElement> flightData = driver.findElements(By.id("flightTable"))
                .getFirst().findElements(By.tagName("tbody"))
                .getFirst().findElements(By.tagName("tr"));
        Assertions.assertEquals(1, flightData.size());

        TestHelpers.scrollAndClick(driver, wait, flightData.getFirst().findElement(By.id("flightPageLink")));
        wait.until(ExpectedConditions.titleIs(flightPageTitle));

        TestHelpers.scrollAndClick(driver, wait, "bookTicket");
        wait.until(ExpectedConditions.titleIs(bookingTitle));

        TestHelpers.fillField(wait, "clientPhoneNumber", phoneNumber);
        TestHelpers.scrollAndClick(driver, wait, "findClient");

        wait.until(ExpectedConditions.or(
                ExpectedConditions.elementToBeClickable(By.id("createClientButton")),
                ExpectedConditions.elementToBeClickable(By.id("submit"))));


        if (knownPhoneNumbers.contains(phoneNumber)) {
            WebElement submit = driver.findElement(By.id("submit"));
            Assertions.assertTrue(submit.isEnabled());
            TestHelpers.scrollAndClick(driver, wait, "submit");
        } else {
            TestHelpers.scrollAndClick(driver, wait, "createClientButton");
            registerClient(driver, wait);
            List<String> newPhoneList = new ArrayList<>(knownPhoneNumbers);
            newPhoneList.add(phoneNumber);
            bookTicket(departureCity, driver, wait, phoneNumber, newPhoneList, isSuccess);
        }

        if (!isSuccess) {
            wait.until(ExpectedConditions.titleIs(errorTitle));
        }
    }

    private void purchaseTicket(WebDriver driver, WebDriverWait wait, String phoneNumber, int usedAmount, boolean isSuccess, boolean useBonus) {
        wait.until(ExpectedConditions.titleIs(ticketPageTitle));
        TestHelpers.scrollAndClick(driver, wait, "paymentButton");
        wait.until(ExpectedConditions.titleIs(paymentPageTitle));

        List<WebElement> bonusRows = TestHelpers.getTableRows(wait, "bonusInfo");
        Assertions.assertEquals(1, bonusRows.size());

        if (useBonus) {
            bonusRows.getFirst().findElement(By.id("selectBonusButton")).click();

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("bonusActive")));
            TestHelpers.fillField(wait, "bonusAmount", Integer.toString(usedAmount));
            TestHelpers.scrollAndClick(driver, wait, "confirmBonus");
        }

        WebElement paymentButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("submitPayment")));
        String price = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("totalPrice"))).getText();
        Assertions.assertEquals(String.format("Итоговая стоимость билета: %.2f", BigDecimal.valueOf(1 - usedAmount)), price);
        TestHelpers.scrollAndClick(driver, wait, paymentButton);
        wait.until(ExpectedConditions.or(ExpectedConditions.titleIs(paymentSuccessTitle), ExpectedConditions.titleIs(paymentErrorTitle)));

        if (isSuccess) {
            Assertions.assertEquals(paymentSuccessTitle, driver.getTitle());
        } else {
            Assertions.assertEquals(paymentErrorTitle, driver.getTitle());

        }
        wait.until(ExpectedConditions.elementToBeClickable(By.id("ticketPageLink"))).click();
        wait.until(ExpectedConditions.titleIs(ticketPageTitle));

        try {
            TestHelpers.getNthLineOfText(wait, "paymentInfo", 1);
        } catch (AssertionError e) {
            Assertions.assertFalse(isSuccess);
            Assertions.assertTrue(driver.findElement(By.id("paymentButton")).isEnabled());
            return;
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.id("clientPageLink"))).click();
        wait.until(ExpectedConditions.titleIs(clientPageTitle));

        String clientPhoneNumber = TestHelpers.getNthLineOfText(wait, "clientInfo", 3);
        Assertions.assertEquals("Номер телефона: " + phoneNumber, clientPhoneNumber);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("clientTicketLink"))).click();
        wait.until(ExpectedConditions.titleIs(ticketListPageTitle));
        List<WebElement> ticketData = TestHelpers.getTableRows(wait, "ticketTable");

        Assertions.assertEquals(1, ticketData.size());

        List<WebElement> cells = ticketData.getFirst().findElements(By.tagName("td"));
        Assertions.assertEquals("city2 (DEF)", cells.get(0).getText());
        Assertions.assertEquals("city1 (ABC)", cells.get(1).getText());
        Assertions.assertEquals("Оплачен", cells.get(4).getText());
    }

    private void registerClient(WebDriver driver, WebDriverWait wait) {
        wait.until(ExpectedConditions.titleIs(registerTitle));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("saveClient")));

        TestHelpers.fillField(wait, "firstName", "First");
        TestHelpers.fillField(wait, "lastName", "Last");
        TestHelpers.fillField(wait, "fathersName", "Father");

        TestHelpers.scrollAndClick(driver, wait, "saveClient");
    }

    @Test
    public void purchaseWithBonus() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setSize(new Dimension(1024,768));
        driver.manage().window().setPosition(new Point(0,0));

        String phoneNumber = "+7(777)777-77-77";

        bookTicket("city2", driver, wait, phoneNumber, List.of(phoneNumber), true);
        purchaseTicket(driver, wait, phoneNumber, 1, true, true);

        driver.quit();
    }

    @Test
    public void purchaseWithWrongBonus() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        String phoneNumber = "+7(777)777-77-77";

        bookTicket("city2", driver, wait, phoneNumber, List.of(phoneNumber), true);
        purchaseTicket(driver, wait, phoneNumber, 2, false, true);

        driver.quit();
    }

    @Test
    public void purchaseWithoutBonus() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        String phoneNumber = "+7(777)777-77-77";

        bookTicket("city2", driver, wait, phoneNumber, List.of(phoneNumber), true);
        purchaseTicket(driver, wait, phoneNumber, 0, true, false);

        driver.quit();
    }

    @Test
    public void purchaseNewClient() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        String phoneNumber = "+7(123)123-12-12";

        bookTicket("city2", driver, wait, phoneNumber, List.of("+7(777)777-77-77"), true);
        purchaseTicket(driver, wait, phoneNumber, 0, true, false);

        driver.quit();
    }

    @Test
    public void purchaseFullFlight() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        String phoneNumber = "+7(777)777-77-77";

        bookTicket("city1", driver, wait, phoneNumber, List.of(phoneNumber), false);

        driver.quit();
    }
}
