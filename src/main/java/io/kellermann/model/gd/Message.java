package io.kellermann.model.gd;

public class Message {

    private String text;
    private String time;

    public Message(String time, String text) {
        this.time = time;
        this.text = text;
    }

    public Message() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
