package ru.invitro.automation.notification.api.json.bild;

import com.google.gson.JsonObject;

import java.util.Arrays;

public class JenkinsBuild {

    transient JsonObject[] changeSet;

    private transient String _class;

    private transient JsonObject[] actions;

    private transient JsonObject[] artifacts;

    private Boolean building;

    private String description;

    private String displayName;

    private Long duration;

    private Long estimatedDuration;

    private transient Object executor;

    private String fullDisplayName;

    private String id;

    private Boolean keepLog;

    private Integer number;

    private Long queueId;

    private String result;

    private Long timestamp;

    private String url;

    private String builtOn;

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public JsonObject[] getActions() {
        return actions;
    }

    public void setActions(JsonObject[] actions) {
        this.actions = actions;
    }

    public JsonObject[] getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(JsonObject[] artifacts) {
        this.artifacts = artifacts;
    }

    public Boolean getBuilding() {
        return building;
    }

    public void setBuilding(Boolean building) {
        this.building = building;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Long estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public Object getExecutor() {
        return executor;
    }

    public void setExecutor(Object executor) {
        this.executor = executor;
    }

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getKeepLog() {
        return keepLog;
    }

    public void setKeepLog(Boolean keepLog) {
        this.keepLog = keepLog;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBuiltOn() {
        return builtOn;
    }

    public void setBuiltOn(String builtOn) {
        this.builtOn = builtOn;
    }

    public JsonObject[] getChangeSet() {
        return changeSet;
    }

    public void setChangeSet(JsonObject[] changeSet) {
        this.changeSet = changeSet;
    }

    @Override
    public String toString() {
        return "JenkinsBuild{" +
            "_class='" + _class + '\'' +
            ", actions=" + Arrays.toString(actions) +
            ", artifacts=" + Arrays.toString(artifacts) +
            ", building=" + building +
            ", description='" + description + '\'' +
            ", displayName='" + displayName + '\'' +
            ", duration=" + duration +
            ", estimatedDuration=" + estimatedDuration +
            ", executor='" + executor + '\'' +
            ", fullDisplayName='" + fullDisplayName + '\'' +
            ", id='" + id + '\'' +
            ", keepLog=" + keepLog +
            ", number=" + number +
            ", queueId=" + queueId +
            ", result='" + result + '\'' +
            ", timestamp=" + timestamp +
            ", url='" + url + '\'' +
            ", builtOn='" + builtOn + '\'' +
            ", changeSet=" + Arrays.toString(changeSet) +
            '}';
    }
}
