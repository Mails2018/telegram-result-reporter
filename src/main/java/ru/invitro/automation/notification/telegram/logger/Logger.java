package ru.invitro.automation.notification.telegram.logger;

import ru.invitro.automation.notification.config.ConfigReader;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Logger {

    private static final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter fileNameTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyy-HH-mm-ss");

    private static final ReentrantLock logLock = new ReentrantLock();

    private static File logFile = null;

    private static LocalDateTime logStart = LocalDateTime.now();

    private Logger() {
    }

    /**
     * Write data to log
     * @param log data
     * @param id operation id for log
     */
    public static void writeLog(Object log, String id) {
        LocalDateTime logTime = LocalDateTime.now();
        try {
            logLock.lock();
            LocalDateTime currentDate = LocalDateTime.now();
            if (logFile == null || logStart.plusDays(1).isBefore(currentDate)) {
                logStart = currentDate;
                String fileName = "log-" + currentDate.format(fileNameTimeFormatter) + ".log";
                logFile = new File(fileName);
                clearLogs();
            }
            try (FileWriter fw = new FileWriter(logFile, true)) {
                System.out.println(log);
                fw.write(logTime.format(logTimeFormatter) + " id: " + id + " " + log.toString() + "\n");
            }
        } catch (Exception ignore) {
        } finally {
            logLock.unlock();
        }
    }

    public static void clearLogs() {
        for (File logFile : getLogFiles()) {
            Pattern pattern = Pattern.compile("^.*log-([\\d-]*)\\.log$");
            Matcher matcher = pattern.matcher(logFile.getName());
            if (matcher.find()) {
                LocalDateTime fileDate = LocalDateTime.parse(matcher.group(1), fileNameTimeFormatter);
                if (fileDate.plusDays(ConfigReader.keepLogs()).isBefore(LocalDateTime.now())) {
                    writeLog("delete log file " + logFile.getName() + " : " + logFile.delete(), "");
                }
            }
        }
    }

    private static List<File> getLogFiles() {
        File[] oldPdfs = (new File(".")).listFiles((dir, name) -> name.toLowerCase().endsWith(".log"));
        if (oldPdfs == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(oldPdfs);
    }
}
