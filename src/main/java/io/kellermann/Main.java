package io.kellermann;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.FfmpegService;
import io.kellermann.services.video.GDVideoGenerationService;
import io.kellermann.services.video.JaffreeFFmpegService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@SpringBootApplication
@AutoConfiguration
public class Main implements CommandLineRunner {

    private GDVideoGenerationService gdVidGenService;
    private VideoConfiguration videoConfig;
    private GDManagementConfig gdConfig;

    private WorshipServiceApi worshipServiceApi;

    private FfmpegService ffmpegService;
    private JaffreeFFmpegService jffmpegService;


    public Main(GDVideoGenerationService gdVidGenService, VideoConfiguration videoConfig, GDManagementConfig gdConfig, WorshipServiceApi worshipServiceApi, FfmpegService ffmpegService, JaffreeFFmpegService jffmpegService) {
        this.gdVidGenService = gdVidGenService;
        this.videoConfig = videoConfig;
        this.gdConfig = gdConfig;
        this.worshipServiceApi = worshipServiceApi;
        this.ffmpegService = ffmpegService;
        this.jffmpegService = jffmpegService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        gdVidGenService.generateGDVideo(Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\charakterfest.jpg"));


    }
}
