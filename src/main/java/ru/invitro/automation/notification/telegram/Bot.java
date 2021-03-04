package ru.invitro.automation.notification.telegram;

import com.google.gson.JsonIOException;
import org.apache.commons.lang3.RandomStringUtils;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.invitro.automation.notification.api.JenkinsApiConnector;
import ru.invitro.automation.notification.config.ConfigReader;
import ru.invitro.automation.notification.config.ProjectConfig;
import ru.invitro.automation.notification.config.ProjectOptions;
import ru.invitro.automation.notification.config.ReportType;
import ru.invitro.automation.notification.config.admins.AdminRequests;
import ru.invitro.automation.notification.config.admins.Request;
import ru.invitro.automation.notification.data.UrlConverter;
import ru.invitro.automation.notification.data.reports.ReportCollector;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.ThucydidesReport;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.factory.ReportsFactory;
import ru.invitro.automation.notification.data.reports.jenkins.thucydides.messages.MessageWithImage;
import ru.invitro.automation.notification.telegram.keyboard.Keyboard;
import ru.invitro.automation.notification.telegram.keyboard.KeyboardFactory;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Bot extends TelegramLongPollingBot {

    final private static List<Stages> adminStages = new ArrayList<>();

    static {
        adminStages.add(Stages.ADD_REPORT_URL);
        adminStages.add(Stages.SAVE_REPORT_URL);

        adminStages.add(Stages.SELECT_URL_TO_REMOVE);
        adminStages.add(Stages.REMOVE_SELECTED_URL);

        adminStages.add(Stages.ADD_URL_FOR_TOKEN_AUTH);
        adminStages.add(Stages.ADD_USER_FOR_TOKEN_AUTH);
        adminStages.add(Stages.ADD_TOKEN);
        adminStages.add(Stages.REENTER_AUTH);

        adminStages.add(Stages.SELECT_PROJECT_TYPE);
        adminStages.add(Stages.ENTER_PROJECT_NAME);
        adminStages.add(Stages.REENTER_PROJECT);

        adminStages.add(Stages.CONFIG_PROJECT);
        adminStages.add(Stages.SELECT_PROJECT_OPTION);
        adminStages.add(Stages.ENTER_OPTION_VALUE);
        adminStages.add(Stages.SAVE_CONFIG);

        adminStages.add(Stages.SELECT_REQUEST);
        adminStages.add(Stages.ACCEPT_REQUEST);
    }

    final private Map<String, LocalDateTime> monitoringFailCount = new ConcurrentHashMap<>();

    final private Map<String, Boolean> wasRunning = new ConcurrentHashMap<>();

    final private Map<Long, BotChat> chatStatusList = new ConcurrentHashMap<>();

    private Keyboard allProjectsKeyboard = KeyboardFactory.makeProjectNamesKeyboard(0L);

    private int keyboardMessageID;

    /**
     * Run bot with selected options ana call runProjectsCheck()
     * @param options DefaultBotOptions
     */
    Bot(DefaultBotOptions options) {
        super(options);
        Thread thread = new Thread(this::runProjectsCheck);
        thread.start();
    }

    /**
     * Start thread for checking reports for each project in config file
     * Start threads fog checking job launch status if required
     */
    private void runProjectsCheck() {
        for (String projectName : ConfigReader.getProjects().keySet()) {
            Thread thread;
            thread = new Thread(() -> checkProject(projectName));
            thread.start();
            if (getProjectConfig(projectName).getCheckLaunch()) {
                thread = new Thread(() -> jobLaunchMonitoring(projectName));
                thread.start();
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Logger.writeLog(e.getMessage(), "");
                e.printStackTrace();
            }
        }
    }

    /**
     * Standard telegram bot method
     * @return bot name
     */
    @Override
    public String getBotUsername() {
        return ConfigReader.getTelegramBotName();
    }

    /**
     * Standard telegram bot method
     * @return bot token
     */
    @Override
    public String getBotToken() {
        return ConfigReader.getTelegramBotToken();
    }

    /**
     * TelegramLongPollingBot method. Receive update and make actions depends on update
     * @param update received update
     */
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long sender = update.getMessage().getFrom().getId().longValue();
            String message = update.getMessage().getText();
            if (message.contains(ConfigReader.getTelegramBotName())) {
                message = message.replaceAll(ConfigReader.getTelegramBotName(), "");
            }
            long currentChatID = update.getMessage().getChat().getId();
            if (!chatStatusList.containsKey(currentChatID)) {
                chatStatusList.put(currentChatID, new BotChat());
            }
            Keyboard chatProjectsKeyboard = KeyboardFactory.makeProjectNamesKeyboard(currentChatID);
            Keyboard chatNonProjectsKeyboard = KeyboardFactory.makeNonProjectNamesKeyboard(currentChatID);

            String operationID = generateID();
            switch (message) {

                case "/help":
                    resetChat(currentChatID);
                    StringBuilder sb = new StringBuilder();
                    sb.append("/help - command list").append("\r\n");
                    sb.append("/reports_list - list of reports url for project").append("\r\n");
                    sb.append("/start_reports - start sending report by selected project to current chat").append("\r\n");
                    sb.append("/stop_reports - stop sending report by selected project to current chat").append("\r\n");
                    sb.append("/get_report - immediately get result of last report from selected project").append("\r\n");
                    sb.append("/add_report - add report to project(*)").append("\r\n");
                    sb.append("/remove_report - remove report from project(*)").append("\r\n");
                    sb.append("/run_job - launch jobs").append("\r\n");
                    sb.append("/stop_job - stop launched jobs").append("\r\n");
                    sb.append("/add_token - add Jenkins API Token(*)").append("\r\n");
                    sb.append("/add_project - add new project(*)").append("\r\n");
                    sb.append("/config_project - configure project(*)").append("\r\n");
                    sb.append("/admin_request - admin rights request").append("\r\n");
                    sb.append("/check_requests - check admin requests(*)").append("\r\n");
                    sb.append("/remove_admin - remove admin(*)").append("\r\n");
                    send(currentChatID, sb.toString(), "");
                    break;

                case "/reports_list":
                    resetChat(currentChatID);
                    keyboardMessageID = sendKeyboard("Project reports list", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                    setChat(currentChatID, Stages.LIST, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    break;

                case "/start_reports":
                    resetChat(currentChatID);
                    if (chatNonProjectsKeyboard.getKeyboardButtons().size() == 0) {
                        send(currentChatID, "No available projects to select", operationID);
                    } else {
                        keyboardMessageID = sendKeyboard("Start report sending by project", currentChatID, chatNonProjectsKeyboard.getInlineKeyboard());
                        setChat(currentChatID, Stages.START_MONITORING, keyboardMessageID, chatNonProjectsKeyboard.getKeyboardButtons(), operationID);
                    }
                    break;

                case "/get_report":
                    resetChat(currentChatID);
                    keyboardMessageID = sendKeyboard("Immediately report by project", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                    setChat(currentChatID, Stages.SELECT_REPORT_TEST, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    break;

                case "/stop_reports":
                    resetChat(currentChatID);
                    keyboardMessageID = sendKeyboard("Stop report sending by project", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                    setChat(currentChatID, Stages.STOP_MONITORING, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    break;

                case "/add_report":
                    if (ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        keyboardMessageID = sendKeyboard("Add report url to project", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                        setChat(currentChatID, Stages.ADD_REPORT_URL, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    }
                    break;

                case "/remove_report":
                    if (ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        keyboardMessageID = sendKeyboard("Remove report url fom project", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                        setChat(currentChatID, Stages.SELECT_URL_TO_REMOVE, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    }
                    break;

                case "/run_job":
                    resetChat(currentChatID);
                    keyboardMessageID = sendKeyboard("Start Jenkins job", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                    if (chatProjectsKeyboard.getInlineKeyboard().getKeyboard().get(0).size() <= 0) {
                        keyboardMessageID = -1;
                    }
                    setChat(currentChatID, Stages.START_TESTS, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    break;

                case "/stop_job":
                    resetChat(currentChatID);
                    keyboardMessageID = sendKeyboard("Stop Jenkins job", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                    if (chatProjectsKeyboard.getInlineKeyboard().getKeyboard().get(0).size() <= 0) {
                        keyboardMessageID = -1;
                    }
                    setChat(currentChatID, Stages.STOP_TESTS, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    break;

                case "/add_token":
                    if (ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        send(currentChatID, "Enter Jenkins server url", operationID);
                        setChat(currentChatID, Stages.ADD_URL_FOR_TOKEN_AUTH, -1, new HashMap<>(), operationID);
                    }
                    break;

                case "/add_project":
                    if (ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        Keyboard projectsTypesKeyboard = KeyboardFactory.makeProjectTypesKeyboard();
                        keyboardMessageID = sendKeyboard("Select project type", currentChatID, projectsTypesKeyboard.getInlineKeyboard());
                        setChat(currentChatID, Stages.SELECT_PROJECT_TYPE, keyboardMessageID, projectsTypesKeyboard.getKeyboardButtons(), operationID);
                    }
                    break;

                case "/config_project":
                    if (ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        keyboardMessageID = sendKeyboard("Change project config", currentChatID, chatProjectsKeyboard.getInlineKeyboard());
                        setChat(currentChatID, Stages.CONFIG_PROJECT, keyboardMessageID, chatProjectsKeyboard.getKeyboardButtons(), operationID);
                    }
                    break;

                case "/admin_request":
                    resetChat(currentChatID);
                    if (currentChatID > 0) {
                        Request request = new Request();
                        request.setId(update.getMessage().getFrom().getId().longValue());
                        request.setUserName(update.getMessage().getFrom().getUserName());
                        request.setFirstName(update.getMessage().getFrom().getFirstName());
                        request.setLastName(update.getMessage().getFrom().getLastName());
                        if (ConfigReader.isAdmin(request.getId())) {
                            send(currentChatID, "You already have rights", operationID);
                            break;
                        }
                        try {
                            AdminRequests.addRequest(request);
                            send(currentChatID, "Request sent", operationID);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Logger.writeLog("Request sent error from chat " + currentChatID + "\n" + e.getMessage(), operationID);
                            send(currentChatID, "Request sent error", operationID);
                        }
                        resetChat(currentChatID);
                    }
                    break;

                case "/check_requests":
                    if (currentChatID > 0 && ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        send(currentChatID, "Current requests: ", operationID);
                        if (AdminRequests.printRequests().size() > 0) {
                            for (String requestValue : AdminRequests.printRequests()) {
                                send(currentChatID, requestValue, operationID);
                            }
                            Keyboard requestsKeyboard = KeyboardFactory.makeRequestsKeyboard();
                            keyboardMessageID = sendKeyboard("Select request", currentChatID, requestsKeyboard.getInlineKeyboard());
                            setChat(currentChatID, Stages.SELECT_REQUEST, keyboardMessageID, requestsKeyboard.getKeyboardButtons(), operationID);
                        } else {
                            send(currentChatID, "There is no requests", operationID);
                        }
                    }
                    break;

                case "/remove_admin":
                    if (currentChatID > 0 && ConfigReader.isAdmin(sender)) {
                        resetChat(currentChatID);
                        send(currentChatID, "Current admins: ", operationID);
                        if (ConfigReader.getAdmins().size() > 0) {
                            send(currentChatID, ConfigReader.printAdmins(), operationID);
                            Keyboard adminsKeyboard = KeyboardFactory.makeAdminsKeyboard();
                            keyboardMessageID = sendKeyboard("Select admin for deleting", currentChatID, adminsKeyboard.getInlineKeyboard());
                            setChat(currentChatID, Stages.REMOVE_ADMIN_SELECT, keyboardMessageID, adminsKeyboard.getKeyboardButtons(), operationID);
                        } else {
                            send(currentChatID, "There is no admins", operationID);
                        }
                    }
                    break;

                default:
                    BotChat thisChat = chatStatusList.get(currentChatID);
                    readMessage(thisChat.actions, message, currentChatID, sender);
            }
        } else if (update.hasCallbackQuery()) {
            Thread thread = new Thread(() -> {
                long sender = update.getCallbackQuery().getFrom().getId().longValue();
                long currentChatID = update.getCallbackQuery().getMessage().getChatId();
                BotChat thisChat = chatStatusList.get(currentChatID);
                String message = update.getCallbackQuery().getData();
                makeAction(thisChat.actions, thisChat.buttonsKeys.get(message), currentChatID, sender);
            });
            thread.start();
        }
    }

    /**
     * Performs an action depending on the current chat action, user message and user rights
     * @param currentAction current chat action
     * @param message received message
     * @param chatID chat ID
     * @param sender sender ID
     */

    private void readMessage(Stages currentAction, String message, long chatID, long sender) {
        if (adminStages.contains(currentAction) && !ConfigReader.isAdmin(sender)) {
            return;
        }
        BotChat thisChat = chatStatusList.get(chatID);
        switch (currentAction) {

            case SAVE_REPORT_URL:
                Pattern urlPattern = Pattern.compile("^(https?://[.:\\-/\\w\\d]*)");
                Matcher urlMatcher = urlPattern.matcher(message);
                if (urlMatcher.find()) {
                    addUrl(urlMatcher.group(1), thisChat.currentButton, chatID, thisChat.operationID);
                } else {
                    send(chatID, "Wrong url format", thisChat.operationID);
                }
                resetChat(chatID);
                break;

            case ADD_URL_FOR_TOKEN_AUTH:
                try {
                    thisChat.urlForAuth = UrlConverter.getJenkinsLink(message);
                    send(chatID, "Enter user login:", thisChat.operationID);
                    setChat(chatID, Stages.ADD_USER_FOR_TOKEN_AUTH, -1, new HashMap<>(), thisChat.operationID);
                } catch (IllegalArgumentException e) {
                    send(chatID, "Wrong url format", thisChat.operationID);
                    resetChat(chatID);
                }
                break;

            case ADD_USER_FOR_TOKEN_AUTH:
                thisChat.userForAuth = message;
                send(chatID, "Enter API Token:", thisChat.operationID);
                setChat(chatID, Stages.ADD_TOKEN, -1, new HashMap<>(), thisChat.operationID);
                break;

            case ADD_TOKEN:
                JenkinsApiConnector jenkinsApiConnector = new JenkinsApiConnector(thisChat.urlForAuth, thisChat.userForAuth, message, thisChat.operationID);
                if (!jenkinsApiConnector.isApiAuthValid()) {
                    send(chatID, "Authentication on server" + thisChat.urlForAuth + " for user " + thisChat.userForAuth + " with token " + message + " not accepted", thisChat.operationID);
                    Keyboard yesNoKeyboard = KeyboardFactory.makeYesNoKeyboard();
                    keyboardMessageID = sendKeyboard("Repeat data enter?", chatID, yesNoKeyboard.getInlineKeyboard());
                    setChat(chatID, Stages.REENTER_AUTH, keyboardMessageID, yesNoKeyboard.getKeyboardButtons(), thisChat.operationID);
                } else {
                    addToken(chatID, thisChat.urlForAuth, thisChat.userForAuth, message, thisChat.operationID);
                }
                break;

            case ENTER_PROJECT_NAME:
                if (ConfigReader.getProjects().containsKey(message)) {
                    send(chatID, "Project name " + message + " already in use", thisChat.operationID);
                    Keyboard yesNoKeyboard = KeyboardFactory.makeYesNoKeyboard();
                    keyboardMessageID = sendKeyboard("Enter new name?", chatID, yesNoKeyboard.getInlineKeyboard());
                    setChat(chatID, Stages.REENTER_PROJECT, keyboardMessageID, yesNoKeyboard.getKeyboardButtons(), thisChat.operationID);
                } else {
                    addProject(chatID, message, thisChat.reportName, thisChat.operationID);
                }
                break;

            case ENTER_OPTION_VALUE:
                try {
                    int value = Integer.parseInt(message);
                    if (value >= 1 && value <= 1440) {
                        thisChat.currentConfig.setParam(thisChat.configOption, value);
                        Keyboard optionKeyboard2 = KeyboardFactory.makeProjectConfigKeyboard(getProjectConfig(thisChat.reportName).getProjectOptions());
                        keyboardMessageID = sendKeyboard("Select option", chatID, optionKeyboard2.getInlineKeyboard());
                        setChat(chatID, Stages.SELECT_PROJECT_OPTION, keyboardMessageID, optionKeyboard2.getKeyboardButtons(), thisChat.operationID);
                    } else {
                        send(chatID, "Value not in range", thisChat.operationID);
                        send(chatID, "Enter period in minutes (1-1440)", thisChat.operationID);
                    }
                } catch (NumberFormatException e) {
                    send(chatID, "Wrong format. Repeat enter", thisChat.operationID);
                    send(chatID, "Enter period in minutes (1-1440)", thisChat.operationID);
                }
                break;
        }
    }

    /**
     * Performs an action depending on the current chat action, pressed button and user rights
     * @param currentAction current chat action
     * @param keyMessage pressed key
     * @param chatID chat ID
     * @param sender sender ID
     */
    private void makeAction(Stages currentAction, String keyMessage, long chatID, long sender) {
        if (adminStages.contains(currentAction) && !ConfigReader.isAdmin(sender)) {
            return;
        }
        Keyboard testKeyboard;
        BotChat thisChat = chatStatusList.get(chatID);
        send(chatID, keyMessage, thisChat.operationID);
        removeKeyboard(thisChat.keyboardMessageId, chatID, thisChat.operationID);
        switch (currentAction) {
            case REPORT:
                Thread threadReport = new Thread(() -> {
                    getLastReport(keyMessage, chatID);
                    resetChat(chatID);
                });
                threadReport.start();
                break;

            case LIST:
                StringBuilder sb = new StringBuilder();
                sb.append("Reports urls list:").append("\r\n");
                for (String url : getProjectConfig(keyMessage).getUrls()) {
                    sb.append(url).append("\r\n");
                }
                send(chatID, sb.toString(), thisChat.operationID);
                resetChat(chatID);
                break;

            case START_MONITORING:
                addChat(chatID, keyMessage, thisChat.operationID);
                resetChat(chatID);
                break;

            case STOP_MONITORING:
                removeChat(chatID, keyMessage, thisChat.operationID);
                resetChat(chatID);
                break;

            case SELECT_REPORT_TEST:
                thisChat.reportName = keyMessage;
                testKeyboard = KeyboardFactory.makeProjectJobsKeyboard(getProjectConfig(keyMessage).getJobNames());
                keyboardMessageID = sendKeyboard("Select url for report", chatID, testKeyboard.getInlineKeyboard());
                setChat(chatID, Stages.REPORT, keyboardMessageID, testKeyboard.getKeyboardButtons(), thisChat.operationID);
                break;

            case ADD_REPORT_URL:
                send(chatID, "Enter new report url", thisChat.operationID);
                thisChat.currentButton = keyMessage;
                setChat(chatID, Stages.SAVE_REPORT_URL, -1, new HashMap<>(), thisChat.operationID);
                break;

            case SELECT_URL_TO_REMOVE:
                testKeyboard = KeyboardFactory.makeProjectURLKeyboard(getProjectConfig(keyMessage).getUrls());
                keyboardMessageID = sendKeyboard("Remove url", chatID, testKeyboard.getInlineKeyboard());
                setChat(chatID, Stages.REMOVE_SELECTED_URL, keyboardMessageID, testKeyboard.getKeyboardButtons(), thisChat.operationID);
                thisChat.currentButton = keyMessage;
                break;

            case REMOVE_SELECTED_URL:
                removeUrl(keyMessage, thisChat.currentButton, chatID, thisChat.operationID);
                thisChat.currentButton = keyMessage;
                break;

            case START_TESTS:
                thisChat.reportName = keyMessage;
                testKeyboard = KeyboardFactory.makeProjectJobsKeyboard(getProjectConfig(keyMessage).getJobNames());
                keyboardMessageID = sendKeyboard("Select job for launch", chatID, testKeyboard.getInlineKeyboard());
                setChat(chatID, Stages.START_SELECTED_TEST, keyboardMessageID, testKeyboard.getKeyboardButtons(), thisChat.operationID);
                break;

            case START_SELECTED_TEST:
                Thread thread1 = new Thread(() -> {
                    runJenkinsJob(keyMessage, chatID);
                    resetChat(chatID);
                });
                thread1.start();
                break;

            case STOP_TESTS:
                thisChat.reportName = keyMessage;
                testKeyboard = KeyboardFactory.makeProjectJobsKeyboard(getProjectConfig(keyMessage).getJobNames());
                keyboardMessageID = sendKeyboard("Select job for stop", chatID, testKeyboard.getInlineKeyboard());
                setChat(chatID, Stages.STOP_SELECTED_TEST, keyboardMessageID, testKeyboard.getKeyboardButtons(), thisChat.operationID);
                break;

            case STOP_SELECTED_TEST:
                Thread thread2 = new Thread(() -> {
                    stopJenkinsJob(keyMessage, chatID);
                    resetChat(chatID);
                });
                thread2.start();
                break;

            case REENTER_AUTH:
                if (keyMessage.equals("YES")) {
                    resetChat(chatID);
                    send(chatID, "Enter Jenkins server url", thisChat.operationID);
                    setChat(chatID, Stages.ADD_URL_FOR_TOKEN_AUTH, -1, new HashMap<>(), thisChat.operationID);
                } else {
                    resetChat(chatID);
                }
                break;

            case SELECT_PROJECT_TYPE:
                thisChat.reportName = keyMessage;
                send(chatID, "Enter new project name", thisChat.operationID);
                setChat(chatID, Stages.ENTER_PROJECT_NAME, -1, new HashMap<>(), thisChat.operationID);
                break;

            case REENTER_PROJECT:
                if (keyMessage.equals("YES")) {
                    send(chatID, "Enter new project name", thisChat.operationID);
                    setChat(chatID, Stages.ENTER_PROJECT_NAME, -1, new HashMap<>(), thisChat.operationID);
                } else {
                    resetChat(chatID);
                }
                break;

            case CONFIG_PROJECT:
                thisChat.reportName = keyMessage;
                send(chatID, getProjectConfig(keyMessage).currentConfig(), thisChat.operationID);
                thisChat.currentConfig = getProjectConfig(keyMessage);
                Keyboard optionKeyboard = KeyboardFactory.makeProjectConfigKeyboard(getProjectConfig(keyMessage).getProjectOptions());
                keyboardMessageID = sendKeyboard("Select option", chatID, optionKeyboard.getInlineKeyboard());
                setChat(chatID, Stages.SELECT_PROJECT_OPTION, keyboardMessageID, optionKeyboard.getKeyboardButtons(), thisChat.operationID);
                break;

            case SELECT_PROJECT_OPTION:
                if (keyMessage.equals("CANCEL")) {
                    send(chatID, "Changes not saved", thisChat.operationID);
                    resetChat(chatID);
                    break;
                }
                if (keyMessage.equals("SAVE")) {
                    send(chatID, thisChat.currentConfig.currentConfig(), thisChat.operationID);
                    saveConfig(thisChat.currentConfig, thisChat.reportName, chatID, thisChat.operationID);
                    resetChat(chatID);
                    break;
                }
                thisChat.configOption = ProjectOptions.valueOf(keyMessage);
                send(chatID, "Current value: " + thisChat.currentConfig.getOptionValue(ProjectOptions.valueOf(keyMessage)), thisChat.operationID);
                if (thisChat.configOption.type.equals(Boolean.class)) {
                    Keyboard trueFalse = KeyboardFactory.makeTrueFalseKeyboard();
                    keyboardMessageID = sendKeyboard("Select value", chatID, trueFalse.getInlineKeyboard());
                    setChat(chatID, Stages.ENTER_OPTION_VALUE, keyboardMessageID, trueFalse.getKeyboardButtons(), thisChat.operationID);
                } else if (thisChat.configOption.type.equals(Integer.class)) {
                    send(chatID, "Enter period in minutes (1-1440)", thisChat.operationID);
                    setChat(chatID, Stages.ENTER_OPTION_VALUE, -1, new HashMap<>(), thisChat.operationID);
                } else {
                    thisChat.keyboardMessageId = -1;
                    resetChat(chatID);
                }
                break;

            case ENTER_OPTION_VALUE:
                boolean value;
                value = keyMessage.equals("TRUE");
                thisChat.currentConfig.setParam(thisChat.configOption, value);
                Keyboard optionKeyboard2 = KeyboardFactory.makeProjectConfigKeyboard(getProjectConfig(thisChat.reportName).getProjectOptions());
                keyboardMessageID = sendKeyboard("Select value", chatID, optionKeyboard2.getInlineKeyboard());
                setChat(chatID, Stages.SELECT_PROJECT_OPTION, keyboardMessageID, optionKeyboard2.getKeyboardButtons(), thisChat.operationID);
                break;

            case SELECT_REQUEST:
                Long requestId = Long.parseLong(keyMessage);
                send(chatID, AdminRequests.printRequest(requestId), thisChat.operationID);
                if (Objects.nonNull(AdminRequests.getRequest(requestId))) {
                    thisChat.requestId = requestId;
                    Keyboard acceptKeyboard = KeyboardFactory.makeAcceptKeyboard();
                    keyboardMessageID = sendKeyboard("Accept request?", chatID, acceptKeyboard.getInlineKeyboard());
                    setChat(chatID, Stages.ACCEPT_REQUEST, keyboardMessageID, acceptKeyboard.getKeyboardButtons(), thisChat.operationID);
                } else {
                    resetChat(chatID);
                }
                break;

            case ACCEPT_REQUEST:
                if (keyMessage.equals("ACCEPT")) {
                    Request request = AdminRequests.getRequest(thisChat.requestId);
                    addAdmin(request, chatID, sender, thisChat.operationID);
                } else {
                    send(thisChat.requestId, "Request decline", thisChat.operationID);
                }
                AdminRequests.removeRequest(thisChat.requestId, thisChat.operationID);
                resetChat(chatID);
                break;

            case REMOVE_ADMIN_SELECT:
                Long adminId = Long.parseLong(keyMessage);
                send(chatID, ConfigReader.printAdmin(adminId), thisChat.operationID);
                if (Objects.nonNull(ConfigReader.getAdmin(adminId))) {
                    thisChat.requestId = adminId;
                    Keyboard removeKeyboard = KeyboardFactory.makeRemoveKeyboard();
                    keyboardMessageID = sendKeyboard("Remove administrator?", chatID, removeKeyboard.getInlineKeyboard());
                    setChat(chatID, Stages.REMOVE_ADMIN_ACCEPT, keyboardMessageID, removeKeyboard.getKeyboardButtons(), thisChat.operationID);
                } else {
                    resetChat(chatID);
                }
                break;

            case REMOVE_ADMIN_ACCEPT:
                if (keyMessage.equals("REMOVE")) {
                    Request request = ConfigReader.getAdmin(thisChat.requestId);
                    removeAdmin(request, chatID, sender, thisChat.operationID);
                }
                resetChat(chatID);
                break;
        }
    }

    /**
     * Collect and sent to chat last job result
     * @param keyMessage "ALL" or report URL
     * @param chatID chat ID
     */

    private void getLastReport(String keyMessage, long chatID) {
        BotChat thisChat = chatStatusList.get(chatID);
        String report = thisChat.reportName;
        List<ThucydidesReport> allJobs = ReportsFactory.getReportList(getProjectConfig(report), thisChat.operationID);
        List<ThucydidesReport> jobsForSend = new ArrayList<>();
        if (keyMessage.equals("ALL")) {
            jobsForSend = allJobs;
        } else {
            for (ThucydidesReport job : allJobs) {
                if (UrlConverter.getJenkinsJobName(job.getUrl()).equals(keyMessage))
                    jobsForSend.add(job);
            }
        }
        for (ThucydidesReport job : jobsForSend) {
            ReportCollector.collectReport(job, thisChat.operationID);
            List reportResult = job.generateTestsReport();
            sendReport(chatID, reportResult, thisChat.operationID);
        }
        send(chatID, "Report finished", thisChat.operationID);
    }

    /**
     * Launch selected job
     * @param keyMessage "ALL" or report URL
     * @param chatID chat ID
     */

    private void runJenkinsJob(String keyMessage, long chatID) {
        BotChat thisChat = chatStatusList.get(chatID);
        String reportName = thisChat.reportName;
        ProjectConfig projectConfig = getProjectConfig(reportName);
        for (String testUrl : projectConfig.getUrls()) {
            if (keyMessage.equals("ALL") || UrlConverter.getJenkinsJobName(testUrl).equals(keyMessage)) {
                Thread threadRun = new Thread(() -> {
                    JenkinsApiConnector jenkinsApiConnector = new JenkinsApiConnector(testUrl, thisChat.operationID);
                    send(chatID, jenkinsApiConnector.startCurrentJob(), thisChat.operationID);
                });
                threadRun.start();
            }
        }
    }

    /**
     * Stop selected job if it was started
     * @param keyMessage "ALL" or report URL
     * @param chatID chat ID
     */
    private void stopJenkinsJob(String keyMessage, long chatID) {
        BotChat thisChat = chatStatusList.get(chatID);
        String reportName = thisChat.reportName;
        ProjectConfig projectConfig = getProjectConfig(reportName);
        for (String testUrl : projectConfig.getUrls()) {
            if (keyMessage.equals("ALL") || UrlConverter.getJenkinsJobName(testUrl).equals(keyMessage)) {
                Thread threadRun = new Thread(() -> {
                    JenkinsApiConnector jenkinsApiConnector = new JenkinsApiConnector(testUrl, thisChat.operationID);
                    send(chatID, jenkinsApiConnector.stopCurrentJob(), thisChat.operationID);
                });
                threadRun.start();
            }
        }
    }

    /**
     * Reset chat status
     * Remove keyboard and clear variables for selected chat
     * @param chatId chat ID
     */
    private void resetChat(Long chatId) {
        BotChat chat = chatStatusList.get(chatId);
        chat.actions = Stages.EMPTY;
        chat.configOption = ProjectOptions.EMPTY;
        if (chat.keyboardMessageId > 0) {
            removeKeyboard(chat.keyboardMessageId, chatId, chat.operationID);
        }
        chat.keyboardMessageId = -1;
        chat.currentConfig = null;
        chat.reportName = null;
        chat.urlForAuth = null;
        chat.userForAuth = null;
        chat.operationID = null;
        chat.requestId = null;
        chatStatusList.put(chatId, chat);
    }

    /**
     * Set chat options
     * @param chatId chat ID
     * @param action current action
     * @param keyboardId active keyboard ID
     * @param keyboard keyboard buttons with ids
     * @param operationID operation ID for logs
     */

    private void setChat(Long chatId, Stages action, int keyboardId, Map<String, String> keyboard, String operationID) {
        BotChat chat = chatStatusList.get(chatId);
        chat.actions = action;
        if (keyboard.size() > 0) {
            chat.keyboardMessageId = keyboardId;
            chat.buttonsKeys = keyboard;
        } else {
            chat.keyboardMessageId = -1;
            chat.buttonsKeys = new HashMap<>();
        }
        chat.operationID = operationID;
        chatStatusList.put(chatId, chat);
    }

    /**
     * Send keyboard to chat
     * @param text text before keyboard
     * @param chatID chat ID
     * @param keyboard keyboard
     * @return keyboard message ID
     */
    private Integer sendKeyboard(String text, long chatID, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatID);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        int count = 0;
        while (count < 300) {
            try {
                return execute(message).getMessageId();
            } catch (TelegramApiException e) {
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
        return 0;
    }

    /**
     * Remove keyboard from chat
     * @param messageID message ID with keyboard
     * @param chatID chat ID
     * @param operationID operation ID for logs
     */
    private void removeKeyboard(int messageID, long chatID, String operationID) {
        BotChat thisChat = chatStatusList.get(chatID);
        thisChat.keyboardMessageId = -1;
        thisChat.buttonsKeys = new HashMap<>();
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatID);
        editMarkup.setMessageId(messageID);
        editMarkup.setReplyMarkup(new InlineKeyboardMarkup());
        int count = 0;
        while (count < 300) {
            try {
                execute(editMarkup);
                return;
            } catch (TelegramApiException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    /**
     * Send message to chat
     * @param chatID char ID
     * @param message massage String
     */
    private void send(long chatID, String message, String operationID) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableHtml(true);
        sendMessage.setChatId(chatID);
        sendMessage.setText(message);
        int count = 0;
        while (count < 300) {
            try {
                execute(sendMessage);
                return;
            } catch (TelegramApiException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    /**
     * Send file to chat
     * @param chatID chat ID
     * @param file file
     */
    private void sendFile(long chatID, File file, String operationID) {
        SendDocument sendNewDocument = new SendDocument();
        sendNewDocument.setChatId(chatID);
        sendNewDocument.setDocument(file);
        int count = 0;
        while (count < 300) {
            try {
                execute(sendNewDocument);
                return;
            } catch (TelegramApiException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    /**
     * Send picture to chat
     * @param chatID chat ID
     * @param image picture file
     */
    private void sendImage(Long chatID, File image, String operationID) {
        SendPhoto sendPhotoRequest = new SendPhoto();
        sendPhotoRequest.setChatId(chatID);
        sendPhotoRequest.setPhoto(image);
        int count = 0;
        while (count < 300) {
            try {
                execute(sendPhotoRequest);
                return;
            } catch (TelegramApiException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    /**
     * Monitoring job launch status and send report about started job to available for selected project chats.
     * Infinite while loop with Exception ignoring
     * @param projectName project name
     */

    private void jobLaunchMonitoring(String projectName) {
        while (true) {
            String launchID = generateID();
            ProjectConfig projectConfig = getProjectConfig(projectName);
            try {
                Logger.writeLog("Check launch for jobs " + projectConfig.getUrls(), launchID);
                sendLaunchStatus(projectConfig, launchID);
                Thread.sleep(projectConfig.getPeriod());
            } catch (Throwable e) {
                Logger.writeLog("Monitoring job launch error for " + projectConfig.getUrls() + "\n" + e.getMessage(), launchID);
                e.printStackTrace();
            }
        }
    }

    /**
     * Check reports status for selected project. Send message to available for selected project chats if report has failed tests.
     * Infinite while loop with Exception ignoring
     * @param projectName project name
     */

    private void checkProject(String projectName) {
        while (true) {
            String monitoringID = generateID();
            ProjectConfig projectConfig = getProjectConfig(projectName);
            try {
                Logger.writeLog("Check reports " + projectConfig.getUrls(), monitoringID);
                if (projectConfig.getReportType().equals(ReportType.CONTINUOUS_MONITORING)) {
                    monitoring(projectConfig, monitoringID);
                } else {
                    checkProjectReports(projectConfig, monitoringID);
                }
                Thread.sleep(projectConfig.getPeriod());
            } catch (Throwable e) {
                Logger.writeLog("Monitoring error for " + projectConfig.getUrls() + "\n" + e.getMessage(), monitoringID);
                e.printStackTrace();
            }
        }
    }

    /**
     * Check jobs statuses for each report in project.
     * If job not running and report has failed tests, send message to chat.
     * Relaunch job.
     * @param projectConfig project config
     * @param operationID operation ID for logs
     */

    private void monitoring(ProjectConfig projectConfig, String operationID) {
        List<ThucydidesReport> jobs = ReportsFactory.getReportList(projectConfig, operationID);
        for (ThucydidesReport job : jobs) {
            String url = job.getUrl();
            if (!monitoringFailCount.containsKey(url)) {
                monitoringFailCount.put(url, LocalDateTime.now().minusDays(2));
            }
            JenkinsApiConnector jenkinsApiConnector = new JenkinsApiConnector(url, operationID);
            if (jenkinsApiConnector.getLastBuildResult().equals("UNAVAILABLE")) {
                List reportResult = job.checkAndGetResult();
                for (Long chatID : projectConfig.getChatsId())
                    sendReport(chatID, reportResult, operationID);
            } else {
                boolean testRunning = jenkinsApiConnector.isLastBuildRun();
                if (!testRunning) {
                    List reportResult = job.checkAndGetResult();
                    if (job.getUnsuccessfulTestCount() > 0
                        && monitoringFailCount.get(url).plusMinutes(projectConfig.getFailMonitoringPeriod())
                        .isBefore(LocalDateTime.now())) {
                        monitoringFailCount.put(url, LocalDateTime.now());
                        for (Long chatID : projectConfig.getChatsId()) {
                            sendReport(chatID, reportResult, operationID);
                        }
                    } else if (job.getUnsuccessfulTestCount() <= 0) {
                        monitoringFailCount.put(url, LocalDateTime.now().minusDays(2));
                    }
                    jenkinsApiConnector.startCurrentJob();
                }
            }
        }
    }

    /**
     * Check job statuses for each report in project.
     * If report is new and have failed tests, send message to chat
     * @param projectConfig project config
     * @param operationID operation ID for logs
     */

    private void checkProjectReports(ProjectConfig projectConfig, String operationID) {
        List<ThucydidesReport> jobs = ReportsFactory.getReportList(projectConfig, operationID);
        for (ThucydidesReport job : jobs) {
            List reportResult = job.checkAndGetResult();
            for (Long chatID : projectConfig.getChatsId())
                sendReport(chatID, reportResult, operationID);
        }
    }

    /**
     * Send report check results to chat
     * @param chatID chat ID
     * @param reportResult report results
     * @param operationID operation ID for logs
     */
    private void sendReport(Long chatID, List reportResult, String operationID) {
        Logger.writeLog("Send to chat " + chatID + "\n" + reportResult, operationID);
        for (Object message : reportResult) {
            if (message instanceof String) {
                System.out.println((String) message);
                send(chatID, (String) message, operationID);
                continue;
            }
            if (message instanceof File) {
                sendFile(chatID, (File) message, operationID);
                continue;
            }
            if (message instanceof MessageWithImage) {
                send(chatID, ((MessageWithImage) message).getMessage(), operationID);
                if (((MessageWithImage) message).getImage() != null) {
                    sendImage(chatID, ((MessageWithImage) message).getImage(), operationID);
                }
                if (((MessageWithImage) message).getAttach() != null) {
                    sendFile(chatID, ((MessageWithImage) message).getAttach(), operationID);
                }
            }
        }
    }

    /**
     * Send job launch status to available for selected project chats
     * @param projectConfig project config
     * @param operationID operation ID for logs
     */
    private void sendLaunchStatus(ProjectConfig projectConfig, String operationID) {
        List<String> urls = projectConfig.getUrls();
        List<Long> chats = projectConfig.getChatsId();
        int count = 0;
        while (count < urls.size()) {
            int threads = urls.size() - count > 3 ? 4 : urls.size() - count;
            CountDownLatch latch = new CountDownLatch(threads);
            for (int i = 0; i < threads; i++) {
                int currentCount = count + i;
                String url = urls.get(currentCount);
                Thread threadRun = new Thread(() -> {
                    JenkinsApiConnector jenkinsApiConnector = new JenkinsApiConnector(url, operationID);
                    if (wasRunning.containsKey(url)) {
                        try {
                            boolean testRunning = jenkinsApiConnector.isLastBuildRun();
                            Boolean testWasRunning = wasRunning.get(url);
                            if (!testWasRunning && testRunning) {
                                for (Long chatId : chats) {
                                    send(chatId, "<b>" + UrlConverter.getJenkinsJobName(url) + "</b>" + " was started \r\n" + UrlConverter.getJenkinsJobLink(url), operationID);
                                    wasRunning.put(url, true);
                                }
                            } else {
                                wasRunning.put(url, testRunning);
                            }
                        } catch (Exception e) {
                            Logger.writeLog("Launch status error for " + url + "\n" + e.getMessage(), operationID);
                            e.printStackTrace();
                        } finally {
                            latch.countDown();
                        }
                    } else {
                        wasRunning.put(url, true);
                        latch.countDown();
                    }
                });
                threadRun.start();
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
            }
            count += threads;
        }
    }

    /**
     * Get project configuration by project name
     * @param reportName project name
     * @return project configuration
     */
    private ProjectConfig getProjectConfig(String reportName) {
        return ConfigReader.getProjects().get(reportName);
    }

    /**
     * Add new report url to selected project config
     * @param url report url
     * @param projectName project name
     * @param chatID chat ID
     * @param operationID operation ID for logs
     */
    private void addUrl(String url, String projectName, long chatID, String operationID) {
        ProjectConfig projectConfig = getProjectConfig(projectName);
        if (!projectConfig.getUrls().contains(url)) {
            try {
                projectConfig.getUrls().add(url);
                ConfigReader.writeConfigFile();
            } catch (IOException | JsonIOException e) {
                Logger.writeLog("Add URL error: " + url + "\n" + e.getMessage(), operationID);
                e.printStackTrace();
                send(chatID, "Cannot add url to project", operationID);
                ConfigReader.readConfigFile();
                return;
            }
            send(chatID, "Url added", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Url already in project", operationID);
    }

    /**
     * Remove report url from selected project
     * @param url report url
     * @param projectName project name
     * @param chatID chat ID
     * @param operationID operation ID for logs
     */
    private void removeUrl(String url, String projectName, long chatID, String operationID) {
        ProjectConfig projectConfig = getProjectConfig(projectName);
        if (projectConfig.getUrls().contains(url)) {
            try {
                projectConfig.getUrls().remove(url);
                ConfigReader.writeConfigFile();
            } catch (IOException | JsonIOException e) {
                Logger.writeLog("Remove URL error: " + url + "\n" + e.getMessage(), operationID);
                e.printStackTrace();
                send(chatID, "Cannot remove url from project", operationID);
                ConfigReader.readConfigFile();
                return;
            }
            ThucydidesReport.removeFromMonitoring(url);
            send(chatID, "Url removed", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "There is no such url in project", operationID);
    }

    /**
     * Add chat to the chat list in project configuration for sending reports
     * @param projectName project name
     * @param chatID chat ID
     * @param operationID operation ID for logs
     */
    private void addChat(long chatID, String projectName, String operationID) {
        ProjectConfig projectConfig = getProjectConfig(projectName);
        Logger.writeLog("Add chat: " + chatID + " to project " + "\n" + projectConfig + "\n Chat is new: " + !projectConfig.getChatsId().contains(chatID), operationID);
        if (!projectConfig.getChatsId().contains(chatID)) {
            try {
                List<Long> chatIDs = projectConfig.getChatsId();
                chatIDs.add(chatID);
                projectConfig.setChatsId(chatIDs);
                ConfigReader.writeConfigFile();
            } catch (IOException e) {
                Logger.writeLog("Add chat error: " + chatID + "\n" + e.getMessage(), operationID);
                e.printStackTrace();
                send(chatID, "Cannot add chat", operationID);
                ConfigReader.readConfigFile();
                return;
            }
            send(chatID, "Chat added", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Chat already activated for this project", operationID);
    }

    /**
     * Remove chat from the chat list of selected project
     * @param projectName project name
     * @param chatID chat ID
     * @param operationID operation ID for logs
     */
    private void removeChat(long chatID, String projectName, String operationID) {
        ProjectConfig projectConfig = getProjectConfig(projectName);
        if (projectConfig.getChatsId().contains(chatID)) {
            try {
                List<Long> chatIDs = projectConfig.getChatsId();
                chatIDs.remove(chatID);
                projectConfig.setChatsId(chatIDs);
                Logger.writeLog("Remove chat: " + chatID + " from project " + "\n" + projectConfig + "\n Chat in project: " + projectConfig.getChatsId().contains(chatID), operationID);
                ConfigReader.writeConfigFile();
            } catch (IOException e) {
                Logger.writeLog("Remove chat error: " + chatID + "\n" + e.getMessage(), operationID);
                e.printStackTrace();
                send(chatID, "Cannot remove chat", operationID);
                ConfigReader.readConfigFile();
                return;
            }
            send(chatID, "Chat deleted", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "There is no such chat in selected project", operationID);
    }

    /**
     * Add Jenkins Auth token
     * @param chatID ID chat for messages
     * @param url Jenkins server url
     * @param login user name
     * @param auth token
     * @param operationID operation ID for logs
     */
    private void addToken(long chatID, String url, String login, String auth, String operationID) {
        ConfigReader.addJenkinsApiTokens(url, login, auth);
        try {
            ConfigReader.writeConfigFile();
        } catch (IOException | JsonIOException e) {
            Logger.writeLog("Add API Token error: " + url + "\n" + e.getMessage(), operationID);
            e.printStackTrace();
            send(chatID, "Cannot add token", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Token added", operationID);
        ConfigReader.readConfigFile();
    }

    /**
     * Add new project with selected project type pattern and start check this project status
     * @param chatID ID chat for messages
     * @param projectName project name
     * @param reportType project type
     * @param operationID operation ID for logs
     */
    private void addProject(long chatID, String projectName, String reportType, String operationID) {
        try {
            ConfigReader.addDefaultProject(reportType, projectName);
            ConfigReader.writeConfigFile();
        } catch (IOException | JsonIOException e) {
            Logger.writeLog("Add new project error: " + projectName + " " + reportType + "\n" + e.getMessage(), operationID);
            e.printStackTrace();
            send(chatID, "Cannot add new project", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Project added: " + projectName + " " + reportType, operationID);
        ConfigReader.readConfigFile();
        allProjectsKeyboard = KeyboardFactory.makeProjectNamesKeyboard(0L);
        new Thread(() -> checkProject(projectName)).start();
    }

    /**
     * Save project configuration
     * @param currentConfig project configuration
     * @param chatID ID chat for messages
     * @param projectName project name
     * @param operationID operation ID for logs
     */
    private void saveConfig(ProjectConfig currentConfig, String projectName, long chatID, String operationID) {
        ConfigReader.getProjects().put(projectName, currentConfig);
        Logger.writeLog("Save config:  project: " + projectName + "\n" + "config: " + "\n" + currentConfig + "\nchat: " + chatID, operationID);
        if (ConfigReader.getProjects().containsKey(projectName)) {
            try {
                ConfigReader.getProjects().put(projectName, currentConfig);
                ConfigReader.writeConfigFile();
            } catch (IOException e) {
                Logger.writeLog("Save config error: " + chatID + "\n" + e.getMessage(), operationID);
                e.printStackTrace();
                send(chatID, "Cannot save configuration", operationID);
                ConfigReader.readConfigFile();
                return;
            }
            send(chatID, "Configuration saved", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Project " + projectName + " not found in configuration", operationID);
    }

    /**
     * Add new administrator
     * @param adminRequest admin request for adding
     * @param chatID ID chat for messages
     * @param sender sender ID
     * @param operationID operation ID for logs
     */
    private void addAdmin(Request adminRequest, long chatID, long sender, String operationID) {
        ConfigReader.readConfigFile();
        ConfigReader.addAdmin(adminRequest);
        Logger.writeLog("Add admin:" + adminRequest + "\n" + "\nfrom chat: " + chatID + " by admin: " + sender, operationID);
        try {
            ConfigReader.writeConfigFile();
        } catch (IOException e) {
            Logger.writeLog("Add admin error: " + chatID + "\n" + e.getMessage(), operationID);
            e.printStackTrace();
            send(chatID, "Cannot add administrator", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Administrator added", operationID);
        send(adminRequest.getId(), "Access granted", operationID);
        ConfigReader.readConfigFile();
    }

    /**
     * Remove administrator
     * @param admin admin request for removing
     * @param chatID ID chat for messages
     * @param sender sender ID
     * @param operationID operation ID for logs
     */
    private void removeAdmin(Request admin, long chatID, long sender, String operationID) {
        ConfigReader.readConfigFile();
        ConfigReader.removeAdmin(admin);
        Logger.writeLog("Remove admin:" + admin + "\n" + "\nfrom chat: " + chatID + " by admin: " + sender, operationID);
        try {
            ConfigReader.writeConfigFile();
        } catch (IOException e) {
            Logger.writeLog("Remove admin error: " + chatID + "\n" + e.getMessage(), operationID);
            e.printStackTrace();
            send(chatID, "Cannot delete administrator", operationID);
            ConfigReader.readConfigFile();
            return;
        }
        send(chatID, "Administrator deleted", operationID);
        send(admin.getId(), "Access revoked", operationID);
        ConfigReader.readConfigFile();
    }

    /**
     * Generate 6 unit id
     * @return id
     */
    private String generateID() {
        return RandomStringUtils.random(6, true, true);
    }
}
