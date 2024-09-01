package io.kellermann.services.video;

import io.kellermann.config.VideoConfiguration;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GDVideoGenerationService {
    private VideoConfiguration videoConfiguration;
    private JaffreeFFmpegService jaffreeFFmpegService;


    public GDVideoGenerationService( VideoConfiguration videoConfiguration, JaffreeFFmpegService jaffreeFFmpegService) {
        this.videoConfiguration = videoConfiguration;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
    }

    public void generateGDVideo() throws IOException {
        generateGDVideo(videoConfiguration.getInputWorkspace().resolve(videoConfiguration.getIntroImageName()));
    }


    public void generateGDVideo(Path theIntroImagePath) throws IOException {
        Path basePath = videoConfiguration.getInputWorkspace();
        Path tmpPath = basePath.resolve(videoConfiguration.getTempWorkspace());

        if (Files.notExists(tmpPath)) {
            Files.createDirectories(tmpPath);
        }

        Path tmpIntroImageVideo = generateIntroImageVideo(theIntroImagePath);
        Path baseIntroVideo = basePath.resolve(videoConfiguration.getIntroVideoName());
        Path baseIntroSound = basePath.resolve(videoConfiguration.getIntroSoundName());

        Path baseOutroVideo = basePath.resolve(videoConfiguration.getOutroVideoName());

        Path tmpIntroVideo = videoConfiguration.getTempWorkspace().resolve("0_intro.mp4");

        jaffreeFFmpegService.concatVideoAndMergeAudio(tmpIntroVideo, baseIntroSound, tmpIntroImageVideo, baseIntroVideo);

        Path mainVideo = videoConfiguration.getTempWorkspace().resolve("1_gdPartGain.mp4");


        jaffreeFFmpegService.cutVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), basePath.resolve(videoConfiguration.getGdVideoOriginalName()), mainVideo);


        Path outPutGDFile = basePath.resolve(videoConfiguration.getFinishedGdVideo());

        jaffreeFFmpegService.concatVideo(outPutGDFile,1.5, tmpIntroVideo, mainVideo, baseOutroVideo);
    }


    public Path generateIntroImageVideo(Path imagePath) {
        System.out.println("Generating Intro Image Video");
        Path outputPath = videoConfiguration.getTempWorkspace().resolve("tmp_intro.mp4");
        jaffreeFFmpegService.imageToVideo(imagePath, outputPath, 3);
        return outputPath;
    }
}
