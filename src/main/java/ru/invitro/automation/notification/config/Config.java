package ru.invitro.automation.notification.config;

import ru.invitro.automation.notification.config.admins.Request;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Config {

    private String TelegramBotToken;

    private String TelegramBotName;

    private String ProxyServer;

    private Integer ProxyPort = 0;

    private String ProxyUser;

    private String ProxyPassword;

    private String ProxyType;

    private ConcurrentHashMap<String, List<String>> JenkinsApiTokens = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, ProjectConfig> reports = new ConcurrentHashMap<>();

    private Set<Request> Admins = ConcurrentHashMap.newKeySet();

    private Integer KeepLogsInDays = 10;

    String getTelegramBotToken() {
        return TelegramBotToken;
    }

    public void setTelegramBotToken(String telegramBotToken) {
        TelegramBotToken = telegramBotToken;
    }

    String getTelegramBotName() {
        return TelegramBotName;
    }

    public void setTelegramBotName(String telegramBotName) {
        TelegramBotName = telegramBotName;
    }

    public String getProxyServer() {
        return ProxyServer;
    }

    public void setProxyServer(String proxyServer) {
        ProxyServer = proxyServer;
    }

    public Integer getProxyPort() {
        return ProxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        ProxyPort = proxyPort;
    }

    public String getProxyUser() {
        return ProxyUser;
    }

    public void setProxyUser(String proxyUser) {
        ProxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return ProxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        ProxyPassword = proxyPassword;
    }

    public String getProxyType() {
        return ProxyType;
    }

    public void setProxyType(String proxyType) {
        ProxyType = proxyType;
    }

    public ConcurrentHashMap<String, ProjectConfig> getReports() {
        return reports;
    }

    public void setReports(ConcurrentHashMap<String, ProjectConfig> reports) {
        this.reports = reports;
    }

    public void addReport(ProjectConfig report) {
        this.reports.put(report.getReportName(), report);
    }

    ConcurrentHashMap<String, List<String>> getJenkinsApiTokens() {
        return JenkinsApiTokens;
    }

    public void addJenkinsApiTokens(String url, List<String> auth) {
        JenkinsApiTokens.put(url, auth);
    }

    public Integer getKeepLogsInDays() {
        return KeepLogsInDays;
    }

    public void setKeepLogsInDays(Integer keepLogsInDays) {
        KeepLogsInDays = keepLogsInDays;
    }

    public Set<Request> getAdmins() {
        return Admins;
    }

    public void setAdmins(Set<Request> admins) {
        Admins = admins;
    }

    public void addAdmin(Request request) {
        Admins.add(request);
    }

    public void removeAdmin(Request request) {
        Admins.remove(request);
    }

    public Boolean isAdmin(Long id) {
        Request request = new Request();
        request.setId(id);
        return Admins.contains(request);
    }

    public String printAdmins() {
        StringBuilder result = new StringBuilder();
        for (Request admin : Admins) {
            result.append(admin.toString());
            result.append("\n\n");
        }
        return result.toString();
    }

    public Request getAdmin(Long id) {
        for (Request admin : Admins) {
            if (admin.getId().equals(id)) {
                return admin;
            }
        }
        return null;
    }

    public String printAdmin(Long id) {
        Request request = getAdmin(id);
        if (Objects.nonNull(request)) {
            return request.toString();
        } else {
            return "Request with id " + id + " not found";
        }
    }

    @Override
    public String toString() {
        return "Config{" +
            "TelegramBotToken='" + TelegramBotToken + '\'' +
            ", TelegramBotName='" + TelegramBotName + '\'' +
            ", ProxyServer='" + ProxyServer + '\'' +
            ", ProxyPort=" + ProxyPort +
            ", ProxyUser='" + ProxyUser + '\'' +
            ", ProxyPassword='" + ProxyPassword + '\'' +
            ", ProxyType='" + ProxyType + '\'' +
            ", KeepLogsInDays='" + KeepLogsInDays + '\'' +
            ", Admins='" + Admins + '\'' +
            ", JenkinsApiTokens='" + JenkinsApiTokens + '\'' +
            ",\r\n reports=\r\n" + reports +
            '}';
    }

    public void synchronizeProjectName() {
        for (String projectName : reports.keySet()) {
            reports.get(projectName).setReportName(projectName);
        }
    }
}
