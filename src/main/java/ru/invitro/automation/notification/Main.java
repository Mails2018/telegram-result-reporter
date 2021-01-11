package ru.invitro.automation.notification;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import ru.invitro.automation.notification.config.ConfigReader;
import ru.invitro.automation.notification.telegram.BotFactory;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

    /**
     * Delete old files
     * Clear memory
     * Read configuration
     * If configuration read successfully. Initialize and launch bot
     */

    public static void main(String[] args) {
        deleteFiles(".txt");
        deleteFiles(".xml");
        deleteFiles(".png");
        List<String> arguments = Arrays.asList(args);
        if (arguments.contains("-c") || arguments.contains("-C")) {
            try {
                Runtime.getRuntime().exec("taskkill /F /T /IM chromedriver.exe");
                Runtime.getRuntime().exec("taskkill /F /T /IM chrome.exe");
            } catch (IOException ignore) {
            }
        }
        if (ConfigReader.readConfigFile()) {
            try {
                ApiContextInitializer.init();
                TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
                telegramBotsApi.registerBot(BotFactory.getBot());
            } catch (TelegramApiRequestException e) {
                Logger.writeLog("Bot start error \n" + e.getMessage(), "main");
                e.printStackTrace();
            }
        }
    }

    /**
     * Delete files with selected extension
     * @param extension
     */
    private static void deleteFiles(String extension) {
        for (File file : getFilesList(extension)) {
            if (!file.getName().contains("pom.xml")) {
                Logger.writeLog("delete file " + file.getName() + " : " + file.delete(), "main");
            }
        }
    }

    /**
     * Collect files with selected extension
     * @param extension
     * @return List of Files
     */
    public static List<File> getFilesList(String extension) {
        File[] files = (new File(".").listFiles((dir, name) -> name.toLowerCase().endsWith(extension)));
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(files);
    }
}
