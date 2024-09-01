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

//        WorshipMetaData availableWorshipMetaData = worshipServiceApi.getMostRecentWorship();
//        Files.write(Paths.get("D:\\test1.jpg"), worshipServiceApi.getSeriesImage(ImageType.WIDESCREEN, availableWorshipMetaData));
//        Files.write(Paths.get("D:\\test2.jpg"), worshipServiceApi.getSeriesImage(ImageType.ALBUMART, availableWorshipMetaData));
//
//        Path input = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\charakterfest.jpg");
//
//        Path introImage = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\genIntro.mp4");
        Path intro = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\tmp\\0_intro.mp4");
        Path cut = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\tmp\\1_gdPartGain.mp4");
        Path outro = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\3_outro.mp4");
//
//        Path audio = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\1_introsound.mp4");
//
//        Path outputIntro = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\finalIntro.mp4");
//
//
        Path original = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\original.mp4");
        Path outVersion = Paths.get("C:\\Users\\Arieh\\Desktop\\AutoGD\\input\\outVersion.mp4");
//
//        System.out.println(jffmpegService.getDurationNanos(introImage));
//        System.out.println(jffmpegService.getDurationNanos(intro));
//        System.out.println(jffmpegService.getDurationNanos(original));
//
//        jffmpegService.cutVideo(videoConfig.getGdVideoStartTime(), videoConfig.getGdVideoEndTime(), original, cut);
//
//
        ArrayList<Path> paths = new ArrayList<>();
        paths.add(intro);
        paths.add(cut);
        paths.add(outro);


////        ffmpegService.concatVideoWithReEncode(paths, outVersion);
//        System.out.println(jffmpegService.concatFadeFilter(paths, .5));
//        jffmpegService.concatVideo(outVersion, paths);

    }
}
