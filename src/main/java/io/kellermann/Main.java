package io.kellermann;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.kellermann.config.GDManagementConfig;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.config.YoutubeConfiguration;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.GdGenerationService;
import io.kellermann.services.video.JaffreeFFmpegService;
import io.kellermann.services.youtube.YoutubeUploader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.nio.file.Path;
import java.util.Objects;

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

    public Main(GdGenerationService gdVidGenService, VideoConfiguration videoConfig, GDManagementConfig gdConfig, WorshipServiceApi worshipServiceApi, JaffreeFFmpegService jffmpegService, YoutubeConfiguration configuration, YoutubeUploader youtubeUploader) {
        this.gdVidGenService = gdVidGenService;
        this.videoConfig = videoConfig;
        this.gdConfig = gdConfig;
        this.worshipServiceApi = worshipServiceApi;
        this.jffmpegService = jffmpegService;
        this.configuration = configuration;
        this.youtubeUploader = youtubeUploader;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        WorshipMetaData worshipMetaData = worshipServiceApi.getMostRecentWorship();

        if (Objects.nonNull(worshipMetaData)) {
            Path outputPath = gdVidGenService.gemerateGDVideo(worshipMetaData);
            youtubeUploader.uploadToYoutube(outputPath, worshipMetaData);
        } else {
            throw new Exception("Couldn't find any worship data. Try specify date and time of worship in the application.yml");
        }

    }
}
