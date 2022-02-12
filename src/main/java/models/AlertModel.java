package models;

public class AlertModel {
    private final String title, messageText;

    public AlertModel(String title, String messageText) {
        this.title = title;
        this.messageText = messageText;
    }

    public String getTitle() {
        return title;
    }

    public String getMessageText() {
        return messageText;
    }
}
