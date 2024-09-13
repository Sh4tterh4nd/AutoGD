package io.kellermann;

import io.kellermann.config.GDManagementConfig;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import io.kellermann.services.video.GdGenerationService;
import io.kellermann.services.video.JaffreeFFmpegService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Paths;

@SpringBootApplication
@AutoConfiguration
public class Main implements CommandLineRunner {

    private GdGenerationService gdVidGenService;
    private VideoConfiguration videoConfig;
    private GDManagementConfig gdConfig;

    private WorshipServiceApi worshipServiceApi;

    private JaffreeFFmpegService jffmpegService;


    public Main(GdGenerationService gdVidGenService, VideoConfiguration videoConfig, GDManagementConfig gdConfig, WorshipServiceApi worshipServiceApi, JaffreeFFmpegService jffmpegService) {
        this.gdVidGenService = gdVidGenService;
        this.videoConfig = videoConfig;
        this.gdConfig = gdConfig;
        this.worshipServiceApi = worshipServiceApi;
        this.jffmpegService = jffmpegService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

//        gdVidGenService.generateGDVideo(Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\charakterfest.jpg"));
        gdVidGenService.gen();


    }
}
