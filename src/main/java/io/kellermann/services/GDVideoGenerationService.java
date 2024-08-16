package io.kellermann.services;

import io.kellermann.config.VideoConfiguration;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class GDVideoGenerationService {
    private FfmpegService ffmpegService;
    private VideoConfiguration videoConfiguration;

    public GDVideoGenerationService(FfmpegService ffmpegService, VideoConfiguration videoConfiguration) {
        this.ffmpegService = ffmpegService;
        this.videoConfiguration = videoConfiguration;
    }

    public void generateGDVideo() throws IOException {
        Path basePath = Paths.get(videoConfiguration.getInputWorkspace());
        Path tmpPath = basePath.resolve(videoConfiguration.getTempWorkspace());

        if (Files.notExists(tmpPath)) {
            Files.createDirectories(tmpPath);
        }

        Path tmpIntroImageVideo = ffmpegService.generateIntroImageVideo(basePath.resolve(videoConfiguration.getIntroImageName()));
        Path baseIntroVideo = basePath.resolve(videoConfiguration.getIntroVideoName());
        Path baseIntroSound = basePath.resolve(videoConfiguration.getIntroSoundName());

        Path baseOutroVideo = basePath.resolve(videoConfiguration.getOutroVideoName());

        Path tmpIntroVideo = ffmpegService.concatVideoMergeAudio(baseIntroSound, tmpIntroImageVideo, baseIntroVideo);

        Path mainVideo = ffmpegService.cutVideoAndAudioGain(basePath.resolve(videoConfiguration.getGdVideoOriginalName()),
                videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), videoConfiguration.getLoudnormParameter());


        Path outPutGDFile = basePath.resolve(videoConfiguration.getFinishedGdVideo());

        ffmpegService.concatVideoWithReEncode(outPutGDFile, tmpIntroVideo, mainVideo, baseOutroVideo);
    }
}
