package io.kellermann.model.gd;

public class Status {
    private String message;
    private Integer progress;

    public Status(String message, Integer progress) {
        this.message = message;
        this.progress = progress;
    }

    public Status() {
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
