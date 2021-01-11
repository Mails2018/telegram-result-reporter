package ru.invitro.automation.notification.data.reports.jenkins.thucydides;

public class PipeLineNodeReport extends ThucydidesReport {

    public PipeLineNodeReport(String url, boolean isSmoke, String id) {
        super(url, isSmoke, false, id);
    }

    String generateReportForPipeline() {

        StringBuilder sb = new StringBuilder();
        if (!reportAvailable) {
            sb.append("Unavailable report url:").append("\r\n");
        } else {
            sb.append("<b>").append(reportDate).append("</b>").append("\r\n");
            sb.append("<b>").append("Numbers of scenario: ").append(reportScenarios).append("</b>").append("\r\n");
            if (!context.equals("")) {
                sb.append("Environment: <b>").append(context).append("</b>").append("\r\n");
            }
            if (unsuccessfulTestCount > 0) {
                sb.append(unsuccessfulTestReport());
            } else {
                sb.append("<b>With out errors").append("</b>").append("\r\n");
            }
            sb.append("Report: ");
        }
        sb.append(url).append("\r\n");
        return sb.toString();
    }
}
