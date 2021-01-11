package ru.invitro.automation.notification.api.json.job;

public class JenkinsBuildInfo {

    private String _class;

    private Integer number;

    private String url;

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "JenkinsBuildInfo{" +
            "_class='" + _class + '\'' +
            ", number=" + number +
            ", url='" + url + '\'' +
            '}';
    }
}
