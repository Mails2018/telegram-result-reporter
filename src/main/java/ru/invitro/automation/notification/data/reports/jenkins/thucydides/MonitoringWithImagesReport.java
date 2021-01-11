package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

import ru.invitro.automation.notification.data.reports.ReportCollector;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.messages.MessageWithImage;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.tests.ThucydiesScenarioReport;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MonitoringWithImagesReport extends FullThucydidesReport {

    private List<ThucydiesScenarioReport> failTestsList;

    public MonitoringWithImagesReport(String url, boolean isSmoke, boolean jobBasedReport, String id) {
        super(url, isSmoke, jobBasedReport, id);
    }

    @Override
    public List generateTestsReport() {
        List<Object> generatedReport = new ArrayList<>();
        if (!reportAvailable) {
            generatedReport.add(unavailableMessage());
        } else {
            if (unsuccessfulTestCount > 0) {
                for (ThucydiesScenarioReport thucydiesScenarioReport : failTestsList) {
                    MessageWithImage messageWithImage = new MessageWithImage();
                    StringBuilder testSummary = new StringBuilder();
                    String scenarioName = thucydiesScenarioReport.getTestName().replace(context.toUpperCase(), "");
                    testSummary.append(scenarioName).append("\r\n");
                    testSummary.append("<b>").append(context.toLowerCase()).append("</b>").append("\r\n");
                    testSummary.append("Step: <b>").append(thucydiesScenarioReport.getFailedStepName().replaceAll("^[^ ]+ ", "")).append("</b>").append("\r\n");
                    Pattern pattern = Pattern.compile("\\*{5}((\\n|.)*)\\*{5}");
                    Matcher matcher = pattern.matcher(thucydiesScenarioReport.getAttach());
                    if (matcher.find()) {
                        testSummary.append("\r\n");
                        for (String info : matcher.group(1).split(";")) {
                            testSummary.append(info).append("\r\n");
                        }
                        testSummary.append("\r\n");
                    }
                    testSummary.append(thucydiesScenarioReport.getTestUrl()).append("\r\n").append("\r\n").append("\r\n");
                    messageWithImage.setMessage(testSummary.toString());
                    if (thucydiesScenarioReport.getImage() != null) {
                        messageWithImage.setImage(thucydiesScenarioReport.getImage());
                    }
                    if (thucydiesScenarioReport.getAttach() != null) {
                        String fileName = fileNameWithDateTime("log %date.txt");
                        messageWithImage.setAttach(createFile(fileName, thucydiesScenarioReport.getAttach().replaceAll("\\*{5}(\\n|.)*\\*{5}", "")));
                    }
                    generatedReport.add(messageWithImage);
                }
            }
        }
        return generatedReport;
    }

    public void setFailTestsList(List<ThucydiesScenarioReport> testsList) {
        this.failTestsList = testsList;
    }

    public MonitoringWithImagesReport reportForTest(String job) {
        collectStatusData("test");
        String coreUrl = url;
        this.url = job;
        ReportCollector.collectReport(this, "test");
        this.url = coreUrl;
        return this;
    }
}
