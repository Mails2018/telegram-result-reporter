package ru.invitro.automation.notification.telegram;

import ru.invitro.automation.notification.config.ProjectConfig;
import ru.invitro.automation.notification.config.ProjectOptions;

import java.util.HashMap;
import java.util.Map;

class BotChat {

    Stages actions = Stages.EMPTY;

    ProjectOptions configOption = ProjectOptions.EMPTY;

    ProjectConfig currentConfig = null;

    int keyboardMessageId = -1;

    String reportName = null;

    String currentButton = null;

    Map<String, String> buttonsKeys = new HashMap<>();

    String urlForAuth = null;

    String userForAuth = null;

    String operationID = null;

    Long requestId = null;
}
