package io.kellermann.model.gd;

public class StepStatus {
    private String progress;
    private String title;
    private String message;
    private String type;

    public StepStatus(String progress, String title, String message, String type) {
        this.progress = progress;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public StepStatus() {
    }

    public String getProgress() {
        return progress;
    }

    public void setProgress(String progress) {
        this.progress = progress;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
