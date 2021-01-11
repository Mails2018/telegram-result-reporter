package ru.invitro.automation.notification.api;

import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ru.invitro.automation.notification.api.exception.JenkinsApiConnectorException;
import ru.invitro.automation.notification.api.json.bild.JenkinsBuild;
import ru.invitro.automation.notification.api.json.job.JenkinsJob;
import ru.invitro.automation.notification.config.ConfigReader;
import ru.invitro.automation.notification.data.UrlConverter;
import ru.invitro.automation.notification.telegram.logger.Logger;

public class JenkinsApiConnector {

    private final int apiConnectTry = 6;

    final private String jobRequestPattern = "/job/{jobName}/api/json";

    final private String buildRequestPattern = "/job/{jobName}/{build}/api/json";

    final private String startBuildRequestPattern = "/job/{jobName}/build";

    final private String stopBuildRequestPattern = "/job/{jobName}/{build}/stop";

    final private String cancelQueueItem = "/queue/cancelItem";

    private JenkinsJob nullPointJob;

    private String jobReportUrl;

    private RequestSpecification jenkinsSpec;

    private String password = "test";

    private String user = "test";

    private String operationID = "";

    public JenkinsApiConnector(String url, String user, String password, String operationID) {
        this.jenkinsSpec = new RequestSpecBuilder().setBaseUri(UrlConverter.getJenkinsLink(url)).build().auth().preemptive().basic(user, password);
        this.jobReportUrl = url;
        this.user = user;
        this.password = password;
        this.operationID = operationID;
    }

    public JenkinsApiConnector(String url, String operationID) {
        String jenkinsLink = UrlConverter.getJenkinsLink(url);
        if (ConfigReader.getJenkinsApiTokens().containsKey(jenkinsLink)) {
            Logger.writeLog(url + " use api token auth", operationID);
            String apiUser = ConfigReader.getJenkinsApiTokens().get(jenkinsLink).get(0);
            String apiToken = ConfigReader.getJenkinsApiTokens().get(jenkinsLink).get(1);
            this.jenkinsSpec = new RequestSpecBuilder().setBaseUri(UrlConverter.getJenkinsLink(url)).build().auth().preemptive().basic(apiUser, apiToken);
        } else {
            this.jenkinsSpec = new RequestSpecBuilder().setBaseUri(UrlConverter.getJenkinsLink(url)).build().auth().preemptive().basic(user, password);
        }
        this.jobReportUrl = url;
        this.operationID = operationID;
//        Logger.writeLog(url + " auth success: " + isApiAuthValid(), operationID);
    }

    public boolean isApiAuthValid() {
        int count = 0;
        while (count < apiConnectTry) {
            try {
                Response response = RestAssured.given().spec(jenkinsSpec.queryParam("pretty", "true")).when().get("/api");
                return response.getStatusCode() == 200;
            } catch (Exception e) {
                Logger.writeLog("Auth check error\n " + e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    Logger.writeLog(e1.getMessage(), operationID);
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }

    public String getLastBuildTimeStamp() {
        try {
            return getLastBuild().getTimestamp().toString();
        } catch (Exception e) {
            return "0";
        }
    }

    public String getLastSuccessfulBuildTimeStamp() {
        try {
            return getLastSuccessfulBuild().getTimestamp().toString();
        } catch (Exception e) {
            return "0";
        }
    }

    public Boolean isLastBuildRun() {
        try {
            return getLastBuild().getBuilding();
        } catch (NullPointerException e) {
            return false;
        }
    }

    private Boolean isJobInQueue() {
        return getCurrentJob().getInQueue();
    }

    public String getLastBuildResult() {
        try {
            return isLastBuildRun() ? "Job is run" : getLastBuild().getResult();
        } catch (Exception e) {
            return "UNAVAILABLE";
        }
    }

    public JenkinsBuild getLastBuild() {
        try {
            String lastBuildUrl = getCurrentJob().getLastBuild().getUrl();
            int count = 0;
            while (count < apiConnectTry) {
                try {
                    Response response = RestAssured.given().spec(jenkinsSpec.queryParam("pretty", "true")).when().get(buildGetApi(lastBuildUrl));
                    if (response.getStatusCode() == 200) {
                        return new Gson().fromJson(response.then().extract().asString(), JenkinsBuild.class);
                    } else {
                        throw new JenkinsApiConnectorException("Cannot get build from " + lastBuildUrl + " with code " + response.getStatusCode());
                    }
                } catch (Exception e) {
                    Logger.writeLog("Get last build request error\n" + e.getMessage(), operationID);
                    e.printStackTrace();
                    count++;
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        Logger.writeLog(e1.getMessage(), operationID);
                        e1.printStackTrace();
                    }
                }
            }
            throw new JenkinsApiConnectorException("Cannot get build from " + lastBuildUrl + " connection error");
        } catch (NullPointerException e) {
            Logger.writeLog(jobReportUrl + " NullPointerException in get last build", operationID);
            Logger.writeLog(jobReportUrl + " job \n" + nullPointJob, operationID);
            Logger.writeLog(jobReportUrl + " last build \n" + nullPointJob.getLastBuild(), operationID);
            try {
                Logger.writeLog(jobReportUrl + " last build url \n" + nullPointJob.getLastBuild().getUrl(), operationID);
            } catch (NullPointerException j) {
                Logger.writeLog(jobReportUrl + " cannot get last build url \n", operationID);
            }
            throw new NullPointerException(e.getMessage());
        }
    }

    public JenkinsBuild getLastSuccessfulBuild() {
        String lastBuildUrl = getCurrentJob().getLastSuccessfulBuild().getUrl();
        int count = 0;
        while (count < apiConnectTry) {
            try {
                Response response = RestAssured.given().spec(jenkinsSpec.queryParam("pretty", "true")).when().get(buildGetApi(lastBuildUrl));
                if (response.getStatusCode() == 200) {
                    return new Gson().fromJson(response.then().extract().asString(), JenkinsBuild.class);
                } else {
                    throw new JenkinsApiConnectorException("Cannot get build from " + lastBuildUrl + " with code " + response.getStatusCode());
                }
            } catch (Exception e) {
                Logger.writeLog("Get last successful build request error \n" + e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    Logger.writeLog(e1.getMessage(), operationID);
                    e1.printStackTrace();
                }
            }
        }
        throw new JenkinsApiConnectorException("Cannot get build from " + lastBuildUrl + " connection error");
    }

    private JenkinsJob getCurrentJob() {
        int count = 0;
        while (count < apiConnectTry) {
            try {
                Response response = RestAssured.given().spec(jenkinsSpec.queryParam("pretty", "true")).when().get(jobGetApi());
                if (response.getStatusCode() == 200) {
                    nullPointJob = new Gson().fromJson(response.then().extract().asString(), JenkinsJob.class);
                    return new Gson().fromJson(response.then().extract().asString(), JenkinsJob.class);
                } else {
                    throw new JenkinsApiConnectorException("Can't get job from " + UrlConverter.getJenkinsLink(jobReportUrl) + jobGetApi() + " with code " + response.getStatusCode());
                }
            } catch (Exception e) {
                Logger.writeLog("Get current job request error\n" + e.getMessage(), operationID);
                e.printStackTrace();
                count++;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    Logger.writeLog(e1.getMessage(), operationID);
                    e1.printStackTrace();
                }
            }
        }
        throw new JenkinsApiConnectorException("Cannot get build from " + jobGetApi() + " connection error");
    }

    public String startCurrentJob() {
        String jobUrl = UrlConverter.getJenkinsJobLink(jobReportUrl);
        Logger.writeLog("Start job " + jobUrl, operationID);
        try {
            if (isLastBuildRun()) {
                return ("Build №" + getLastBuild().getNumber() + " already launched \r\n" + jobUrl);
            }
            Response response = RestAssured.given().spec(jenkinsSpec).when().post(buildStartApi());
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Logger.writeLog(e.getMessage(), operationID);
                e.printStackTrace();
            }
            if (response.getStatusCode() != 201)
                throw new JenkinsApiConnectorException("Can't start job from " + jobUrl + " with code " + response.getStatusCode());
            if (isLastBuildRun()) {
                return ("Build №" + getLastBuild().getNumber() + " was started \r\n" + jobUrl);
            }
            if (isJobInQueue()) {
                return ("Build №" + getCurrentJob().getNextBuildNumber() + " in queue \r\n" + jobUrl);
            } else {
                return ("Build №" + getLastBuild().getNumber() + " cannot start build \r\n" + jobUrl);
            }
        } catch (Exception e) {
            return ("Unavailable job \r\n" + jobUrl);
        }
    }

    public String stopCurrentJob() {
        String jobUrl = UrlConverter.getJenkinsJobLink(jobReportUrl);
        String lastBuildUrl = getCurrentJob().getLastBuild().getUrl();
        String removeFromQueueMessage = "";
        try {
            if (!isLastBuildRun() && !isJobInQueue()) {
                return ("The job is currently not running \r\n" + jobUrl);
            }
            if (isJobInQueue()) {
                removeFromQueueMessage = removeFromQueue();
            }
            if (isLastBuildRun()) {
                Response response = RestAssured.given().spec(jenkinsSpec).when().post(buildStopApi(lastBuildUrl));
                if (!(response.getStatusCode() == 302 || response.getStatusCode() == 201))
                    throw new JenkinsApiConnectorException("Can't stop job from \r\n" + jobUrl + " with code " + response.getStatusCode());
                int count = 0;
                while (count < 20 && isLastBuildRun()) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        Logger.writeLog(e.getMessage(), operationID);
                        e.printStackTrace();
                    }
                    count++;
                }
            }
            if (!isLastBuildRun() && !isJobInQueue()) {
                return ("Build №" + getLastBuild().getNumber() + " was stopped \r\n" + jobUrl + ". " + removeFromQueueMessage);
            }
            if (isLastBuildRun() && !isJobInQueue()) {
                return ("Build №" + getLastBuild().getNumber() + " was stopped but still \r\n" + jobUrl + ". " + removeFromQueueMessage);
            } else {
                return ("Build №" + getLastBuild().getNumber() + " can't stop job \r\n" + jobUrl + ". " + removeFromQueueMessage);
            }
        } catch (Exception e) {
            return ("Unavailable job \r\n" + jobUrl + ". " + removeFromQueueMessage);
        }
    }

    private String removeFromQueue() {
        String jobUrl = UrlConverter.getJenkinsLink(jobReportUrl) + jobGetApi();
        try {

            if (isJobInQueue()) {
                int queueId = getCurrentJob().getQueueItem().getId();
                RestAssured.given().spec(jenkinsSpec.queryParam("id", queueId)).when().post(cancelQueueItem);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.writeLog(e.getMessage(), operationID);
                    e.printStackTrace();
                }
                if (isJobInQueue()) {
                    return ("Can't remove job from queue \r\n" + jobUrl);
                }
                return ("Job removed from queue \r\n" + jobUrl);
            } else {
                return ("Not in queue \r\n" + jobUrl);
            }
        } catch (Exception e) {
            return ("Unavailable job \r\n" + jobUrl);
        }
    }

    private String jobGetApi() {
        return jobRequestPattern.replace("{jobName}", UrlConverter.getJenkinsJobName(jobReportUrl));
    }

    private String buildGetApi(String buildUrl) {
        return buildRequestPattern
            .replace("{jobName}", UrlConverter.getJenkinsJobName(jobReportUrl))
            .replace("{build}", UrlConverter.getJenkinsBuildNumber(buildUrl));
    }

    private String buildStartApi() {
        return startBuildRequestPattern.replace("{jobName}", UrlConverter.getJenkinsJobName(jobReportUrl));
    }

    private String buildStopApi(String buildUrl) {
        return stopBuildRequestPattern
            .replace("{jobName}", UrlConverter.getJenkinsJobName(jobReportUrl))
            .replace("{build}", UrlConverter.getJenkinsBuildNumber(buildUrl));
    }
}
