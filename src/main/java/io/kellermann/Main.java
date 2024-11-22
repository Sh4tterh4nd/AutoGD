package io.kellermann;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.config.YoutubeConfiguration;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.GdGenerationService;
import io.kellermann.services.video.JaffreeFFmpegService;
import io.kellermann.services.youtube.ThumbnailService;
import io.kellermann.services.youtube.YoutubeUploader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;

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
        WorshipMetaData worshipMetaData = worshipServiceApi.getMostRecentWorship();

        Path path = Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\tmp\\thumb");

        thumbnailService.generateThumbnails(worshipMetaData);

//        Files.createDirectories(path);
//        Files.createDirectories(Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\tmp\\cropped"));
//        Files.createDirectories(Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\tmp\\detected"));
//        Files.createDirectories(Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\tmp\\detected2"));
//        Path outputPath = gdVidGenService.gemerateGDVideo(worshipMetaData);
//        jffmpegService.generateImageFromVideo(
//                LocalTime.of(0,23,10),
////                LocalTime.of(0,54,20),
//                LocalTime.of(0,54,20),
//                Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\recordings\\LIVE - 2024.09.15 - 09-56-45 AM.mp4"),
//                Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\tmp\\thumb"),
//                2);
//        thumbnailService.detectFace(Paths.get("C:\\Users\\Arieh\\Desktop\\NewGD\\tmp\\thumb"), worshipMetaData)/**/;
//        Files.walk(path).filter(Files::isRegularFile).forEach(s->thumbnailService.detectFace(s,worshipMetaData));
//
//
//        if (Objects.nonNull(worshipMetaData)) {
//            Path outputPath = gdVidGenService.gemerateGDVideo(worshipMetaData);
//            youtubeUploader.uploadToYoutube(outputPath, worshipMetaData);
//        } else {
//            throw new Exception("Couldn't find any worship data. Try specify date and time of worship in the application.yml");
//        }

    }
}
