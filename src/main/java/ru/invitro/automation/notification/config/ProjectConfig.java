package ru.invitro.automation.notification.config;

import com.google.gson.JsonIOException;
import org.jvnet.hk2.annotations.Optional;
import ru.invitro.automation.notification.data.UrlConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectConfig {

    private String reportName;

    private ReportType reportType;

    private Boolean checkLaunch = false;

    private Boolean smoke = false;

    @Optional
    private Boolean jobNumberBasedReport = false;

    private Integer period = 60000;

    private List<String> urls = new ArrayList<>();

    private List<String> chatsId = new ArrayList<>();

    @Optional
    private Integer failMonitoringPeriod;

    public static ProjectConfig getDefaultConfig(String reportTypeName, String reportName) {
        switch (ReportType.valueOf(reportTypeName)) {
            case API:
                return defaultApiProject(reportName);
            case PRICE:
                return defaultPriceProject(reportName);
            case PIPELINE:
                return defaultPipelineProject(reportName);
            case STANDARD:
                return defaultStandardProject(reportName);
            case WITH_TXT_FILE:
                return defaultWithTxtFile(reportName);
            case CONTINUOUS_MONITORING:
                return defaultMonitoring(reportName);
            default:
                throw new JsonIOException("Wrong project type " + reportTypeName);
        }
    }

    private static ProjectConfig defaultApiProject(String reportName) {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.setReportName(reportName);
        projectConfig.setReportType(ReportType.API);
        projectConfig.setPeriod(180000);
        return projectConfig;
    }

    private static ProjectConfig defaultPriceProject(String reportName) {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.setReportName(reportName);
        projectConfig.setReportType(ReportType.PRICE);
        projectConfig.setPeriod(180000);
        return projectConfig;
    }

    private static ProjectConfig defaultPipelineProject(String reportName) {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.setReportName(reportName);
        projectConfig.setReportType(ReportType.PIPELINE);
        projectConfig.setPeriod(360000);
        return projectConfig;
    }

    private static ProjectConfig defaultStandardProject(String reportName) {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.setReportName(reportName);
        projectConfig.setReportType(ReportType.STANDARD);
        return projectConfig;
    }

    private static ProjectConfig defaultWithTxtFile(String reportName) {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.setReportName(reportName);
        projectConfig.setReportType(ReportType.WITH_TXT_FILE);
        return projectConfig;
    }

    private static ProjectConfig defaultMonitoring(String reportName) {
        ProjectConfig projectConfig = new ProjectConfig();
        projectConfig.setReportName(reportName);
        projectConfig.setReportType(ReportType.CONTINUOUS_MONITORING);
        projectConfig.setPeriod(60000);
        projectConfig.setJobNumberBasedReport(true);
        projectConfig.setFailMonitoringPeriod(30);
        return projectConfig;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public List<String> getUrls() {
        return urls;
    }

    public List<String> getJobNames() {
        List<String> result = new ArrayList<>();
        for (String url:urls){
            result.add(UrlConverter.getJenkinsJobName(url));
        }
        return result;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public List<Long> getChatsId() {
        return chatsId.stream().map(Long::valueOf).collect(Collectors.toList());
    }

    public void setChatsId(List<Long> chatsId) {
        this.chatsId = chatsId.stream().map(String::valueOf).collect(Collectors.toList());
    }

    public Boolean getCheckLaunch() {
        return checkLaunch;
    }

    public void setCheckLaunch(Boolean checkLaunch) {
        this.checkLaunch = checkLaunch;
    }

    public Boolean isSmoke() {
        return smoke;
    }

    public void setSmoke(Boolean smoke) {
        this.smoke = smoke;
    }

    public void setJobNumberBasedReport(Boolean jobNumberBasedReport) {
        this.jobNumberBasedReport = jobNumberBasedReport;
    }

    public Boolean jobNumberBasedReport() {
        return jobNumberBasedReport;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getFailMonitoringPeriod() {
        return failMonitoringPeriod;
    }

    public void setFailMonitoringPeriod(Integer failMonitoringPeriod) {
        this.failMonitoringPeriod = failMonitoringPeriod;
    }

    public void setParam(ProjectOptions option, Object value) {
        switch (option) {
            case CHECK_LAUNCH:
                if (value instanceof Boolean) {
                    this.checkLaunch = (Boolean) value;
                } else {
                    throw new RuntimeException("Value " + value + " not Boolean");
                }
                break;
            case SMOKE:
                if (value instanceof Boolean) {
                    this.smoke = (Boolean) value;
                } else {
                    throw new RuntimeException("Value " + value + " not Boolean");
                }
                break;
            case JOB_NUMBER_BASED_REPORT:
                if (value instanceof Boolean) {
                    this.jobNumberBasedReport = (Boolean) value;
                } else {
                    throw new RuntimeException("Value " + value + " not Boolean");
                }
                break;
            case PERIOD:
                if (value instanceof Integer) {
                    this.period = (Integer) value * 60000;
                } else {
                    throw new RuntimeException("Value " + value + " not Integer");
                }
                break;
            case FAIL_MONITORING_PERIOD:
                if (value instanceof Integer) {
                    this.failMonitoringPeriod = (Integer) value;
                } else {
                    throw new RuntimeException("Value " + value + " not Integer");
                }
                break;
            default:
                throw new RuntimeException("Wrong option type" + option.name());
        }
    }

    public List<ProjectOptions> getProjectOptions() {
        List<ProjectOptions> optionsList = new ArrayList<>();
        if (Objects.nonNull(checkLaunch)) {
            optionsList.add(ProjectOptions.CHECK_LAUNCH);
        }
        if (Objects.nonNull(smoke)) {
            optionsList.add(ProjectOptions.SMOKE);
        }
        if (Objects.nonNull(jobNumberBasedReport)) {
            optionsList.add(ProjectOptions.JOB_NUMBER_BASED_REPORT);
        }
        if (Objects.nonNull(period)) {
            optionsList.add(ProjectOptions.PERIOD);
        }
        if (Objects.nonNull(failMonitoringPeriod)) {
            optionsList.add(ProjectOptions.FAIL_MONITORING_PERIOD);
        }
        return optionsList;
    }

    public String getOptionValue(ProjectOptions option) {
        switch (option) {
            case CHECK_LAUNCH:
                return checkLaunch.toString();
            case SMOKE:
                return smoke.toString();
            case JOB_NUMBER_BASED_REPORT:
                return jobNumberBasedReport.toString();
            case PERIOD:
                return String.valueOf(period / 60000);
            case FAIL_MONITORING_PERIOD:
                return String.valueOf(failMonitoringPeriod);
            default:
                throw new RuntimeException("Wrong option type " + option.name());
        }
    }

    public String currentConfig() {
        String currentConfig = reportName;
        if (Objects.nonNull(checkLaunch)) {
            currentConfig += "\ncheckLaunch: " + checkLaunch;
        }
        if (Objects.nonNull(smoke)) {
            currentConfig += "\nsmoke: " + smoke;
        }
        if (Objects.nonNull(jobNumberBasedReport)) {
            currentConfig += "\njobNumberBasedReport: " + jobNumberBasedReport;
        }
        if (Objects.nonNull(period)) {
            currentConfig += "\nperiod: " + period / 60000;
        }
        if (Objects.nonNull(failMonitoringPeriod)) {
            currentConfig += "\nfailMonitoringPeriod: " + failMonitoringPeriod;
        }
        return currentConfig;
    }

    @Override
    public String toString() {
        return "ReportConfig{" +
            "reportName='" + reportName + '\'' +
            ", reportType=" + reportType +
            ", period=" + period +
            ", failMonitoringPeriod=" + failMonitoringPeriod +
            ", checkLaunch=" + checkLaunch +
            ", smoke=" + smoke +
            ", jobNumberBasedReport=" + jobNumberBasedReport +
            ", urls=" + urls +
            ", chatsId=" + chatsId +
            "}\r\n";
    }
}
