package ru.msu.cmc.webprac.FrontendTests;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webprac.backend.entity.BonusCard;
import ru.msu.cmc.webprac.backend.entity.Client;
import ru.msu.cmc.webprac.backend.entity.Company;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application.properties")
public class ClientCreationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private SessionFactory sessionFactory;

    private static final String indexTitle = "Главная страница";
    private static final String clientsTitle = "Клиенты";
    private static final String registerTitle = "Новый клиент";

    private static final List<String> requiredFields = List.of(
            "firstName",
            "lastName",
            "fathersName",
            "phoneNumber");

    private List<String> optionalFields = List.of(
            "email",
            "address"
    );

    private static final List<String> bonusInputs = List.of(
            "bonusIds",
            "bonusAmounts",
            "bonusCompanies"
    );

    private static final Map<String, String> correctOptions = Map.of(
            "firstName", "First",
            "lastName", "Last",
            "fathersName", "Father",
            "phoneNumber", "+7(123)123-12-12",
            "email", "email@example.com",
            "address", "address"
    );

    private static final Map<String, String> correctBonusOptions = Map.of(
            "bonusIds", "987654321",
            "bonusAmounts", "0.00",
            "bonusCompanies", "company2"
    );

    private static final Map<String, String> errorOptions = Map.of(
            "phoneNumber", "+7(777)777-77-77"
    );

    private static final Map<String, String> errorBonusOptions = Map.of(
            "bonusIds", "1"
    );

    @BeforeEach
    void fillDatabase() {
        cleanDatabase();
        try (Session session = sessionFactory.openSession()) {
            Client client = new Client("a", "b", "c", "+7(777)777-77-77");
            Company company1 = new Company("company1");
            Company company2 = new Company("company2");
            BonusCard bonusCard = new BonusCard(1, company1, client, BigDecimal.ONE);
            session.beginTransaction();
            session.persist(client);
            session.persist(company1);
            session.persist(company2);
            session.persist(bonusCard);
            session.getTransaction().commit();
        }
    }

    @AfterEach
    void cleanDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createNativeMutationQuery("DELETE FROM bonus_card").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM client").executeUpdate();
            session.createNativeMutationQuery("DELETE FROM company").executeUpdate();
            session.getTransaction().commit();
        }
    }

    private void getToClientPage(WebDriver driver, WebDriverWait wait) {
        driver.manage().window().setPosition(new Point(0,0));
        driver.manage().window().setSize(new Dimension(1024,768));
        driver.get("http://localhost:" + port + '/');
        Assertions.assertEquals(indexTitle, driver.getTitle());
        wait.until(ExpectedConditions.elementToBeClickable(By.id("clientListLink"))).click();

        Assertions.assertEquals(clientsTitle, driver.getTitle());
        wait.until(ExpectedConditions.elementToBeClickable(By.id("registerButton"))).click();

        Assertions.assertEquals(registerTitle, driver.getTitle());
    }

    private void tryToCreate(WebDriver driver, WebDriverWait wait, Map<String, String> inputs, Map<String, String> bonusInputs, String resultingPage, boolean isCorrect) {
        getToClientPage(driver, wait);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("saveClient")));

        for (Map.Entry<String, String> input : inputs.entrySet()) {
            String id = input.getKey();
            String value = input.getValue();

            TestHelpers.fillField(wait, id, value);
        }

        if (bonusInputs != null) {
            TestHelpers.scrollAndClick(driver, wait,"addBonusCard");

            for (Map.Entry<String, String> input : bonusInputs.entrySet()) {
                String id = input.getKey();
                String value = input.getValue();

                if (id.equals("bonusCompanies")) {
                    TestHelpers.selectValueInField(wait, "bonusCompanies", value);
                    continue;
                }

                TestHelpers.fillField(wait, id, value);
            }
        }

        TestHelpers.scrollAndClick(driver, wait, "saveClient");

        wait.until(ExpectedConditions.not(ExpectedConditions.titleContains("Новый клиент")));

        Assertions.assertEquals(resultingPage, driver.getTitle(), inputs.toString());

        if (isCorrect) {
            checkCorrectness(driver, wait, inputs, bonusInputs);
            deleteClient(driver, wait, inputs.get("phoneNumber"));
        }
    }

    private void checkCorrectness(WebDriver driver, WebDriverWait wait, Map<String, String> inputs, Map<String, String> bonusInputs) {
        WebElement clientInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("clientInfo")));
        List<WebElement> data = clientInfo.findElements(By.tagName("span"));
        Assertions.assertEquals("Фамилия: " + inputs.get("lastName"), data.get(0).getText());
        Assertions.assertEquals("Имя: " + inputs.get("firstName"), data.get(1).getText());
        Assertions.assertEquals("Отчество: " + inputs.get("fathersName"), data.get(2).getText());
        Assertions.assertEquals("Номер телефона: " + inputs.get("phoneNumber"), data.get(3).getText());
        if (inputs.containsKey("email")) {
            Assertions.assertEquals("e-mail: " + inputs.get("email"), data.get(4).getText());
        } else {
            Assertions.assertEquals("e-mail: -", data.get(4).getText());
        }

        if (inputs.containsKey("address")) {
            Assertions.assertEquals("Адрес: " + inputs.get("address"), data.get(5).getText());
        } else {
            Assertions.assertEquals("Адрес: -", data.get(5).getText());
        }

        if (bonusInputs != null) {
            WebElement bonusInfo = driver.findElement(By.id("bonusInfo"));
            List<WebElement> lines = bonusInfo.findElements(By.tagName("tbody")).getFirst().findElements(By.tagName("tr"));
            Assertions.assertEquals(1, lines.size());
            List<WebElement> bonusData = lines.getFirst().findElements(By.tagName("td"));

            Assertions.assertEquals(bonusInputs.get("bonusIds"), bonusData.get(0).getText());
            Assertions.assertEquals("company2", bonusData.get(1).getText());
            Assertions.assertEquals(bonusInputs.get("bonusAmounts"), bonusData.get(2).getText());
        } else {
            WebElement bonusInfo = driver.findElement(By.id("bonusInfo"));
            List<WebElement> lines = bonusInfo.findElements(By.tagName("tbody")).getFirst().findElements(By.tagName("tr"));
            Assertions.assertEquals(1, lines.size());
            Assertions.assertEquals("Бонусные карты не найдены", lines.getFirst().getText());
        }
    }

    private void deleteClient(WebDriver driver, WebDriverWait wait, String phoneNumber) {
        TestHelpers.scrollAndClick(driver, wait, "deleteButton");
        wait.until(ExpectedConditions.titleContains(clientsTitle));
        List<WebElement> clientRows = TestHelpers.getTableRows(wait, "clientTable");

        for (WebElement row : clientRows) {
            String phoneNumberChecked = row.findElements(By.tagName("td")).get(3).getText();
            Assertions.assertNotEquals(phoneNumber, phoneNumberChecked);
        }
    }

    @Test
    public void clientTest() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.of(10, ChronoUnit.SECONDS));

        for (String required : requiredFields) {
            Map<String, String> inputs = new HashMap<>(correctOptions);

            if (errorOptions.containsKey(required)) {
                inputs.put(required, errorOptions.get(required));
                tryToCreate(driver, wait, inputs, null, "Ошибка", false);
            }
        }

        Map<String, String> inputs = new HashMap<>(correctOptions);
        for (String optional : optionalFields) {
            if (errorOptions.containsKey(optional)) {
                inputs.put(optional, errorOptions.get(optional));
                tryToCreate(driver, wait, inputs, null, "Ошибка", false);
            }

            inputs.put(optional, correctOptions.get(optional));
            tryToCreate(driver, wait, inputs, null, "Клиент", true);
        }

        for (String bonus : bonusInputs) {
            Map<String, String> bonusInputs = new HashMap<>(correctBonusOptions);
            if (errorBonusOptions.containsKey(bonus)) {
                bonusInputs.put(bonus, errorBonusOptions.get(bonus));
                tryToCreate(driver, wait, inputs, bonusInputs, "Ошибка", false);
            }
        }

        tryToCreate(driver, wait, correctOptions, correctBonusOptions, "Клиент", true);
        driver.quit();
    }
}
