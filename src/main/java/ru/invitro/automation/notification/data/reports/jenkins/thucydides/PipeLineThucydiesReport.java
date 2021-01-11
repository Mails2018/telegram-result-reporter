package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

import ru.invitro.automation.notification.data.UrlConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipeLineThucydiesReport extends ThucydidesReport {

    private List<PipeLineNodeReport> nodesReports;

    public PipeLineThucydiesReport(String url, boolean isSmoke, String id) {
        super(url, isSmoke, false, id);
    }

    @Override
    public List generateTestsReport() {
        List<String> generatedReport = new ArrayList<>();
        if (nodesReports.size() > 0) {
            Map<String, PipeLineNodeReport> reportsMap = new HashMap<>();
            List<String> sortedList = new ArrayList<>();
            for (PipeLineNodeReport report : nodesReports) {
                reportsMap.put(report.getContext(), report);
                if (report.getContext() == null) {
                    sortedList.add(report.getUrl());
                } else {
                    sortedList.add(report.getContext());
                }
            }
            Collections.sort(sortedList);
            StringBuilder message = new StringBuilder();
            message.append("Project name: <b>").append(UrlConverter.getJenkinsJobName(url)).append("</b>").append("\r\n");
            message.append("\r\n");
            int failCount = 0;
            int totalScenarioCount = 0;
            for (String reportContext : sortedList) {
                StringBuilder nodeReport = new StringBuilder();
                PipeLineNodeReport report = reportsMap.get(reportContext);
                try {
                    nodeReport.append(report.generateReportForPipeline());
                    nodeReport.append("\r\n");
                    failCount += report.getUnsuccessfulTestCount();
                    totalScenarioCount += report.getReportScenarios();
                } catch (NullPointerException e) {
                    nodeReport.append(reportContext);
                    nodeReport.append("\r\n");
                    nodeReport.append("Report unavailable");
                    nodeReport.append("\r\n");
                    nodeReport.append("\r\n");
                }
                if (message.length() + nodeReport.length() > 4000) {
                    generatedReport.add(message.toString());
                    message = nodeReport;
                } else {
                    message.append(nodeReport);
                }
            }
            if (message.length() > 3800) {
                generatedReport.add(message.toString());
                message = new StringBuilder();
            }
            message.append("Failed scenarios: <b>").append(failCount).append("</b>").append("\r\n");
            message.append("Total scenarios: <b>").append(totalScenarioCount).append("</b>").append("\r\n");
            generatedReport.add(message.toString());
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("<b> Unavailable report: ").append("</b>").append("\r\n");
            sb.append(url);
            generatedReport.add(sb.toString());
        }
        return generatedReport;
    }

    public void setNodesReports(List<PipeLineNodeReport> nodesReports) {
        this.nodesReports = nodesReports;
    }

    @Override
    List<String> generateRecoveryTestsReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Project name: <b>").append(UrlConverter.getJenkinsJobName(url)).append("</b>").append("\r\n");
        sb.append("\r\n");
        sb.append(url).append("\r\n");
        sb.append("Report availability restored.");
        List<String> report = new ArrayList<>();
        report.add(sb.toString());
        return report;
    }
}
