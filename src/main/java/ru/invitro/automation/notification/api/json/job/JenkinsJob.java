package ru.invitro.automation.notification.api.json.job;

import com.google.gson.JsonObject;
import ru.invitro.automation.notification.api.json.queue.JenkisJobQueue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JenkinsJob {

    transient List<JsonObject> scm;

    transient JsonObject[] upstreamProjects;

    private transient String _class;

    private transient JsonObject[] actions;

    private String description;

    private String displayName;

    private String fullDisplayName;

    private String fullName;

    private String name;

    private String url;

    private Boolean buildable;

    private List<JenkinsBuildInfo> builds = new ArrayList<>();

    private String color;

    private transient JsonObject[] healthReport;

    private Boolean inQueue;

    private Boolean keepDependencies;

    private JenkinsBuildInfo firstBuild;

    private JenkinsBuildInfo lastBuild;

    private JenkinsBuildInfo lastCompletedBuild;

    private JenkinsBuildInfo lastFailedBuild;

    private JenkinsBuildInfo lastStableBuild;

    private JenkinsBuildInfo lastSuccessfulBuild;

    private JenkinsBuildInfo lastUnstableBuild;

    private JenkinsBuildInfo lastUnsuccessfulBuild;

    private Integer nextBuildNumber;

    private transient JsonObject[] property;

    private JenkisJobQueue queueItem;

    private Boolean concurrentBuild;

    private transient JsonObject[] downstreamProjects;

    private String labelExpression;

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

    public String getFullDisplayName() {
        return fullDisplayName;
    }

    public void setFullDisplayName(String fullDisplayName) {
        this.fullDisplayName = fullDisplayName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getBuildable() {
        return buildable;
    }

    public void setBuildable(Boolean buildable) {
        this.buildable = buildable;
    }

    public List<JenkinsBuildInfo> getBuilds() {
        return builds;
    }

    public void setBuilds(List<JenkinsBuildInfo> builds) {
        this.builds = builds;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public JsonObject[] getHealthReport() {
        return healthReport;
    }

    public void setHealthReport(JsonObject[] healthReport) {
        this.healthReport = healthReport;
    }

    public Boolean getInQueue() {
        return inQueue;
    }

    public void setInQueue(Boolean inQueue) {
        this.inQueue = inQueue;
    }

    public Boolean getKeepDependencies() {
        return keepDependencies;
    }

    public void setKeepDependencies(Boolean keepDependencies) {
        this.keepDependencies = keepDependencies;
    }

    public JenkinsBuildInfo getFirstBuild() {
        return firstBuild;
    }

    public void setFirstBuild(JenkinsBuildInfo firstBuild) {
        this.firstBuild = firstBuild;
    }

    public JenkinsBuildInfo getLastBuild() {
        return lastBuild;
    }

    public void setLastBuild(JenkinsBuildInfo lastBuild) {
        this.lastBuild = lastBuild;
    }

    public JenkinsBuildInfo getLastCompletedBuild() {
        return lastCompletedBuild;
    }

    public void setLastCompletedBuild(JenkinsBuildInfo lastCompletedBuild) {
        this.lastCompletedBuild = lastCompletedBuild;
    }

    public JenkinsBuildInfo getLastFailedBuild() {
        return lastFailedBuild;
    }

    public void setLastFailedBuild(JenkinsBuildInfo lastFailedBuild) {
        this.lastFailedBuild = lastFailedBuild;
    }

    public JenkinsBuildInfo getLastStableBuild() {
        return lastStableBuild;
    }

    public void setLastStableBuild(JenkinsBuildInfo lastStableBuild) {
        this.lastStableBuild = lastStableBuild;
    }

    public JenkinsBuildInfo getLastSuccessfulBuild() {
        return lastSuccessfulBuild;
    }

    public void setLastSuccessfulBuild(JenkinsBuildInfo lastSuccessfulBuild) {
        this.lastSuccessfulBuild = lastSuccessfulBuild;
    }

    public JenkinsBuildInfo getLastUnstableBuild() {
        return lastUnstableBuild;
    }

    public void setLastUnstableBuild(JenkinsBuildInfo lastUnstableBuild) {
        this.lastUnstableBuild = lastUnstableBuild;
    }

    public JenkinsBuildInfo getLastUnsuccessfulBuild() {
        return lastUnsuccessfulBuild;
    }

    public void setLastUnsuccessfulBuild(JenkinsBuildInfo lastUnsuccessfulBuild) {
        this.lastUnsuccessfulBuild = lastUnsuccessfulBuild;
    }

    public Integer getNextBuildNumber() {
        return nextBuildNumber;
    }

    public void setNextBuildNumber(Integer nextBuildNumber) {
        this.nextBuildNumber = nextBuildNumber;
    }

    public JsonObject[] getProperty() {
        return property;
    }

    public void setProperty(JsonObject[] property) {
        this.property = property;
    }

    public JenkisJobQueue getQueueItem() {
        return queueItem;
    }

    public void setQueueItem(JenkisJobQueue queueItem) {
        this.queueItem = queueItem;
    }

    public Boolean getConcurrentBuild() {
        return concurrentBuild;
    }

    public void setConcurrentBuild(Boolean concurrentBuild) {
        this.concurrentBuild = concurrentBuild;
    }

    public JsonObject[] getDownstreamProjects() {
        return downstreamProjects;
    }

    public void setDownstreamProjects(JsonObject[] downstreamProjects) {
        this.downstreamProjects = downstreamProjects;
    }

    public String getLabelExpression() {
        return labelExpression;
    }

    public void setLabelExpression(String labelExpression) {
        this.labelExpression = labelExpression;
    }

    public List<JsonObject> getScm() {
        return scm;
    }

    public void setScm(List<JsonObject> scm) {
        this.scm = scm;
    }

    public JsonObject[] getUpstreamProjects() {
        return upstreamProjects;
    }

    public void setUpstreamProjects(JsonObject[] upstreamProjects) {
        this.upstreamProjects = upstreamProjects;
    }

    @Override
    public String toString() {
        return "JenkinsJob{" +
            "_class='" + _class + '\'' +
            ", actions=" + Arrays.toString(actions) +
            ", description='" + description + '\'' +
            ", displayName='" + displayName + '\'' +
            ", fullDisplayName='" + fullDisplayName + '\'' +
            ", fullName='" + fullName + '\'' +
            ", name='" + name + '\'' +
            ", url='" + url + '\'' +
            ", buildable=" + buildable +
            ", builds=" + builds +
            ", color='" + color + '\'' +
            ", healthReport=" + Arrays.toString(healthReport) +
            ", inQueue=" + inQueue +
            ", keepDependencies=" + keepDependencies +
            ", firstBuild=" + firstBuild +
            ", lastBuild=" + lastBuild +
            ", lastCompletedBuild=" + lastCompletedBuild +
            ", lastFailedBuild=" + lastFailedBuild +
            ", lastStableBuild=" + lastStableBuild +
            ", lastSuccessfulBuild=" + lastSuccessfulBuild +
            ", lastUnstableBuild=" + lastUnstableBuild +
            ", lastUnsuccessfulBuild=" + lastUnsuccessfulBuild +
            ", nextBuildNumber=" + nextBuildNumber +
            ", property=" + Arrays.toString(property) +
            ", queueItem='" + queueItem + '\'' +
            ", concurrentBuild=" + concurrentBuild +
            ", downstreamProjects=" + Arrays.toString(downstreamProjects) +
            ", labelExpression='" + labelExpression + '\'' +
            ", scm=" + scm +
            ", upstreamProjects=" + Arrays.toString(upstreamProjects) +
            '}';
    }
}
