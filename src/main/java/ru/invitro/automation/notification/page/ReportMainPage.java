package ru.invitro.automation.notification.page;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.AttachThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.FullThucydidesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.ThucydidesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.tests.ThucydiesScenarioReport;
import ru.invitro.automation.notification.page.exception.ReportReadException;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class ReportMainPage extends Page {

    private final String selectLinkOpenInNewTab = Keys.chord(Keys.CONTROL, Keys.RETURN);

    private final String id;

    @FindBy(xpath = "/html/body")
    WebElement pageBody;

    @FindBy(xpath = "//div[@id='logo']")
    private WebElement logo;

    @FindBy(xpath = "//span[@class='projectname']") //
    private WebElement projectName;

    @FindBy(xpath = "//span[@class='date-and-time']") //
    private WebElement projectDate;

    @FindBy(xpath = "//h5[contains(text(),'Context')]/following-sibling::div//span[@class='badge']")
    private WebElement context;

    @FindBy(xpath = "//em[contains(text(),'Unsuccessful')]/ancestor::a[@href]") //
    private WebElement unsuccessfulTests;

    @FindBy(xpath = "//a[contains(text(),'Failed')]") //
    private WebElement failedTests;

    @FindBy(xpath = "//a[contains(text(),'Pending')]")
    private WebElement pendingTests;

    @FindBy(xpath = "//table[@class='table']//td[contains(@class,'aggregate-result-count')]//i[contains(@class,'pending-icon')]/following::td[1]")
    private WebElement pendingTestsCount;

    @FindBy(xpath = "//table[@class='table']//td[contains(@class,'aggregate-result-count')]//i[contains(@class,'failure-icon')]/following::td[1]")
    private WebElement failedTestsCount;

    @FindBy(xpath = "//table[@class='table']//td[contains(@class,'aggregate-result-count')]//i[contains(@class,'error-icon')]/following::td[1]")
    private WebElement brokenTestsCount;

    @FindBy(xpath = "//a[@href='#tests']") //
    private WebElement resultOfTests;

    @FindBy(xpath = "//li[@id='scenario-results_next']")
    private WebElement nextButton;

    @FindAll({@FindBy(xpath = "//td[2]/a[@href]")})
    private List<WebElement> failedTestsList;

    @FindAll({@FindBy(xpath = "//tr[contains(@class,'scenario-result')]")})
    private List<WebElement> listOfAllTests;

    @FindAll({@FindBy(xpath = "//tr[contains(@class,'test')]")})
    private List<WebElement> steps;

    @FindBy(xpath = "//button[contains(text(),'Exported')]")
    private WebElement reportButton;

    @FindBy(xpath = "//button[contains(text(),'Exported')]/../..//pre")
    private WebElement attachReport;

    @FindBy(xpath = "//div[contains(text(),'test scenarios')]")
    private WebElement scenarioCount;

    private List<ThucydiesScenarioReport> allFailedThucydiesScenarioReports;

    public ReportMainPage(String url, String operationID) {
        super(url, 30, 180, 30, 90);
        this.id = operationID;
    }

    public void getFullFailReport(FullThucydidesReport report) {

        try {
            collectBaseReportData(report);
            List<ThucydiesScenarioReport> failedThucydiesScenarioReports = new ArrayList<>();
            List<ThucydiesScenarioReport> pendingThucydiesScenarioReports = new ArrayList<>();
            boolean hasFaledTest = false;
            if (openUnsuccessfulTests()) {
                failedThucydiesScenarioReports = getFailedTests();
                hasFaledTest = true;
            }
            driver.navigate().to(url);
            if (openPendingTests()) {
                pendingThucydiesScenarioReports = getPendingTests();
                hasFaledTest = true;
            }
            if (!hasFaledTest) {
                report.setUnsuccessfulTestCount(0);
                return;
            }
            report.setUnsuccessfulTestCount(failedThucydiesScenarioReports.size() + pendingThucydiesScenarioReports.size());
            failedThucydiesScenarioReports.addAll(pendingThucydiesScenarioReports);
            report.setFailTestsList(failedThucydiesScenarioReports);
        } catch (WebDriverException e) {
            Logger.writeLog("Web page error " + report.getUrl() + "\n" + e.getMessage(), id);
            e.printStackTrace();
            report.setReportAvailable(false);
        } finally {
            closePage();
        }
    }

    public void collectBaseReportData(ThucydidesReport report) {
        if (!openReport(url)) {
            report.setReportAvailable(false);
            return;
        }
        report.setReportAvailable(true);
        report.setContext(getProjectContext());
        report.setReportDate(getProjectDate());
        report.setReportName(getProjectName());
        report.setReportScenarios(getReportScenario());
        report.setPendingTests(Integer.parseInt(pendingTestsCount.getText()));
        report.setBrokenTests(Integer.parseInt(brokenTestsCount.getText()));
        report.setFailedTests(Integer.parseInt(failedTestsCount.getText()));
        report.calculateUnsuccessfulTestCount();
    }

    private Integer getReportScenario() {
        return Integer.valueOf(scenarioCount.getText().replaceAll("\\(.*\\)", "").replaceAll("[^\\d]", ""));
    }

    public void getXmlPriceReport(AttachThucydiesReport report) {
        try {
            collectBaseReportData(report);
            try {
                if (openFailedTests()) {
                    List<String> xmlReports = getAttachReport();
                    report.setXmlReport(xmlReports);
                }
            } catch (ReportReadException | WebDriverException e) {
                Logger.writeLog("Web page error " + report.getUrl() + "\n" + e.getMessage(), id);
                e.printStackTrace();
            }
        } catch (WebDriverException e) {
            Logger.writeLog("Web page error " + report.getUrl() + "\n" + e.getMessage(), id);
            e.printStackTrace();
            report.setReportAvailable(false);
        } finally {
            closePage();
        }
    }

    private boolean openReport(String url) {
        int count = 0;
        while (count < 3) {
            try {
                driver.get(url);
                authorization();
                try {
                    if (!pageBody.getAttribute("class").equals("results-page")) {
                        driver.switchTo().frame("myframe");
                    }
                } catch (WebDriverException ignore) {
                }
                longWait.until(ExpectedConditions.visibilityOfAllElements(logo));
                return !check404();
            } catch (WebDriverException e) {
                Logger.writeLog("Web page error " + url + "\n" + e.getMessage(), id);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    Logger.writeLog(e1.getMessage(), id);
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    private boolean openTest(WebElement webElement) {
        try {
            webElement.click();
            resultOfTests.click();
            longWait.until(ExpectedConditions.visibilityOfAllElements(logo));
            return !check404();
        } catch (WebDriverException e) {
            Logger.writeLog("Web page open test error " + url + "\n" + e.getMessage(), id);
            return false;
        }
    }

    private boolean openUnsuccessfulTests() {
        return openTest(unsuccessfulTests);
    }

    private boolean openFailedTests() {
        return openTest(failedTests);
    }

    private boolean openPendingTests() {
        return openTest(pendingTests);
    }

    private String getProjectName() {
        return projectName.getText().trim();
    }

    private String getProjectDate() {
        return projectDate.getText().trim();
    }

    private String getProjectContext() {
        try {
            shortWait.until(ExpectedConditions.visibilityOfAllElements(context));
            String context = this.context.getText().trim();
            Pattern browserPattern = Pattern.compile("^ *([^ ]*) *\\d*$");
            Matcher matcher = browserPattern.matcher(context);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (WebDriverException e) {
            Logger.writeLog("Web page get context error " + url + "\n" + e.getMessage(), id);
            e.printStackTrace();
            return "";
        }
        return "";
    }

    private List<ThucydiesScenarioReport> getFailedTests() {
        allFailedThucydiesScenarioReports = new ArrayList<>();
        List<WebElement> rows = failedTestsList;
        if (rows.size() > 0) {
            while (true) {
                for (WebElement row : rows) {
                    ThucydiesScenarioReport failedThucydiesScenarioReport = new ThucydiesScenarioReport();
                    shortWait.until(ExpectedConditions.visibilityOfAllElements(row));
                    String testName = row.getAttribute("innerText").trim();
                    String testUrl = row.getAttribute("href");
                    failedThucydiesScenarioReport.setTestName(testName);
                    failedThucydiesScenarioReport.setTestUrl(testUrl);
                    String currentWindow = openInNewWindow(row);
                    List<WebElement> testSteps = steps;
                    for (WebElement step : testSteps) {
                        String stepStatus = step.getAttribute("class").trim();
                        if (stepStatus.equals("test-ERROR") || stepStatus.equals("test-FAILURE")) {
                            step.click();
                            WebElement stepDescription;
                            WebElement stepImage;
                            String stepName;
                            try {
                                stepDescription = step.findElement(By.xpath(".//div[@class='step-description']//span"));
                                stepName = stepDescription.getAttribute("innerText").trim();
                            } catch (WebDriverException e) {
                                stepName = "Cannot read step name";
                            }
                            failedThucydiesScenarioReport.setFailedStepName(stepName);
                            if (stepStatus.equals("test-ERROR")) {
                                failedThucydiesScenarioReport.setErrorType(ThucydiesScenarioReport.ErrorType.BROKEN);
                            } else {
                                failedThucydiesScenarioReport.setErrorType(ThucydiesScenarioReport.ErrorType.FAILED);
                            }
                            try {
                                longWait.until(ExpectedConditions.visibilityOf(reportButton));
                                reportButton.click();
                                longWait.until(ExpectedConditions.visibilityOf(reportButton));
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    Logger.writeLog(e.getMessage(), "");
                                    e.printStackTrace();
                                }
                                failedThucydiesScenarioReport.setAttach(attachReport.getText());
                            } catch (WebDriverException e) {
                                e.printStackTrace();
                            }
                            try {
                                stepImage = step.findElement(By.xpath(".//a/img"));
                                String stepURL = url.replaceAll("/$", "") + "/" + stepImage.getAttribute("href").trim();
                                System.out.println(stepURL);
                                driver.navigate().to(stepURL);
                                URL imageURL = new URL(stepURL);
                                URLConnection connection = imageURL.openConnection();
                                List<Cookie> cookies = new ArrayList<>(driver.manage().getCookies());
                                if (cookies.size() > 0) {
                                    connection.setRequestProperty("Cookie", cookies.get(0).toString());
                                }
                                BufferedImage saveImage = ImageIO.read(connection.getInputStream());
                                File file = new File(generateFileName() + ".png");
                                ImageIO.write(saveImage, "png", file);
                                failedThucydiesScenarioReport.setImage(file);
                                driver.navigate().back();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            driver.navigate().back();
                            break;
                        }
                    }
                    allFailedThucydiesScenarioReports.add(failedThucydiesScenarioReport);
                    driver.close();
                    driver.switchTo().window(currentWindow);
                }
                String nextButtonArgument = nextButton.getAttribute("class");
                if (!nextButtonArgument.contains("disabled")) {
                    nextButton.click();
                } else {
                    break;
                }
            }
        }
        return allFailedThucydiesScenarioReports;
    }

    private List<ThucydiesScenarioReport> getPendingTests() {
        allFailedThucydiesScenarioReports = new ArrayList<>();
        List<WebElement> rows = failedTestsList;
        if (rows.size() > 0) {
            while (true) {
                for (WebElement row : rows) {
                    ThucydiesScenarioReport failedThucydiesScenarioReport = new ThucydiesScenarioReport();
                    shortWait.until(ExpectedConditions.visibilityOfAllElements(row));
                    String testName = row.getAttribute("innerText").trim();
                    String testUrl = row.getAttribute("href");
                    failedThucydiesScenarioReport.setTestName(testName);
                    failedThucydiesScenarioReport.setTestUrl(testUrl);
                    String currentWindow = openInNewWindow(row);
                    List<WebElement> testSteps = steps;
                    for (WebElement step : testSteps) {
                        String stepStatus = step.getAttribute("class").trim();
                        if (stepStatus.equals("test-PENDING")) {
                            WebElement stepDescription = step.findElement(By.xpath(".//span[@class='top-level-step']"));
                            String stepName = stepDescription.getText().trim();
                            failedThucydiesScenarioReport.setFailedStepName(stepName);
                            failedThucydiesScenarioReport.setErrorType(ThucydiesScenarioReport.ErrorType.PENDING);
                            driver.navigate().back();
                            break;
                        }
                    }
                    allFailedThucydiesScenarioReports.add(failedThucydiesScenarioReport);
                    driver.close();
                    driver.switchTo().window(currentWindow);
                }
                String nextButtonArgument = nextButton.getAttribute("class");
                if (!nextButtonArgument.contains("disabled")) {
                    nextButton.click();
                } else {
                    break;
                }
            }
        }
        return allFailedThucydiesScenarioReports;
    }

    private List<String> getAttachReport() {
        List<WebElement> rows = failedTestsList;
        List<String> allTestsReport = new ArrayList<>();
        if (rows.size() > 0) {
            while (true) {
                for (WebElement row : rows) {
                    try {
                        shortWait.until(ExpectedConditions.visibilityOf(row));
                        WebElement icon = row.findElement(By.xpath("./ancestor::tr/td[6]//i"));
                        String scenarioStatus = icon.getAttribute("title");
                        if (scenarioStatus.equals("FAILURE")) {
                            String currentWindow = openInNewWindow(row);
                            try {
                                longWait.until(ExpectedConditions.visibilityOf(reportButton));
                                reportButton.click();
                                longWait.until(ExpectedConditions.visibilityOf(reportButton));
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    Logger.writeLog(e.getMessage(), "");
                                    e.printStackTrace();
                                }
                                allTestsReport.add(attachReport.getText());
                            } catch (WebDriverException e) {
                                e.printStackTrace();
                            }
                            driver.close();
                            driver.switchTo().window(currentWindow);
                        }
                    } catch (WebDriverException e) {
                        e.printStackTrace();
                    }
                }
                String nextButtonArgument = nextButton.getAttribute("class");
                if (!nextButtonArgument.contains("disabled")) {
                    nextButton.click();
                } else {
                    break;
                }
            }
        }
        return allTestsReport;
    }

    private String openInNewWindow(WebElement webElement) {
        String currentWindow = driver.getWindowHandle();
        webElement.sendKeys(selectLinkOpenInNewTab);
        Set<String> windowsList = driver.getWindowHandles();
        for (String window : windowsList) {
            if (!window.equals(currentWindow)) {
                driver.switchTo().window(window);
            }
        }
        return currentWindow;
    }

    private boolean check404() {
        return driver.getTitle().contains("404");
    }

    private String generateFileName() {
        return RandomStringUtils.random(8, true, true);
    }
}
