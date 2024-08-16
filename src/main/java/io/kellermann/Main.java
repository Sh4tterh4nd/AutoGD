package io.kellermann;

import io.kellermann.services.FfmpegService;
import io.kellermann.services.GDVideoGenerationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@AutoConfiguration
public class Main implements CommandLineRunner {

    private GDVideoGenerationService gdVidGenService;
private FfmpegService ffmpegService;

    public Main(GDVideoGenerationService gdVidGenService, FfmpegService ffmpegService) {
        this.gdVidGenService = gdVidGenService;
        this.ffmpegService = ffmpegService;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        gdVidGenService.generateGDVideo();


    }
}