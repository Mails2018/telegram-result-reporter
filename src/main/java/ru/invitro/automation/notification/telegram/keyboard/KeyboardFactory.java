package ru.invitro.automation.notification.telegram.keyboard;

import ru.invitro.automation.notification.config.ConfigReader;
import ru.invitro.automation.notification.config.ProjectConfig;
import ru.invitro.automation.notification.config.ProjectOptions;
import ru.invitro.automation.notification.config.ReportType;
import ru.invitro.automation.notification.config.admins.AdminRequests;
import ru.invitro.automation.notification.config.admins.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyboardFactory {

    public static Keyboard makeProjectNamesKeyboard(Long chatID) {
        List<String> projectsName = new ArrayList<>();
        for (String projectName : ConfigReader.getProjects().keySet()) {
            ProjectConfig projectConfig = ConfigReader.getProjects().get(projectName);
            if (chatID == 0 || projectConfig.getChatsId().contains(chatID)) {
                projectsName.add(projectName);
            }
        }
        return new Keyboard().makeKeyboard(projectsName, 3);
    }

    public static Keyboard makeProjectURLKeyboard(List<String> urls) {
        return new Keyboard().makeKeyboard(urls, 1);
    }

    public static Keyboard makeProjectJobsKeyboard(List<String> jobs) {
        jobs.add(0, "ALL");
        return new Keyboard().makeKeyboard(jobs, 1);
    }

    public static Keyboard makeYesNoKeyboard() {
        String[] yesNo = {"YES", "NO"};
        return new Keyboard().makeKeyboard(Arrays.asList(yesNo), 1);
    }

    public static Keyboard makeProjectTypesKeyboard() {
        List<String> reportsTypeName = new ArrayList<>();
        for (ReportType reportType : ReportType.values()) {
            reportsTypeName.add(reportType.name());
        }
        return new Keyboard().makeKeyboard(reportsTypeName, 1);
    }

    public static Keyboard makeProjectConfigKeyboard(List<ProjectOptions> options) {
        Keyboard keyboard = new Keyboard();
        List<String> optionsName = new ArrayList<>();
        for (ProjectOptions option : options) {
            optionsName.add(option.name());
        }
        keyboard.makeKeyboard(optionsName, 2);
        String[] saveCancel = {"SAVE", "CANCEL"};
        return keyboard.addButtons(Arrays.asList(saveCancel), 2);
    }

    public static Keyboard makeTrueFalseKeyboard() {
        String[] trueFalse = {"TRUE", "FALSE"};
        return new Keyboard().makeKeyboard(Arrays.asList(trueFalse), 1);
    }

    public static Keyboard makeRequestsKeyboard() {
        return new Keyboard().makeKeyboard(AdminRequests.getRequestsId(), 2);
    }

    public static Keyboard makeAdminsKeyboard() {
        List<String> ids = new ArrayList<>();
        for (Request admin : ConfigReader.getAdmins()) {
            ids.add(admin.getId().toString());
        }
        return new Keyboard().makeKeyboard(ids, 2);
    }

    public static Keyboard makeAcceptKeyboard() {
        String[] acceptDecline = {"ACCEPT", "DECLINE"};
        return new Keyboard().makeKeyboard(Arrays.asList(acceptDecline), 1);
    }

    public static Keyboard makeRemoveKeyboard() {
        String[] removeCancel = {"REMOVE", "CANCEL"};
        return new Keyboard().makeKeyboard(Arrays.asList(removeCancel), 1);
    }
}
