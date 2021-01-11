package ru.invitro.automation.notification.data.reports.jenkins.thucydides.factory;

import ru.invitro.automation.notification.Main;
import ru.invitro.automation.notification.config.ProjectConfig;
import ru.invitro.automation.notification.config.ReportType;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.AttachThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.FullThucydidesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.MonitoringWithImagesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.PipeLineThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.PriceCheckThucydiesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.ThucydidesReport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ReportsFactory {

    private ReportsFactory() {
    }

    public static List<ThucydidesReport> getReportList(ProjectConfig projectConfig, String operationID) {
        deleteOldReportFiles();
        List<ThucydidesReport> reportList = new ArrayList<>();
        for (String url : projectConfig.getUrls()) {
            if (projectConfig.getReportType().equals(ReportType.STANDARD)) {
                reportList.add(
                    new ThucydidesReport(
                        url,
                        projectConfig.isSmoke(),
                        projectConfig.jobNumberBasedReport(),
                        operationID));
                continue;
            }
            if (projectConfig.getReportType().equals(ReportType.API)) {
                reportList.add(
                    new FullThucydidesReport(
                        url,
                        projectConfig.isSmoke(),
                        projectConfig.jobNumberBasedReport(),
                        operationID));
                continue;
            }
            if (projectConfig.getReportType().equals(ReportType.CONTINUOUS_MONITORING)) {
                reportList.add(
                    new MonitoringWithImagesReport(
                        url,
                        projectConfig.isSmoke(),
                        projectConfig.jobNumberBasedReport(),
                        operationID));
                continue;
            }
            if (projectConfig.getReportType().equals(ReportType.PIPELINE)) {
                reportList.add(new PipeLineThucydiesReport(
                    url,
                    projectConfig.isSmoke(),
                    operationID));
                continue;
            }
            if (projectConfig.getReportType().equals(ReportType.WITH_TXT_FILE)) {
                reportList.add(new AttachThucydiesReport(
                    url,
                    projectConfig.isSmoke(),
                    projectConfig.jobNumberBasedReport(),
                    operationID));
                continue;
            }
            if (projectConfig.getReportType().equals(ReportType.PRICE)) {
                reportList.add(new PriceCheckThucydiesReport(
                    url,
                    projectConfig.isSmoke(),
                    projectConfig.jobNumberBasedReport(),
                    operationID));
            }
        }
        return reportList;
    }

    private static void deleteOldReportFiles() {
        String[] extentions = {".png", ".txt", ".xml"};
        for (String extention : extentions) {
            List<File> files = Main.getFilesList(extention);
            for (File file : files) {
                BasicFileAttributes attrs;
                try {
                    attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    FileTime time = attrs.creationTime();
                    LocalDateTime fileDate = LocalDateTime.ofInstant(time.toInstant(), ZoneId.systemDefault());
                    if (!file.getName().contains("pom.xml")) {
                        if (fileDate.plusHours(2).isBefore(LocalDateTime.now())) {
                            file.delete();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
