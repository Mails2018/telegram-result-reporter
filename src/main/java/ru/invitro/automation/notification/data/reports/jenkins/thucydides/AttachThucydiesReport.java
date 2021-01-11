package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AttachThucydiesReport extends ThucydidesReport {

    List<String> xmlReport;

    public AttachThucydiesReport(String url, boolean isSmoke, boolean jobBasedReport, String id) {
        super(url, isSmoke, jobBasedReport, id);
    }

    @Override
    public List generateTestsReport() {
        List<Object> generatedReport = new ArrayList<>();
        List<String> message = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (!reportAvailable) {
            message.add(unavailableMessage());
        } else {
            sb.append(reportHeader());
            if (unsuccessfulTestCount > 0) {
                sb.append(unsuccessfulTestReport());
                sb.append("\r\n").append("\r\n");
                if (xmlReport == null || xmlReport.size() == 0) {
                    sb.append("Attached file access error").append("\r\n");
                    sb.append("\r\n");
                }
                sb.append(url).append("\r\n");
                sb.append("\r\n");
            } else {
                sb.append(successMessage());
            }
            message.add(sb.toString());
        }
        if (message.size() > 0) {
            generatedReport.addAll(message);
        }
        List<File> files = getFiles();
        if (files.size() > 0) {
            generatedReport.addAll(files);
        }
        return generatedReport;
    }

    public void setXmlReport(List<String> xmlReport) {
        this.xmlReport = xmlReport;
    }

    protected List<File> getFiles() {
        List<File> files = new ArrayList<>();
        String reportAttach = getFileReport();
        if (reportAttach != null) {
            File failureFile = createFile(fileNameWithDate("Failure report %date.txt"), reportAttach);
            files.add(failureFile);
        }
        return files;
    }

    private String getFileReport() {
        StringBuilder sb = new StringBuilder();
        if (xmlReport != null && xmlReport.size() > 0) {
            for (String part : xmlReport) {
                sb.append("\r\n");
                sb.append(part);
            }
            return sb.toString();
        } else {
            return null;
        }
    }
}
