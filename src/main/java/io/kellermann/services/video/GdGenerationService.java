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
import java.util.Objects;


@Service
public class GdGenerationService {
    private VideoConfiguration videoConfiguration;
    private JaffreeFFmpegService jaffreeFFmpegService;
    private WorshipServiceApi worshipServiceApi;
    private UtilityComponent utility;

    public GdGenerationService(VideoConfiguration videoConfiguration, JaffreeFFmpegService jaffreeFFmpegService, WorshipServiceApi worshipServiceApi, UtilityComponent utilityComponent) {
        this.videoConfiguration = videoConfiguration;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
        this.worshipServiceApi = worshipServiceApi;
        this.utility = utilityComponent;
    }

    /**
     * @param worshipMetaData
     * @return
     * @throws IOException
     */
    public Path gemerateGDVideo(WorshipMetaData worshipMetaData) throws IOException {
        setupWorkspace();
        Path tempWorkspace = videoConfiguration.getTempWorkspace();
        Path widescreenImage;
        if (Objects.isNull(worshipMetaData.getServiceImage()) || worshipMetaData.getServiceImage().isBlank()) {
            widescreenImage = tempWorkspace.resolve("widescreen_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        } else {
            widescreenImage = tempWorkspace.resolve("widescreen_" + worshipMetaData.getServiceImage());
        }


        Path imageIntro = tempWorkspace.resolve("image_intro.mp4");
        Path renderedIntro = tempWorkspace.resolve("rendered_intro.mp4");


        worshipServiceApi.saveGDImageTo(ImageType.WIDESCREEN, worshipMetaData, widescreenImage);


        //Convert title image to video
        jaffreeFFmpegService.imageToVideo(widescreenImage, imageIntro, 3);

        //Merge intro parts to introVideo
        jaffreeFFmpegService.concatVideoAndMergeAudio(renderedIntro, videoConfiguration.getIntroSoundName(), imageIntro, videoConfiguration.getIntroVideoName());


        //Cut original main video
        Path originalCut = tempWorkspace.resolve("original_cut.mp4");
        jaffreeFFmpegService.cutAudioVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), utility.getMainRecording(worshipMetaData), originalCut, false);

        //Render finished GD
        jaffreeFFmpegService.concatVideo(videoConfiguration.getOutput().resolve("finalGD.mp4"), 1.5, renderedIntro, originalCut, videoConfiguration.getOutroVideoName());


        //Generate podcast
        jaffreeFFmpegService.convertToWav(videoConfiguration.getOutput().resolve("finalGD.mp4"), videoConfiguration.getWavTarget().resolve("podcast.wav"));

        setupWorkspace();

        return videoConfiguration.getOutput().resolve("finalGD.mp4");
    }



    public void setupWorkspace() {
        if (Files.exists(videoConfiguration.getTempWorkspace())) {
            utility.clearDirectory(videoConfiguration.getTempWorkspace());
        } else {
            try {
                Files.createDirectories(videoConfiguration.getTempWorkspace());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
