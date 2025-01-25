package io.kellermann;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.config.YoutubeConfiguration;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.GdGenerationService;
import io.kellermann.services.video.JaffreeFFmpegService;
import io.kellermann.services.youtube.ThumbnailService;
import io.kellermann.services.youtube.YoutubeUploader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AutoConfiguration
public class Main implements CommandLineRunner {

    private GdGenerationService gdVidGenService;
    private VideoConfiguration videoConfig;
    private GDManagementConfig gdConfig;

    private WorshipServiceApi worshipServiceApi;

    private JaffreeFFmpegService jffmpegService;

    private YoutubeConfiguration configuration;

    private YoutubeUploader youtubeUploader;

    private ThumbnailService thumbnailService;

    public Main(GdGenerationService gdVidGenService, VideoConfiguration videoConfig, GDManagementConfig gdConfig,
                WorshipServiceApi worshipServiceApi, JaffreeFFmpegService jffmpegService, YoutubeConfiguration configuration, YoutubeUploader youtubeUploader,
                ThumbnailService thumbnailService) {
        this.gdVidGenService = gdVidGenService;
        this.videoConfig = videoConfig;
        this.gdConfig = gdConfig;
        this.worshipServiceApi = worshipServiceApi;
        this.jffmpegService = jffmpegService;
        this.configuration = configuration;
        this.youtubeUploader = youtubeUploader;
        this.thumbnailService = thumbnailService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        System.out.println(jffmpegService.concatAudioCrossFadeFilterBuilder(4, 13.5));
//        WorshipMetaData worshipMetaData = worshipServiceApi.getMostRecentWorship();
//
//
//        if (Objects.nonNull(worshipMetaData)) {
//            Path outputPath = gdVidGenService.gemerateGDVideo(worshipMetaData);
//            thumbnailService.generateThumbnails(worshipMetaData);
//
//            String url = youtubeUploader.uploadToYoutube(Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\video\\finalGD.mp4"), worshipMetaData);
//
////            worshipServiceApi.submitURLToGDTool(url, worshipMetaData);
//        } else {
//            throw new Exception("Couldn't find any worship data. Try specify date and time of worship in the application.yml");
//        }

    }
}
