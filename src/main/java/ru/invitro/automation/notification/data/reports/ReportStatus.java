package ru.invitro.automation.notification.data.reports;

public class ReportStatus {

    private String successfulTimestamp = "0";

    private String failTimestamp = "0";

    private Boolean available = true;

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getSuccessfulTimestamp() {
        return successfulTimestamp;
    }

    public void setSuccessfulTimestamp(String successfulTimestamp) {
        this.successfulTimestamp = successfulTimestamp;
    }

    public String getFailTimestamp() {
        return failTimestamp;
    }

    public void setFailTimestamp(String failTimestamp) {
        this.failTimestamp = failTimestamp;
    }
}


