package io.kellermann;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.config.YoutubeConfiguration;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.JaffreeFFmpegService;
import io.kellermann.services.video.PodcastGenerationService;
import io.kellermann.services.video.VideoGenerationService;
import io.kellermann.services.youtube.ThumbnailService;
import io.kellermann.services.youtube.YoutubeUploader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;

@SpringBootApplication
@AutoConfiguration
public class Main implements CommandLineRunner {

    private final VideoGenerationService videoGenerationService;
    private final JaffreeFFmpegService jaffreeFFmpegService;
    private VideoGenerationService gdVidGenService;
    private VideoConfiguration videoConfig;
    private GDManagementConfig gdConfig;

    private WorshipServiceApi worshipServiceApi;

    private JaffreeFFmpegService jffmpegService;

    private YoutubeConfiguration configuration;

    private YoutubeUploader youtubeUploader;

    private ThumbnailService thumbnailService;

    private PodcastGenerationService podcastGenerationService;

    public Main(VideoGenerationService gdVidGenService, VideoConfiguration videoConfig, GDManagementConfig gdConfig,
                WorshipServiceApi worshipServiceApi, JaffreeFFmpegService jffmpegService, YoutubeConfiguration configuration, YoutubeUploader youtubeUploader,
                ThumbnailService thumbnailService, VideoGenerationService videoGenerationService, PodcastGenerationService podcastGenerationService, JaffreeFFmpegService jaffreeFFmpegService) {
        this.gdVidGenService = gdVidGenService;
        this.videoConfig = videoConfig;
        this.gdConfig = gdConfig;
        this.worshipServiceApi = worshipServiceApi;
        this.jffmpegService = jffmpegService;
        this.configuration = configuration;
        this.youtubeUploader = youtubeUploader;
        this.thumbnailService = thumbnailService;
        this.videoGenerationService = videoGenerationService;
        this.podcastGenerationService = podcastGenerationService;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        WorshipMetaData worshipMetaData = worshipServiceApi.getMostRecentWorship();

        System.out.println("Start");
        worshipServiceApi.getAllWorshipsFromTheMostRecentWorshipDay(LocalDate.now()).forEach(System.out::println);
//
//        if (Objects.nonNull(worshipMetaData)) {
//            Path outputPath = gdVidGenService.gemerateGDVideo(worshipMetaData).gemerateGDVideo(worshipMetaData);
//
//
//            thumbnailService.generateThumbnails(worshipMetaData);
//            String url = youtubeUploader.uploadToYoutube(outputPath, worshipMetaData);
//
//            podcastGenerationService.generateGDPodcast(worshipMetaData);
//            worshipServiceApi.submitYoutubeUrlToGDManagement(url, worshipMetaData);
//        } else {
//            throw new Exception("Couldn't find any worship data. Try specify date and time of worship in the application.yml");
//        }

    }
}
