package io.kellermann.services.video;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.Language;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@Service
public class GdGenerationService {
    private VideoConfiguration videoConfiguration;
    private JaffreeFFmpegService jaffreeFFmpegService;
    private WorshipServiceApi worshipServiceApi;
    private UtilityComponent utilityC;

    public GdGenerationService(VideoConfiguration videoConfiguration, JaffreeFFmpegService jaffreeFFmpegService, WorshipServiceApi worshipServiceApi, UtilityComponent utilityComponent) {
        this.videoConfiguration = videoConfiguration;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
        this.worshipServiceApi = worshipServiceApi;
        this.utilityC = utilityComponent;
    }


    public void generateGDVideo(Path theIntroImagePath) throws IOException {
        Path basePath = videoConfiguration.getInputWorkspace();
        Path tmpPath = videoConfiguration.getTempWorkspace();

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

        jaffreeFFmpegService.concatVideo(outPutGDFile, 1.5, tmpIntroVideo, mainVideo, baseOutroVideo);
    }


    public Path generateIntroImageVideo(Path imagePath) {
        System.out.println("Generating Intro Image Video");
        Path outputPath = videoConfiguration.getTempWorkspace().resolve("tmp_intro.mp4");
        jaffreeFFmpegService.imageToVideo(imagePath, outputPath, 3);
        return outputPath;
    }


    public void gen() {
        WorshipMetaData mostRecentWorship = worshipServiceApi.getMostRecentWorship();
        setupWorkspace();

        Path tempWorkspace = videoConfiguration.getTempWorkspace();
        Path albumartImage = tempWorkspace.resolve("albumart_" + mostRecentWorship.getSeries().getSeriesAlbumartLanguage(Language.fromString(mostRecentWorship.getServiceLanguage())));
        Path widescreenImage = tempWorkspace.resolve("widescreen_" + mostRecentWorship.getSeries().getSeriesAlbumartLanguage(Language.fromString(mostRecentWorship.getServiceLanguage())));
        Path imageIntro = tempWorkspace.resolve("image_intro.mp4");

        Path renderedIntro = tempWorkspace.resolve("rendered_intro.mp4");

        System.err.println(videoConfiguration.getInputWorkspace());

        worshipServiceApi.saveSeriesImageTo(ImageType.ALBUMART, mostRecentWorship, albumartImage);
        worshipServiceApi.saveSeriesImageTo(ImageType.WIDESCREEN, mostRecentWorship, widescreenImage);


        //Convert title image to video
        jaffreeFFmpegService.imageToVideo(widescreenImage, imageIntro, 0.75);

        //Merge intro parts to introVideo
        jaffreeFFmpegService.concatVideoAndMergeAudio(renderedIntro, videoConfiguration.getIntroSoundName(), imageIntro, videoConfiguration.getIntroVideoName());


        //Cut original main video
        Path originalCut = tempWorkspace.resolve("original_cut.mp4");
        jaffreeFFmpegService.cutVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), getMainRecording(), originalCut);

        //Render finished GD
        jaffreeFFmpegService.concatVideo(videoConfiguration.getOutput().resolve("finalGD.mp4"), 1.5, renderedIntro, originalCut, videoConfiguration.getOutroVideoName());


        //Generate podcast
        jaffreeFFmpegService.convertToWav(videoConfiguration.getOutput().resolve("finalGD.mp4"),videoConfiguration.getWavTarget().resolve("podcast.wav"));

    }

    public void setupWorkspace() {
        if (Files.exists(videoConfiguration.getTempWorkspace())) {
            utilityC.clearDirectory(videoConfiguration.getTempWorkspace());
        } else {
            try {
                Files.createDirectories(videoConfiguration.getTempWorkspace());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }


    public Path getMainRecording() {
        return videoConfiguration.getRecordings().resolve("original.mp4");
    }
}
