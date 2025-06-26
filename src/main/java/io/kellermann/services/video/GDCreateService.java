package io.kellermann.services.video;

import io.kellermann.model.gd.GdJob;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.StatusService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GDCreateService {
    private final PodcastGenerationService podcastGenerationService;
    private final VideoGenerationService videoGenerationService;
    private final StatusService statusService;


    public GDCreateService(PodcastGenerationService podcastGenerationService, VideoGenerationService videoGenerationService, StatusService statusService) {
        this.podcastGenerationService = podcastGenerationService;
        this.videoGenerationService = videoGenerationService;
        this.statusService = statusService;
    }

    @Async
    public void startGDCreation(WorshipMetaData worshipMetaData, GdJob gdJob) {
        try {
            videoGenerationService.gemerateGDVideo(worshipMetaData, gdJob);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


//        try {
//            podcastGenerationService.generateGDPodcast(worshipMetaData);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

}
