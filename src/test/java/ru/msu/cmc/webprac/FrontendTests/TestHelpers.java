package ru.msu.cmc.webprac.FrontendTests;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public abstract class TestHelpers {
    public static void scrollAndClick(WebDriver driver, WebDriverWait wait, String id) {
        scrollAndClick(driver, wait, By.id(id));
    }

    public static void scrollAndClick(WebDriver driver, WebDriverWait wait, By locator) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollAndClick(driver, button);
    }

    public static void scrollAndClick(WebDriver driver, WebDriverWait wait, WebElement element) {
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(element));
        scrollAndClick(driver, button);
    }

    private static void scrollAndClick(WebDriver driver, WebElement button) {
        Actions actions = new Actions(driver);
        actions.moveToElement(button).click().perform();
    }

    public static List<WebElement> getTableRows(WebDriverWait wait, String tableId) {
        WebElement table = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(tableId)));
        WebElement tableBody = table.findElement(By.tagName("tbody"));
        return tableBody.findElements(By.tagName("tr"));
    }

    public static String getNthLineOfText(WebDriverWait wait, String textBlockId, int i) {
        WebElement textBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(textBlockId)));
        List<WebElement> textLines = textBlock.findElements(By.tagName("span"));
        Assertions.assertTrue(textLines.size() > i);
        return textLines.get(i).getText();
    }


    public static String getHeaderOfTextBlock(WebDriverWait wait, String textBlockId, String tagName) {
        WebElement textBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(textBlockId)));
        return textBlock.findElement(By.tagName(tagName)).getText();
    }

    public static void fillField(WebDriverWait wait, String fieldId, String value) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(fieldId)));
        field.clear();
        field.sendKeys(value);
    }

    public static void selectValueInField(WebDriverWait wait, String fieldId, String textValue) {
        WebElement field = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(fieldId)));
        Select fieldSelect = new Select(field);
        fieldSelect.selectByVisibleText(textValue);
    }
}
