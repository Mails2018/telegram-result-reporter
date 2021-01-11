package ru.invitro.automation.notification.data.reports.jenkins.thucydides.messages;

import java.io.File;

public class MessageWithImage {

    String message;

    File image;

    File attach;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public File getAttach() {
        return attach;
    }

    public void setAttach(File attach) {
        this.attach = attach;
    }

    @Override
    public String toString() {
        return "MessageWithImage{" +
            "message='" + message + '\'' +
            ", image=" + image +
            ", attach=" + attach +
            '}';
    }
}
