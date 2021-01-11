package ru.invitro.automation.notification.page;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindBys;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.ArrayList;
import java.util.List;

public class JenkinsPage extends Page {

    @FindBy(xpath = "//h1[@class='build-caption page-headline']")
    private WebElement buildNameHeader;

    @FindBys({@FindBy(xpath = "//img[contains(@src,'/graph.png')]/ancestor::a")})
    private List<WebElement> reports;

    public JenkinsPage(String url) {
        super(url, 5, 90, 2, 10);
    }

    public List<String> collectNodesReportUrl() {
        List<String> result = new ArrayList<>();
        driver.get(url);
        authorization();
        longWait.until(ExpectedConditions.visibilityOf(buildNameHeader));
        for (WebElement report : reports) {
            result.add(report.getAttribute("href"));
        }
        closePage();
        return result;
    }
}
