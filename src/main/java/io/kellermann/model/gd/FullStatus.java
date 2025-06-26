package io.kellermann.model.gd;

public class FullStatus {
    private String stepTitle;
    private String message;
    private Integer detailPercentage;
    private Integer stepPercentage;
    private Integer fullPercentage;
    private String targetType;

    public FullStatus(String stepTitle, String message, String targetType, Integer detailPercentage, Integer stepPercentage, Integer fullPercentage) {
        this.stepTitle = stepTitle;
        this.message = message;
        this.detailPercentage = detailPercentage;
        this.stepPercentage = stepPercentage;
        this.fullPercentage = fullPercentage;
        this.targetType = targetType;
    }

    public FullStatus() {
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getDetailPercentage() {
        return detailPercentage;
    }

    public void setDetailPercentage(Integer detailPercentage) {
        this.detailPercentage = detailPercentage;
    }

    public Integer getStepPercentage() {
        return stepPercentage;
    }

    public void setStepPercentage(Integer stepPercentage) {
        this.stepPercentage = stepPercentage;
    }

    public Integer getFullPercentage() {
        return fullPercentage;
    }

    public void setFullPercentage(Integer fullPercentage) {
        this.fullPercentage = fullPercentage;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}
