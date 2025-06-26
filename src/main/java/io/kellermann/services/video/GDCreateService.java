package io.kellermann.services.video;

import io.kellermann.model.gd.GdJob;
import io.kellermann.model.gd.StatusKeys;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.StatusService;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.youtube.ThumbnailService;
import io.kellermann.services.youtube.YoutubeUploader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class GDCreateService {
    private final PodcastGenerationService podcastGenerationService;
    private final VideoGenerationService videoGenerationService;
    private final ThumbnailService thumbnailService;
    private final StatusService statusService;
    private final YoutubeUploader youtubeUploader;
    private final WorshipServiceApi worshipServiceApi;

    @Value("${spring.profiles.active:none}")
    private String activeProfile;


    public GDCreateService(PodcastGenerationService podcastGenerationService, VideoGenerationService videoGenerationService, ThumbnailService thumbnailService, StatusService statusService, YoutubeUploader youtubeUploader, WorshipServiceApi worshipServiceApi) {
        this.podcastGenerationService = podcastGenerationService;
        this.videoGenerationService = videoGenerationService;
        this.thumbnailService = thumbnailService;
        this.statusService = statusService;
        this.youtubeUploader = youtubeUploader;
        this.worshipServiceApi = worshipServiceApi;
    }

    @Async
    public void startGDCreation(WorshipMetaData worshipMetaData, GdJob gdJob) {
        try {
            Path gdVideo = videoGenerationService.gemerateGDVideo(worshipMetaData, gdJob);
            thumbnailService.generateThumbnails(worshipMetaData, gdJob);
            String youtubeUrl = youtubeUploader.uploadToYoutube(gdVideo, worshipMetaData);
            statusService.sendFullDetail(StatusKeys.VIDEO_REGISTER, 0., "");
            //Todo uncomment
            if (!activeProfile.contains("dev")) {
                worshipServiceApi.submitYoutubeUrlToGDManagement(youtubeUrl, worshipMetaData);
            }
            statusService.sendFullDetail(StatusKeys.VIDEO_REGISTER, 1., "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        try {
            Path podcastPath = podcastGenerationService.generateGDPodcast(worshipMetaData, gdJob);
            if (!activeProfile.contains("dev")) {
                podcastGenerationService.uploadPodcastAndRegister(podcastPath, worshipMetaData.getServiceID());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
