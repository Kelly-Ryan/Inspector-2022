package models;

public class DialogModel {
    private final String title, messageText;

    public DialogModel(String title, String messageText) {
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
