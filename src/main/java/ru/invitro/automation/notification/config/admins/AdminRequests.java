package ru.invitro.automation.notification.config.admins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import ru.invitro.automation.notification.telegram.logger.Logger;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class AdminRequests {

    private static final String REQUESTS_FILE = "admin.json";

    public static Set<Request> requests;

    public static Set<Request> readRequestFile() {
        try (FileInputStream fis = new FileInputStream(REQUESTS_FILE);
             Reader reader = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
            Type requestList = TypeToken.getParameterized(Set.class, Request.class).getType();
            requests = new Gson().fromJson(reader, requestList);
            Logger.writeLog("Current request list:\n" + requests, "main");
        } catch (IOException | JsonSyntaxException | JsonIOException e) {
            Logger.writeLog("Requests read error\n" + e.getMessage(), "main");
            e.printStackTrace();
            requests = new HashSet<>();
        }
        return requests;
    }

    public static void addRequest(Request request) throws IOException, JsonIOException {
        readRequestFile();
        requests.add(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(REQUESTS_FILE)) {
            gson.toJson(requests, writer);
        }
    }

    public static void removeRequest(Long id, String operationId) {
        Request request = new Request();
        request.setId(id);
        removeRequest(request, operationId);
    }

    public static void removeRequest(Request request, String operationId) {
        readRequestFile();
        requests.remove(request);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(REQUESTS_FILE)) {
            gson.toJson(requests, writer);
        } catch (IOException | JsonIOException e) {
            Logger.writeLog("Requests " + request + " remove error\n" + e.getMessage(), operationId);
            e.printStackTrace();
        }
    }

    public static List<String> printRequests() {
        readRequestFile();
        List<String> result = new ArrayList<>();
        for (Request request : requests) {
            result.add(request.toString());
        }
        return result;
    }

    public static Request getRequest(Long id) {
        for (Request request : requests) {
            if (request.id.equals(id)) {
                return request;
            }
        }
        return null;
    }

    public static String printRequest(Long id) {
        Request request = getRequest(id);
        if (Objects.nonNull(request)) {
            return request.toString();
        } else {
            return "Request with id " + id + " not found";
        }
    }

    public static List<String> getRequestsId() {
        List<String> result = new ArrayList<>();
        for (Request request : requests) {
            result.add(request.getId().toString());
        }
        return result;
    }
}
