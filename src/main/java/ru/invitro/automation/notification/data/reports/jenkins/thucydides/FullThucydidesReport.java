package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

import ru.invitro.automation.notification.data.reports.jenkins.thucydides.tests.ThucydiesScenarioReport;

import java.util.ArrayList;
import java.util.List;

public class FullThucydidesReport extends ThucydidesReport {

    private List<ThucydiesScenarioReport> failTestsList;

    public FullThucydidesReport(String url, boolean isSmoke, boolean jobBasedReport, String id) {
        super(url, isSmoke, jobBasedReport, id);
    }

    @Override
    public List generateTestsReport() {
        List<Object> generatedReport = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (!reportAvailable) {
            generatedReport.add(unavailableMessage());
        } else {
            sb.append(reportHeader());
            if (unsuccessfulTestCount > 0) {
                sb.append(unsuccessfulTestReport());
                for (ThucydiesScenarioReport thucydiesScenarioReport : failTestsList) {
                    StringBuilder testSummary = new StringBuilder();
                    testSummary.append(thucydiesScenarioReport.getTestName()).append("\r\n");
                    testSummary.append("Step: <b>").append(thucydiesScenarioReport.getFailedStepName()).append("</b>").append("\r\n");
                    testSummary.append(thucydiesScenarioReport.getTestUrl()).append("\r\n").append("\r\n").append("\r\n");
                    if (sb.length() + testSummary.length() > 4000) {
                        generatedReport.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    sb.append(testSummary);
                }
            } else {
                sb.append(successMessage());
            }
            generatedReport.add(sb.toString());
        }
        return generatedReport;
    }

    public void setFailTestsList(List<ThucydiesScenarioReport> testsList) {
        this.failTestsList = testsList;
    }
}
