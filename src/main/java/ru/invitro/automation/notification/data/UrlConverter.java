package ru.invitro.automation.notification.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlConverter {

    private final static String JENKINSLINKFROMJOBPATTERN = "(https?:\\/\\/[^\\/]+)\\/.*job\\/[^\\/]+\\/.*";

    private final static String JENKINSLINKPATTERN = "(https?:\\/\\/[^\\/]+).*";

    private final static String JENKINSJOBLINKPATTERN = "(https?:\\/\\/.+\\/job\\/[^\\/]+)\\/.*";

    private final static String JENKINSJOBNAMEPATTERN = "https?:\\/\\/.+\\/job\\/([^\\/]+)\\/.*";

    private final static String JENKINSBUILDNUMBERPATTERN = "^https?:\\/\\/.+\\/job\\/[^\\/]+\\/([\\d]+)\\/$";

    private static String convertUrlByPattern(String url, String pattern) {
        Pattern urlPattern = Pattern.compile(pattern);
        Matcher matcher = urlPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IllegalArgumentException("Url " + url + " not match to Jenkins job pattern");
        }
    }

    public static String getJenkinsLink(String url) {
        try {
            return convertUrlByPattern(url, JENKINSLINKFROMJOBPATTERN);
        } catch (IllegalArgumentException e) {
            return convertUrlByPattern(url, JENKINSLINKPATTERN);
        }
    }

    public static String getJenkinsJobLink(String url) {
        return convertUrlByPattern(url, JENKINSJOBLINKPATTERN);
    }

    public static String getJenkinsJobName(String url) {
        return convertUrlByPattern(url, JENKINSJOBNAMEPATTERN);
    }

    public static String getJenkinsBuildNumber(String url) {
        return convertUrlByPattern(url, JENKINSBUILDNUMBERPATTERN);
    }
}
