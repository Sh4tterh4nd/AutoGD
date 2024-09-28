package io.kellermann.services.video;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.ImageType;
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



    public void gemerateGDVideo(WorshipMetaData worshipMetaData) throws IOException {

        setupWorkspace();

        Path tempWorkspace = videoConfiguration.getTempWorkspace();
        Path albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        Path widescreenImage = tempWorkspace.resolve("widescreen_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        Path imageIntro = tempWorkspace.resolve("image_intro.mp4");
        Path renderedIntro = tempWorkspace.resolve("rendered_intro.mp4");


        worshipServiceApi.saveSeriesImageTo(ImageType.ALBUMART, worshipMetaData, albumartImage);
        worshipServiceApi.saveSeriesImageTo(ImageType.WIDESCREEN, worshipMetaData, widescreenImage);


        //Convert title image to video
        jaffreeFFmpegService.imageToVideo(widescreenImage, imageIntro, 3);

        //Merge intro parts to introVideo
        jaffreeFFmpegService.concatVideoAndMergeAudio(renderedIntro, videoConfiguration.getIntroSoundName(), imageIntro, videoConfiguration.getIntroVideoName());


        //Cut original main video
        Path originalCut = tempWorkspace.resolve("original_cut.mp4");
        jaffreeFFmpegService.cutVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), getMainRecording(), originalCut);

        //Render finished GD
        jaffreeFFmpegService.concatVideo(videoConfiguration.getOutput().resolve("finalGD.mp4"), 1.5, renderedIntro, originalCut, videoConfiguration.getOutroVideoName());


        //Generate podcast
        jaffreeFFmpegService.convertToWav(videoConfiguration.getOutput().resolve("finalGD.mp4"),videoConfiguration.getWavTarget().resolve("podcast.wav"));


        setupWorkspace();
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
