package io.kellermann.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "autogd.video")
public class VideoConfiguration {
    private String ffmpegLocation;
    private String ffprobeLocation;
    private String inputWorkspace;

    private String tempWorkspace = "tmp";

    private String introImageName = "0_title.png";
    private String introVideoName = "1_intro.mp4";
    private String introSoundName = "1_introsound.mp4";
    private String outroVideoName = "3_outro.mp4";


    private String gdVideoOriginalName = "original.mp4";
    private String gdVideoStartTime = "00:00:00.000";
    private String gdVideoEndTime = "00:00:00.000";

    private String finishedGdVideo = "finishedGD.mp4";


    private String loudnormParameter  = "I=-10:TP=0.00";

    public VideoConfiguration() {
    }

    public String getFinishedGdVideo() {
        return finishedGdVideo;
    }

    public void setFinishedGdVideo(String finishedGdVideo) {
        this.finishedGdVideo = finishedGdVideo;
    }

    public String getLoudnormParameter() {
        return loudnormParameter;
    }

    public void setLoudnormParameter(String loudnormParameter) {
        this.loudnormParameter = loudnormParameter;
    }

    public String getGdVideoStartTime() {
        return gdVideoStartTime;
    }

    public void setGdVideoStartTime(String gdVideoStartTime) {
        this.gdVideoStartTime = gdVideoStartTime;
    }

    public String getGdVideoEndTime() {
        return gdVideoEndTime;
    }

    public void setGdVideoEndTime(String gdVideoEndTime) {
        this.gdVideoEndTime = gdVideoEndTime;
    }

    public String getTempWorkspace() {
        return tempWorkspace;
    }

    public void setTempWorkspace(String tempWorkspace) {
        this.tempWorkspace = tempWorkspace;
    }

    public String getFfmpegLocation() {
        return ffmpegLocation;
    }

    public void setFfmpegLocation(String ffmpegLocation) {
        this.ffmpegLocation = ffmpegLocation;
    }

    public String getFfprobeLocation() {
        return ffprobeLocation;
    }

    public void setFfprobeLocation(String ffprobeLocation) {
        this.ffprobeLocation = ffprobeLocation;
    }

    public String getInputWorkspace() {
        return inputWorkspace;
    }

    public void setInputWorkspace(String inputWorkspace) {
        this.inputWorkspace = inputWorkspace;
    }

    public String getIntroImageName() {
        return introImageName;
    }

    public void setIntroImageName(String introImageName) {
        this.introImageName = introImageName;
    }

    public String getIntroVideoName() {
        return introVideoName;
    }

    public void setIntroVideoName(String introVideoName) {
        this.introVideoName = introVideoName;
    }

    public String getIntroSoundName() {
        return introSoundName;
    }

    public void setIntroSoundName(String introSoundName) {
        this.introSoundName = introSoundName;
    }

    public String getGdVideoOriginalName() {
        return gdVideoOriginalName;
    }

    public void setGdVideoOriginalName(String gdVideoOriginalName) {
        this.gdVideoOriginalName = gdVideoOriginalName;
    }

    public String getOutroVideoName() {
        return outroVideoName;
    }

    public void setOutroVideoName(String outroVideoName) {
        this.outroVideoName = outroVideoName;
    }
}
