package ru.invitro.automation.notification.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import ru.invitro.automation.notification.config.admins.Request;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigReader {

    private static final String CONFIGFILE = "config.json";

    public static Config config;

    public static boolean readConfigFile() {
        try (
            FileInputStream fis = new FileInputStream(CONFIGFILE);
            Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            config = new Gson().fromJson(reader, Config.class);
            config.synchronizeProjectName();
            Logger.writeLog("Current config:\n" + config, "main");
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            Logger.writeLog("Config read error\n" + e.getMessage(), "main");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void writeConfigFile() throws IOException, JsonIOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(CONFIGFILE)) {
            gson.toJson(config, writer);
        }
    }

    public static ConcurrentHashMap<String, ProjectConfig> getProjects() {
        return config.getReports();
    }

    public static String getTelegramBotName() {
        return config.getTelegramBotName();
    }

    public static String getTelegramBotToken() {
        return config.getTelegramBotToken();
    }

    public static ConcurrentHashMap<String, List<String>> getJenkinsApiTokens() {
        return config.getJenkinsApiTokens();
    }

    public static void addJenkinsApiTokens(String url, String login, String token) {
        String[] auth = {login, token};
        config.addJenkinsApiTokens(url, new ArrayList<>(Arrays.asList(auth)));
    }

    public static Integer keepLogs() {
        return config.getKeepLogsInDays();
    }

    public static void addDefaultProject(String reportTypeName, String reportName) {
        config.addReport(ProjectConfig.getDefaultConfig(reportTypeName, reportName));
    }

    public static Boolean isAdmin(Long id) {
        readConfigFile();
        return config.isAdmin(id);
    }

    public static Set<Request> getAdmins() {
        return config.getAdmins();
    }

    public static String printAdmins() {
        return config.printAdmins();
    }

    public static String printAdmin(Long id) {
        return config.printAdmin(id);
    }

    public static Request getAdmin(Long id) {
        return config.getAdmin(id);
    }

    public static void addAdmin(Request request) {
        config.addAdmin(request);
    }

    public static void removeAdmin(Request request) {
        config.removeAdmin(request);
    }
}
