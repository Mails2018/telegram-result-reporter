package ru.invitro.automation.notification.config;

public enum ProjectOptions {
    EMPTY(Boolean.class), CHECK_LAUNCH(Boolean.class), SMOKE(Boolean.class), JOB_NUMBER_BASED_REPORT(Boolean.class), PERIOD(Integer.class), FAIL_MONITORING_PERIOD(Integer.class);

    public final Class type;

    private ProjectOptions(Class type) {
        this.type = type;
    }
}
