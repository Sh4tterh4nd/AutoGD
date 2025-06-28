package io.kellermann.model.gd;

public class StepStatus {

    private String title;
    private String message;
    private String type;
    private Integer progress;

    public StepStatus(String title, String message, String type, Integer progress) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.progress = progress;
    }

    public StepStatus() {
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
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
