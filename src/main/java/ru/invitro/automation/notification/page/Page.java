package ru.invitro.automation.notification.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class Page {

    WebDriver driver;

    String url;

    WebDriverWait longWait;

    WebDriverWait shortWait;

    @FindBy(xpath = "//div[@id='loginIntroDefault']")
    private WebElement jenkinsLogo;

    @FindBy(xpath = "//input[@name='j_username']")
    private WebElement jenkinsLogin;

    @FindBy(xpath = "//input[@name='j_password']")
    private WebElement jenkinsPassword;

    @FindBy(xpath = "//input[@name='Submit']")
    private WebElement submitButton;

    @FindBy(xpath = "//div[text()='Invalid username or password']")
    private WebElement invalidLogin;

    Page(String url, long implicitly, long pageLoad, long shortTimeOut, long longTimeOut) {
        this.url = url;
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        Map<String, Object> chromeDrivePreference = new HashMap<>();
        chromeDrivePreference.put("profile.default_content_settings.popups", 0);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromeDrivePreference);
        options.addArguments("--disable-notifications");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(implicitly, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(pageLoad, TimeUnit.SECONDS);
        longWait = new WebDriverWait(driver, longTimeOut);
        shortWait = new WebDriverWait(driver, shortTimeOut);
        PageFactory.initElements(driver, this);
    }

    public void closePage() {
        driver.quit();
    }

    void authorization() {
        try {
            longWait.until(ExpectedConditions.visibilityOfAllElements(jenkinsLogo));
            longWait.until(ExpectedConditions.elementToBeClickable(submitButton));
            int count = 0;
            while (count < 3) {
                jenkinsLogin.clear();
                jenkinsPassword.clear();
                jenkinsLogin.click();
                for (char loginChar : "test".toCharArray()) {
                    jenkinsLogin.sendKeys(loginChar + "");
                }
                jenkinsPassword.clear();
                jenkinsPassword.click();
                for (char loginChar : "test".toCharArray()) {
                    jenkinsPassword.sendKeys(loginChar + "");
                }
                submitButton.click();
                try {
                    shortWait.until(ExpectedConditions.visibilityOf(invalidLogin));
                    count++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException j) {
                        j.printStackTrace();
                    }
                } catch (WebDriverException k) {
                    break;
                }
            }
        } catch (WebDriverException ignore) {

        }
    }
}
