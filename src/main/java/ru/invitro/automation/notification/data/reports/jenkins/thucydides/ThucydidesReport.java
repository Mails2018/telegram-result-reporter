package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

import org.apache.commons.lang3.RandomStringUtils;
import ru.invitro.automation.notification.api.JenkinsApiConnector;
import ru.invitro.automation.notification.data.UrlConverter;
import ru.invitro.automation.notification.data.reports.ReportCollector;
import ru.invitro.automation.notification.data.reports.ReportStatus;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.tests.ThucydiesScenarioReport;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThucydidesReport {

    static Map<String, ReportStatus> reportsStatuses = new ConcurrentHashMap<>();

    protected String url;

    protected String id;

    boolean reportAvailable;

    String reportDate;

    String context;

    int unsuccessfulTestCount;

    int reportScenarios;

    private boolean isSmoke;

    private boolean jobBasedReport;

    private ReportStatus previousReportStatus;

    private JenkinsApiConnector jenkinsApiConnector;

    private String lastBuildStatus;

    private boolean reportIsNew;

    private boolean failIsNew;

    private boolean jenkinsAvailable;

    private boolean smokeCheck;

    private String reportName;

    private int pendingTests;

    private int brokenTests;

    private int failedTests;

    public ThucydidesReport(String url, boolean isSmoke, boolean jobBasedReport, String id) {
        this.url = url;
        this.isSmoke = isSmoke;
        reportAvailable = false;
        unsuccessfulTestCount = 0;
        this.id = id;
        this.jobBasedReport = jobBasedReport;
    }

    public static void removeFromMonitoring(String url) {
        reportsStatuses.remove(url);
    }

    void collectStatusData(String reportID) {
        previousReportStatus = getPreviousReportStatus(url);
        jenkinsApiConnector = new JenkinsApiConnector(url, reportID);
        lastBuildStatus = jenkinsApiConnector.getLastBuildResult();
        if (lastBuildStatus.equals("UNAVAILABLE")) {
            smokeCheck = false;
            reportIsNew = false;
            failIsNew = false;
        } else {
            smokeCheck = isSmoke && (lastBuildStatus.equals("SUCCESS") || lastBuildStatus.equals("FAILURE"));
            reportIsNew = reportIsNew();
            failIsNew = failIsNew(reportID);
        }
        jenkinsAvailable = !lastBuildStatus.equals("UNAVAILABLE");
    }

    public List checkAndGetResult() {
        List generatedReport = new ArrayList();
        String reportID = id + "-" + RandomStringUtils.random(6, true, true);
        Logger.writeLog("check " + url, reportID);
        collectStatusData(reportID);
        Logger.writeLog(UrlConverter.getJenkinsJobName(url)
            + " jenkinsAvailable:" + jenkinsAvailable
            + " failIsNew:" + failIsNew
            + " reportIsNew:" + reportIsNew
            + " FirstTime:" + !reportsStatuses.containsKey(url)
            + " LastBuild:" + lastBuildStatus, reportID);
        if (!reportsStatuses.containsKey(url)) {
            previousReportStatus.setAvailable(jenkinsAvailable);
            previousReportStatus.setSuccessfulTimestamp(getLastSuccessfulBuildTimeStamp());
            previousReportStatus.setFailTimestamp(jenkinsApiConnector.getLastBuildTimeStamp());
        } else if (!isSmoke && lastBuildStatus.equals("FAILURE") && failIsNew) {
            generatedReport = generateFailureTestsReport();
            previousReportStatus.setFailTimestamp(jenkinsApiConnector.getLastBuildTimeStamp());
        } else if (smokeCheck || lastBuildStatus.equals("SUCCESS")) {
            if (reportIsNew()) {
                Logger.writeLog("start collect " + UrlConverter.getJenkinsJobName(url), reportID);
                if (jobBasedReport) {
                    String coreReportUrl = url;
                    String lastSuccessfulBuildReportUrl = jenkinsApiConnector.getLastSuccessfulBuild().getUrl() + "thucydidesReport/";
                    Logger.writeLog("lastSuccessfulBuildReportUrl " + lastSuccessfulBuildReportUrl, reportID);
                    this.url = lastSuccessfulBuildReportUrl;
                    ReportCollector.collectReport(this, reportID);
                    this.url = coreReportUrl;
                } else {
                    ReportCollector.collectReport(this, reportID);
                }
                if (reportAvailable) {
                    previousReportStatus.setSuccessfulTimestamp(getLastSuccessfulBuildTimeStamp());
                    generatedReport = generateTestsReport();
                } else if (previousReportStatus.isAvailable()) {
                    generatedReport = generateTestsReport();
                }
                previousReportStatus.setAvailable(reportAvailable);
            } else if (!previousReportStatus.isAvailable()) {
                ReportCollector.collectReport(this, reportID);
                if (reportAvailable) {
                    generatedReport = generateRecoveryTestsReport();
                    previousReportStatus.setAvailable(true);
                    previousReportStatus.setSuccessfulTimestamp(jenkinsApiConnector.getLastBuild().getTimestamp().toString());
                }
            }
        } else if (lastBuildStatus.equals("UNAVAILABLE") && previousReportStatus.isAvailable()) {
            generatedReport = generateUnavailableTestsReport();
            previousReportStatus.setAvailable(false);
        }
        reportsStatuses.put(url, previousReportStatus);
        return generatedReport;
    }

    String getLastSuccessfulBuildTimeStamp() {
        return isSmoke ? jenkinsApiConnector.getLastBuildTimeStamp() : jenkinsApiConnector.getLastSuccessfulBuildTimeStamp();
    }

    private ReportStatus getPreviousReportStatus(String url) {
        if (reportsStatuses.containsKey(url)) {
            return reportsStatuses.get(url);
        } else {
            return new ReportStatus();
        }
    }

    boolean reportIsNew() {
        if (!reportsStatuses.containsKey(url)) {
            return true;
        }
        String newBuildTimeStamp = getLastSuccessfulBuildTimeStamp();
        String oldBuildTimeStamp = reportsStatuses.get(url).getSuccessfulTimestamp();
        return !(newBuildTimeStamp.equals(oldBuildTimeStamp));
    }

    private boolean failIsNew(String reportID) {
        String print = "Check fail is new " + url + " first time: " + !reportsStatuses.containsKey(url) + " ";
        if (!reportsStatuses.containsKey(url)) {
            Logger.writeLog(print, reportID);
            return true;
        }
        String newBuildTimeStamp = jenkinsApiConnector.getLastBuildTimeStamp();
        String oldBuildTimeStamp = reportsStatuses.get(url).getFailTimestamp();
        Logger.writeLog(print + newBuildTimeStamp + " " + oldBuildTimeStamp + " " + !newBuildTimeStamp.equals(oldBuildTimeStamp), id);
        return !(newBuildTimeStamp.equals(oldBuildTimeStamp));
    }

    List<String> generateRecoveryTestsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(reportHeader());
        sb.append(url).append("\r\n");
        sb.append("Report availability restored.");
        List<String> report = new ArrayList<>();
        report.add(sb.toString());
        return report;
    }

    List<String> generateUnavailableTestsReport() {
        List<String> report = new ArrayList<>();
        report.add(unavailableMessage());
        return report;
    }

    List<String> generateFailureTestsReport() {
        List<String> report = new ArrayList<>();
        report.add(failureMessage());
        return report;
    }

    public List generateTestsReport() {
        List<String> generatedReport = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (!reportAvailable) {
            generatedReport.add(unavailableMessage());
        } else {
            sb.append(reportHeader());
            if (unsuccessfulTestCount > 0) {
                sb.append(unsuccessfulTestReport());
                sb.append("\r\n").append("\r\n");
                sb.append("Report: ");
                sb.append(url).append("\r\n");
            } else {
                sb.append(successMessage());
            }
            generatedReport.add(sb.toString());
        }
        return generatedReport;
    }

    String unavailableMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b> Report unavailable: ").append("</b>").append("\r\n");
        sb.append(url).append("\r\n");
        return sb.toString();
    }

    String failureMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Build " + jenkinsApiConnector.getLastBuild().getNumber() + " FAILURE\n" + UrlConverter.getJenkinsJobLink(url));
        sb.append("\r\n");
        return sb.toString();
    }

    String reportHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project name: <b>").append(reportName).append("</b>").append("\r\n");
        sb.append("<b>").append(reportDate).append("</b>").append("\r\n");
        sb.append("<b>").append("Total scenarios: ").append(reportScenarios).append("</b>").append("\r\n");
        sb.append("\r\n");
        if (!context.equals("")) {
            sb.append("Environment: <b>").append(context).append("</b>").append("\r\n");
            sb.append("\r\n");
        }
        return sb.toString();
    }

    String unsuccessfulTestReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Number of failed scenarios: ").append(unsuccessfulTestCount).append(" </b>( ");
        Map<ThucydiesScenarioReport.ErrorType, Integer> errorsCount = getErrorsType();
        for (ThucydiesScenarioReport.ErrorType errorType : errorsCount.keySet()) {
            sb.append(" ").append(errorType.name()).append(": ").append(errorsCount.get(errorType)).append(" ");
        }
        sb.append(")");
        return sb.toString();
    }

    String successMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>With out errors").append("</b>").append("\r\n");
        sb.append(url).append("\r\n");
        return sb.toString();
    }

    private Map<ThucydiesScenarioReport.ErrorType, Integer> getErrorsType() {
        Map<ThucydiesScenarioReport.ErrorType, Integer> result = new HashMap<>();
        if (failedTests > 0)
            result.put(ThucydiesScenarioReport.ErrorType.FAILED, failedTests);
        if (brokenTests > 0)
            result.put(ThucydiesScenarioReport.ErrorType.BROKEN, brokenTests);
        if (pendingTests > 0)
            result.put(ThucydiesScenarioReport.ErrorType.PENDING, pendingTests);
        return result;
    }

    public void calculateUnsuccessfulTestCount() {
        unsuccessfulTestCount = failedTests + brokenTests + pendingTests;
    }

    public void setReportAvailable(boolean reportAvailable) {
        this.reportAvailable = reportAvailable;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public void setPendingTests(int pendingTests) {
        this.pendingTests = pendingTests;
    }

    public void setBrokenTests(int brokenTests) {
        this.brokenTests = brokenTests;
    }

    public void setFailedTests(int failedTests) {
        this.failedTests = failedTests;
    }

    public boolean isSmoke() {
        return isSmoke;
    }

    public String getUrl() {
        return url;
    }

    String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getUnsuccessfulTestCount() {
        return unsuccessfulTestCount;
    }

    public void setUnsuccessfulTestCount(int unsuccessfulTestCount) {
        this.unsuccessfulTestCount = unsuccessfulTestCount;
    }

    int getReportScenarios() {
        return reportScenarios;
    }

    public void setReportScenarios(int reportScenarios) {
        this.reportScenarios = reportScenarios;
    }

    String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public File createFile(String fileName, String fileData) {
        File file = new File(fileName);
        try {
            BufferedWriter writerFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            writerFile.write(fileData);
            writerFile.close();
        } catch (IOException e) {
            Logger.writeLog("Error in file create " + fileName + "\n" + e.getMessage(), id);
            e.printStackTrace();
        }
        return file;
    }

    public String fileNameWithDate(String filePathPattern) {
        Pattern pattern = Pattern.compile("([\\d-]+) ");
        Matcher matcher = pattern.matcher(getReportDate());
        String date = "";
        if (matcher.find()) {
            date = matcher.group(1);
        }
        return filePathPattern.replace("%date", date + " " + generateID());
    }

    public String fileNameWithDateTime(String filePathPattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yy HH-mm");
        String currentTime = dateTimeFormatter.format(LocalDateTime.now());
        return filePathPattern.replace("%date", currentTime + " " + generateID());
    }

    private String generateID() {
        return RandomStringUtils.random(6, false, true);
    }
}
