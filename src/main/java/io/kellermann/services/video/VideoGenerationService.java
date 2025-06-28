package io.kellermann.services.video;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gd.GdJob;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.StatusService;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;


@Service
public class VideoGenerationService {
    private final VideoConfiguration vidConfig;
    private final JaffreeFFmpegService jaffreeFFmpegService;
    private final WorshipServiceApi worshipServiceApi;
    private final UtilityComponent utility;

    private final StatusService statusService;

    public VideoGenerationService(VideoConfiguration vidConfig, JaffreeFFmpegService jaffreeFFmpegService, WorshipServiceApi worshipServiceApi, UtilityComponent utilityComponent, StatusService statusService) {
        this.vidConfig = vidConfig;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
        this.worshipServiceApi = worshipServiceApi;
        this.utility = utilityComponent;
        this.statusService = statusService;
    }

    /**
     * @param worshipMetaData
     * @return
     * @throws IOException
     */
    public Path gemerateGDVideo(WorshipMetaData worshipMetaData, GdJob gdJob) throws IOException {
        Path tmpWorkspace = vidConfig.getTempWorkspace().resolve(worshipMetaData.getServiceID().toString());
        Path resourceDir = vidConfig.getResources().resolve(worshipMetaData.getServiceType().getNameLanguage(worshipMetaData.getServiceLanguage()));

        setupWorkspace(tmpWorkspace);
        Path widescreenImage;
        if (Objects.isNull(worshipMetaData.getServiceImage()) || worshipMetaData.getServiceImage().isBlank()) {
            widescreenImage = tmpWorkspace.resolve("widescreen_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        } else {
            widescreenImage = tmpWorkspace.resolve("widescreen_" + worshipMetaData.getServiceImage());
        }


        Path imageIntro = tmpWorkspace.resolve("image_intro.mp4");
        Path renderedIntro = tmpWorkspace.resolve("rendered_intro.mp4");

        worshipServiceApi.saveGDImageTo(ImageType.WIDESCREEN, worshipMetaData, widescreenImage);


        //Convert title image to video
        jaffreeFFmpegService.imageToVideo(widescreenImage, imageIntro, 3);


        //Merge intro parts to introVideo
        jaffreeFFmpegService.concatVideoAndMergeAudio(renderedIntro,
                resourceDir.resolve(vidConfig.getIntroSoundName()),
                imageIntro,
                resourceDir.resolve(vidConfig.getIntroVideoName()));


        //Cut original main video
        Path originalCut = tmpWorkspace.resolve("original_cut.mp4");
        jaffreeFFmpegService.cutAudioVideo(gdJob.startTime(),
                gdJob.endTime(),
                utility.getMainRecording(worshipMetaData),
                originalCut,
                false);

//        jaffreeFFmpegService.cutAudioVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), utility.getMainRecording(worshipMetaData), originalCut, false);

        //Render finished GD
        jaffreeFFmpegService.concatVideo(vidConfig.getOutput().resolve("finalGD.mp4"),
                1.5,
                renderedIntro,
                originalCut,
                resourceDir.resolve(vidConfig.getOutroVideoName()));

        //Generate podcast
//        jaffreeFFmpegService.convertToWav(videoConfiguration.getOutput().resolve("finalGD.mp4"), videoConfiguration.getWavTarget().resolve("podcast.wav"));

        setupWorkspace(tmpWorkspace);

        return vidConfig.getOutput().resolve("finalGD.mp4");
    }


    public void setupWorkspace(Path path) {
        if (Files.exists(path)) {
            utility.clearDirectory(path);
        } else {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
