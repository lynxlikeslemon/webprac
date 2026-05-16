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
public class FlightEditTest {
    @LocalServerPort
    private int port;

    @Autowired
    private SessionFactory sessionFactory;
    private static final String flightPageTitle = "Рейс";
    private static final String errorTitle = "Ошибка";

    private static final Map<String, String> oldFlight1Data = Map.of(
            "company", "company1",
            "departureAirport", "airport1",
            "arrivalAirport", "airport2",
            "departureDate", "2026-06-06",
            "arrivalDate", "2026-06-07",
            "departureTime", "12:00",
            "arrivalTime", "12:00",
            "price", "1.00",
            "freePlaces", "0/2",
            "flightTime", "24:00:00"
    );

    public static final Set<String> selectionFields = Set.of(
            "companyId",
            "departureAirportId",
            "arrivalAirportId"
    );

    public static final Map<String, String> flightDataFormat = Map.of(
            "company", "Авиакомпания: %s",
            "departureAirport", "Аэропорт отправления: %s",
            "arrivalAirport", "Аэропорт прибытия: %s",
            "departureDate", "Дата отправления: %s",
            "arrivalDate", "Дата прибытия: %s",
            "departureTime", "Время отправления: %s",
            "arrivalTime", "Время прибытия: %s",
            "price", "Стоимость билета: %s",
            "freePlaces", "Число свободных мест: %s",
            "flightTime", "Время в пути: %s"
    );

    @BeforeEach
    void fillDatabase() {
        cleanDatabase();
        try (Session session = sessionFactory.openSession()) {
            final LocalDate day1 = LocalDate.of(2026, 6, 6);
            final LocalDate day2 = LocalDate.of(2026, 6, 7);

            final Timestamp time1 = Timestamp.valueOf(LocalDateTime.of(day1, LocalTime.NOON));
            final Timestamp time2 = Timestamp.valueOf(LocalDateTime.of(day2, LocalTime.NOON));

            Client client = new Client("a", "b", "c", "+7(777)777-77-77");
            Company company1 = new Company("company1");
            Company company2 = new Company("company2");
            Airport airport1 = new Airport("ABC", "airport1", "city1");
            Airport airport2 = new Airport("DEF", "airport2", "city2");
            BonusCard bonusCard = new BonusCard(1, company1, client, BigDecimal.valueOf(2));

            Flight flight1 = new Flight("AB-1234", company1, airport1, airport2, time1, time2, BigDecimal.ONE, 2, 2);
            Flight flight2 = new Flight("CD-5678", company2, airport2, airport1, time1, time2, BigDecimal.ONE, 1, 1);

            session.beginTransaction();
            session.persist(client);
            session.persist(company1);
            session.persist(company2);
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

    private void findFlight1(WebDriver driver, WebDriverWait wait) {
        driver.get("http://localhost:" + port + '/');
        TestHelpers.fillField(wait, "departureCity", "city1");
        TestHelpers.scrollAndClick(driver, wait, "flightSearchSubmit");

        List<WebElement> getFlightRows = TestHelpers.getTableRows(wait, "flightTable");
        Assertions.assertEquals(1, getFlightRows.size());
        TestHelpers.scrollAndClick(driver, wait, getFlightRows.getFirst().findElements(By.tagName("td")).get(7).findElement(By.id("flightPageLink")));

        wait.until(ExpectedConditions.titleIs(flightPageTitle));

        String flightId = TestHelpers.getHeaderOfTextBlock(wait, "flightInfo", "h4");
        Assertions.assertEquals("Рейс номер AB-1234", flightId);
    }

    private void editFlightData(WebDriver driver, WebDriverWait wait, Map<String, String> formInput, Map<String, String> newData, boolean isSuccess) {
        findFlight1(driver, wait);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("editFlightButton"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flightEditForm")));

        for (Map.Entry<String, String> input : formInput.entrySet()) {
            String fieldName = input.getKey();
            String fieldValue = input.getValue();

            if (selectionFields.contains(fieldName)) {
                TestHelpers.selectValueInField(wait, fieldName, fieldValue);
                continue;
            }

            TestHelpers.fillField(wait, fieldName, fieldValue);
        }

        TestHelpers.scrollAndClick(driver, wait, "flightSaveButton");
        if (!isSuccess) {
            wait.until(ExpectedConditions.titleIs(errorTitle));
            return;
        }
        compareFlightData(wait, newData);
        driver.findElement(By.id("flightListLink")).click();
        List<WebElement> rows = TestHelpers.getTableRows(wait, "flightTable");
        Assertions.assertEquals(2, rows.size());
        Set<String> flightIds = Set.of("CD-5678", newData.getOrDefault("flightId", "AB-1234"));

        for (WebElement row : rows) {
            Assertions.assertTrue(flightIds.contains(row.findElements(By.tagName("td")).getFirst().getText()));
        }
    }

    private void compareWithFieldList(WebDriverWait wait, Map<String, String> fieldData, Set<String> checkedFields) {
        for (Map.Entry<String, String> field : fieldData.entrySet()) {
            String lineId = field.getKey();
            String lineValue = field.getValue();
            if (checkedFields.contains(lineId)) {
                continue;
            }

            String text = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(lineId))).getText();
            Assertions.assertEquals(String.format(flightDataFormat.get(lineId), lineValue), text);

            checkedFields.add(lineId);
        }
    }

    private void compareFlightData(WebDriverWait wait, Map<String, String> changedData) {
        WebElement info = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("flightInfo")));
        WebElement header = info.findElement(By.tagName("h4"));
        String id = "AB-1234";
        if (changedData.containsKey("flightId")) {
            id = changedData.get("flightId");
        }

        Set<String> checkedFields = new HashSet<>();
        checkedFields.add("flightId");
        Assertions.assertEquals(String.format("Рейс номер %s", id), header.getText());

        compareWithFieldList(wait, changedData, checkedFields);
        compareWithFieldList(wait, oldFlight1Data, checkedFields);
    }

    @Test
    public void changeAllDataTest() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        Map<String, String> formData = Map.of(
                "newFlightId", "EF-3456",
                "companyId", "company2",
                "departureAirportId", "DEF (airport2, city2)",
                "arrivalAirportId", "ABC (airport1, city1)",
                "departureDate", "06072026",
                "arrivalDate", "06082026",
                "departureTime", "0000AM",
                "arrivalTime", "1100AM",
                "price", "10",
                "places", "10"
        );

        Map<String, String> newFlight1Data = new HashMap<>(Map.of(
                "flightId", "EF-3456",
                "company", "company2",
                "departureAirport", "airport2",
                "arrivalAirport", "airport1",
                "departureDate", "2026-06-07",
                "arrivalDate", "2026-06-08",
                "departureTime", "00:00",
                "arrivalTime", "11:00",
                "price", "10.00",
                "freePlaces", "8/10"
        ));

        newFlight1Data.put("flightTime", "35:00:00");

        editFlightData(driver, wait, formData, newFlight1Data, true);

        driver.quit();
    }

    @Test
    public void changeNoData() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        Map<String, String> formData = Map.of();
        Map<String, String> newFlight1Data = Map.of();

        editFlightData(driver, wait, formData, newFlight1Data, true);

        driver.quit();
    }

    @Test
    public void changeToExistingIdTest() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        Map<String, String> formData = Map.of(
                "newFlightId", "CD-5678"
        );
        Map<String, String> newFlight1Data = Map.of();

        editFlightData(driver, wait, formData, newFlight1Data, false);

        driver.quit();
    }

    @Test
    public void changeToIncorrectDateTimeTest() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        Map<String, String> formData = Map.of(
                "arrivalDate", "06062026",
                "arrivalTime", "1159AM"
        );
        Map<String, String> newFlight1Data = Map.of();
        editFlightData(driver, wait, formData, newFlight1Data, false);

        driver.quit();
    }

    @Test
    public void changeToFewerSeats() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(3, ChronoUnit.SECONDS));
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));

        Map<String, String> formData = Map.of(
                "places", "1"
        );
        Map<String, String> newFlight1Data = Map.of();
        editFlightData(driver, wait, formData, newFlight1Data, false);

        driver.quit();
    }
}
