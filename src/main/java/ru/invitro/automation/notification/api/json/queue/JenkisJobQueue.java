package ru.invitro.automation.notification.api.json.queue;

import com.google.gson.JsonObject;

import java.util.Arrays;

public class JenkisJobQueue {

    private String _class;

    private Boolean blocked;

    private Boolean buildable;

    private Integer id;

    private Long inQueueSince;

    private String params;

    private Boolean stuck;

    private transient JsonObject[] task;

    private String url;

    private String why;

    private Long buildableStartMilliseconds;

    private Boolean pending;

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public Boolean getBuildable() {
        return buildable;
    }

    public void setBuildable(Boolean buildable) {
        this.buildable = buildable;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getInQueueSince() {
        return inQueueSince;
    }

    public void setInQueueSince(Long inQueueSince) {
        this.inQueueSince = inQueueSince;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Boolean getStuck() {
        return stuck;
    }

    public void setStuck(Boolean stuck) {
        this.stuck = stuck;
    }

    public JsonObject[] getTask() {
        return task;
    }

    public void setTask(JsonObject[] task) {
        this.task = task;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWhy() {
        return why;
    }

    public void setWhy(String why) {
        this.why = why;
    }

    public Long getBuildableStartMilliseconds() {
        return buildableStartMilliseconds;
    }

    public void setBuildableStartMilliseconds(Long buildableStartMilliseconds) {
        this.buildableStartMilliseconds = buildableStartMilliseconds;
    }

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    @Override
    public String toString() {
        return "JenkisJobQueue{" +
            "_class='" + _class + '\'' +
            ", blocked=" + blocked +
            ", buildable=" + buildable +
            ", id=" + id +
            ", inQueueSince=" + inQueueSince +
            ", params='" + params + '\'' +
            ", stuck=" + stuck +
            ", task=" + Arrays.toString(task) +
            ", url='" + url + '\'' +
            ", why='" + why + '\'' +
            ", buildableStartMilliseconds=" + buildableStartMilliseconds +
            ", pending=" + pending +
            '}';
    }
}
