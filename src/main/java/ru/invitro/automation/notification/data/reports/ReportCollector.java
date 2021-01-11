package ru.invitro.automation.notification.data.reports;

import org.openqa.selenium.WebDriverException;
import ru.invitro.automation.notification.api.JenkinsApiConnector;
import ru.invitro.automation.notification.data.UrlConverter;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.AttachThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.FullThucydidesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.MonitoringWithImagesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.PipeLineNodeReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.PipeLineThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.PriceCheckThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.ThucydidesReport;
import ru.invitro.automation.notification.page.JenkinsPage;
import ru.invitro.automation.notification.page.ReportMainPage;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class ReportCollector {

    private ReportCollector() {
    }

    public static void collectReport(ThucydidesReport report, String operationID) {
        String url = report.getUrl();
        ReportMainPage reportMainPage = new ReportMainPage(url, operationID);
        try {
            if (report instanceof PipeLineThucydiesReport) {
                List<PipeLineNodeReport> nodesReports = collectPipelineReport(report, operationID);
                ((PipeLineThucydiesReport) report).setNodesReports(nodesReports);
                return;
            }
            if (report instanceof MonitoringWithImagesReport) {
                reportMainPage.getFullFailReport((MonitoringWithImagesReport) report);
                return;
            }
            if (report instanceof FullThucydidesReport) {
                reportMainPage.getFullFailReport((FullThucydidesReport) report);
                return;
            }
            if (report instanceof PriceCheckThucydiesReport) {
                reportMainPage.getXmlPriceReport((PriceCheckThucydiesReport) report);
                return;
            }
            if (report instanceof AttachThucydiesReport) {
                reportMainPage.getXmlPriceReport((AttachThucydiesReport) report);
                return;
            }
            reportMainPage.collectBaseReportData(report);
            Logger.writeLog("finish collect " + UrlConverter.getJenkinsJobName(url), operationID);
        } catch (WebDriverException e) {
            Logger.writeLog("Report " + url + " collect error\n" + e.getMessage(), operationID);
            e.printStackTrace();
            report.setReportAvailable(false);
        } finally {
            reportMainPage.closePage();
        }
    }

    private static List<PipeLineNodeReport> collectPipelineReport(ThucydidesReport job, String operationID) {
        CopyOnWriteArrayList<PipeLineNodeReport> result = new CopyOnWriteArrayList<>();
        JenkinsApiConnector jenkinsApiConnector = new JenkinsApiConnector(job.getUrl(), operationID);
        String lastSuccessfulBuildUrl = job.isSmoke() ? jenkinsApiConnector.getLastBuild().getUrl() : jenkinsApiConnector.getLastSuccessfulBuild().getUrl();
        JenkinsPage buildPage = new JenkinsPage(lastSuccessfulBuildUrl);
        List<String> nodesReportUrlsList = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            try {
                nodesReportUrlsList = buildPage.collectNodesReportUrl();
                break;
            } catch (WebDriverException ignore) {
            }
        List<String> nodesReportUrls = nodesReportUrlsList;
        if (nodesReportUrls.size() > 0) {
            job.setReportAvailable(true);
        }
        int count = 0;
        while (count < nodesReportUrls.size()) {
            int threads = nodesReportUrls.size() - count > 3 ? 4 : nodesReportUrls.size() - count;
            CountDownLatch latch = new CountDownLatch(threads);
            for (int i = 0; i < threads; i++) {
                int currentCount = count + i;
                Thread thread = new Thread(() -> {
                    int getReportFromPageCount = 0;
                    String currentNodeReportUrl = nodesReportUrls.get(currentCount);
                    PipeLineNodeReport failReport = new PipeLineNodeReport(currentNodeReportUrl, false, operationID);
                    while (getReportFromPageCount < 3) {
                        try {
                            collectReport(failReport, operationID);
                            break;
                        } catch (WebDriverException e) {
                            getReportFromPageCount++;
                        }
                    }
                    result.add(failReport);
                    latch.countDown();
                });
                thread.start();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
            }
            count += threads;
        }
        return result;
    }
}
