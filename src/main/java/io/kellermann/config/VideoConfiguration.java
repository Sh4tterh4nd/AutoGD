package io.kellermann.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;

@Component
@ConfigurationProperties(prefix = "autogd.video")
public class VideoConfiguration {

    private Path inputWorkspace = Paths.get("");
    private Path tempWorkspace = Paths.get("");

    private Path resources = Paths.get("");
    private Path recordings = Paths.get("");
    private Path output = Paths.get("");
    private Path wavTarget = Paths.get("");


    private String precodecParam = "";
    private String codec = "libx264";


    private String introVideoName = "1_intro.mp4";
    private String introSoundName = "1_introsound.mp4";
    private String outroVideoName = "3_outro.mp4";

    private String introPodcastName = "1_podcast_intro.mp3";
    private String outroPodcastName = "3_podcast_outro.mp3";


    private String gdVideoOriginalName = "original.mp4";


    private LocalTime gdVideoStartTime = LocalTime.MIN;
    private LocalTime gdVideoEndTime = LocalTime.MIN;

    private String finishedGdVideo = "finishedGD.mp4";


    private String loudnormParameter = "I=-15:TP=0.00";

    public VideoConfiguration() {
    }


    public Path getRecordings() {
        return recordings;
    }

    public void setRecordings(Path recordings) {
        this.recordings = recordings;
    }

    public Path getOutput() {
        return output;
    }

    public void setOutput(Path output) {
        this.output = output;
    }

    public Path getWavTarget() {
        return wavTarget;
    }

    public void setWavTarget(Path wavTarget) {
        this.wavTarget = wavTarget;
    }

    public Path getResources() {
        return resources;
    }

    public void setResources(Path resources) {
        this.resources = resources;
    }

    public String getPrecodecParam() {
        return precodecParam;
    }

    public void setPrecodecParam(String precodecParam) {
        this.precodecParam = precodecParam;
    }

    public String getCodec() {
        return codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
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

    public LocalTime getGdVideoStartTime() {
        return gdVideoStartTime;
    }

    public void setGdVideoStartTime(LocalTime gdVideoStartTime) {
        this.gdVideoStartTime = gdVideoStartTime;
    }

    public LocalTime getGdVideoEndTime() {
        return gdVideoEndTime;
    }

    public void setGdVideoEndTime(LocalTime gdVideoEndTime) {
        this.gdVideoEndTime = gdVideoEndTime;
    }

    public Path getTempWorkspace() {
        return tempWorkspace;
    }

    public void setTempWorkspace(Path tempWorkspace) {
        this.tempWorkspace = tempWorkspace;
    }


    public Path getInputWorkspace() {
        return inputWorkspace;
    }

    public void setInputWorkspace(Path inputWorkspace) {
        this.inputWorkspace = inputWorkspace;
    }


    public Path getIntroVideoName() {
        return resources.resolve(introVideoName);
    }

    public void setIntroVideoName(String introVideoName) {
        this.introVideoName = introVideoName;
    }

    public Path getIntroSoundName() {
        return resources.resolve(introSoundName);
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

    public Path getOutroVideoName() {
        return resources.resolve(outroVideoName);
    }

    public void setOutroVideoName(String outroVideoName) {
        this.outroVideoName = outroVideoName;
    }


    public Path getIntroPodcastName() {
        return resources.resolve(introPodcastName);
    }

    public void setIntroPodcastName(String introPodcastName) {
        this.introPodcastName = introPodcastName;
    }

    public Path getOutroPodcastName() {
        return resources.resolve(outroPodcastName);
    }

    public void setOutroPodcastName(String outroPodcastName) {
        this.outroPodcastName = outroPodcastName;
    }
}
