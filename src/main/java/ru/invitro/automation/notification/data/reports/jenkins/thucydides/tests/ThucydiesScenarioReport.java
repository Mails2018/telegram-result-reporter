package ru.invitro.automation.notification.data.reports.jenkins.thucydides.tests;

import java.io.File;

public class ThucydiesScenarioReport {

    private File image;

    private String attach;

    private ErrorType errorType;

    private String testName;

    private String failedStepName;

    private String testUrl;

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getFailedStepName() {
        return failedStepName;
    }

    public void setFailedStepName(String failedStepName) {
        this.failedStepName = failedStepName;
    }

    public String getTestUrl() {
        return testUrl;
    }

    public void setTestUrl(String testUrl) {
        this.testUrl = testUrl;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "ThucydiesScenarioReport{" +
            "image=" + image +
            ", attach='" + attach + '\'' +
            ", errorType=" + errorType +
            ", testName='" + testName + '\'' +
            ", failedStepName='" + failedStepName + '\'' +
            ", testUrl='" + testUrl + '\'' +
            '}';
    }

    public enum ErrorType {FAILED, BROKEN, PENDING}
}
