package io.kellermann.services.video;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.ImageType;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.UtilityComponent;
import io.kellermann.services.gdManagement.WorshipServiceApi;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class GdGenerationService {
    private VideoConfiguration videoConfiguration;
    private JaffreeFFmpegService jaffreeFFmpegService;
    private WorshipServiceApi worshipServiceApi;
    private UtilityComponent utilityC;

    public GdGenerationService(VideoConfiguration videoConfiguration, JaffreeFFmpegService jaffreeFFmpegService, WorshipServiceApi worshipServiceApi, UtilityComponent utilityComponent) {
        this.videoConfiguration = videoConfiguration;
        this.jaffreeFFmpegService = jaffreeFFmpegService;
        this.worshipServiceApi = worshipServiceApi;
        this.utilityC = utilityComponent;
    }


    public Path gemerateGDVideo(WorshipMetaData worshipMetaData) throws IOException {

        setupWorkspace();

        Path tempWorkspace = videoConfiguration.getTempWorkspace();
        Path albumartImage;
        Path widescreenImage;
        if (Objects.isNull(worshipMetaData.getService_albumart()) || worshipMetaData.getService_albumart().isBlank()) {
            albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        } else {
            albumartImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getService_albumart());
        }

        if (Objects.isNull(worshipMetaData.getServiceImage()) || worshipMetaData.getServiceImage().isBlank()) {
            widescreenImage = tempWorkspace.resolve("widescreen_" + worshipMetaData.getSeries().getAlbumartLanguage(worshipMetaData.getServiceLanguage()));
        } else {
            widescreenImage = tempWorkspace.resolve("albumart_" + worshipMetaData.getServiceImage());
        }


        Path imageIntro = tempWorkspace.resolve("image_intro.mp4");
        Path renderedIntro = tempWorkspace.resolve("rendered_intro.mp4");


        worshipServiceApi.saveGDImageTo(ImageType.ALBUMART, worshipMetaData, albumartImage);
        worshipServiceApi.saveGDImageTo(ImageType.WIDESCREEN, worshipMetaData, widescreenImage);


        //Convert title image to video
        jaffreeFFmpegService.imageToVideo(widescreenImage, imageIntro, 3);

        //Merge intro parts to introVideo
        jaffreeFFmpegService.concatVideoAndMergeAudio(renderedIntro, videoConfiguration.getIntroSoundName(), imageIntro, videoConfiguration.getIntroVideoName());


        //Cut original main video
        Path originalCut = tempWorkspace.resolve("original_cut.mp4");
        jaffreeFFmpegService.cutVideo(videoConfiguration.getGdVideoStartTime(), videoConfiguration.getGdVideoEndTime(), getMainRecording(worshipMetaData), originalCut);

        //Render finished GD
        jaffreeFFmpegService.concatVideo(videoConfiguration.getOutput().resolve("finalGD.mp4"), 1.5, renderedIntro, originalCut, videoConfiguration.getOutroVideoName());


        //Generate podcast
        jaffreeFFmpegService.convertToWav(videoConfiguration.getOutput().resolve("finalGD.mp4"), videoConfiguration.getWavTarget().resolve("podcast.wav"));

        setupWorkspace();

        return videoConfiguration.getOutput().resolve("finalGD.mp4");
    }

    public void setupWorkspace() {
        if (Files.exists(videoConfiguration.getTempWorkspace())) {
            utilityC.clearDirectory(videoConfiguration.getTempWorkspace());
        } else {
            try {
                Files.createDirectories(videoConfiguration.getTempWorkspace());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * Returns the most recent recording according to the vMix naming
     *
     * @param worshipMetaData
     * @return
     * @throws IOException
     */
    public Path getMainRecording(WorshipMetaData worshipMetaData) throws IOException {
        Path recordings = videoConfiguration.getRecordings();
        LocalDateTime worshipDateTimeOffset = worshipMetaData.getStartDate().atTime(worshipMetaData.getStartTime()).plusMinutes(15);

        mp4Candidates mp4Candidates = Files
                .walk(recordings)
                .filter(Files::isRegularFile)
                .filter(this::isMp4)
                .map(this::parsePathToCandidate)
                .filter(s -> s.getDateTime().toLocalDate().isEqual(worshipDateTimeOffset.toLocalDate()))
                .filter(s -> s.getDateTime().isBefore(worshipDateTimeOffset))
                .findFirst().orElse(null);

        if (mp4Candidates == null) {
            return videoConfiguration.getRecordings().resolve("original.mp4");
        }

        return mp4Candidates.getMp4Path();
    }

    public boolean isMp4(Path thePath) {
        return thePath.getFileName().toString().endsWith(".mp4");
    }

    public mp4Candidates parsePathToCandidate(Path path) {
        String fileName = path.getFileName().toString();
        Pattern compile = Pattern.compile("(?<=LIVE - )(?<timestring>.+)(?=\\.mp4)");
        Matcher matcher = compile.matcher(fileName);
        String timestring = "2000.01.01 - 01-01-01 AM";//default val in cases a mp4 doesn't match anything
        while (matcher.find()) {
            timestring = matcher.group("timestring");
        }

        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("yyyy.MM.dd - hh-mm-ss a")
                .toFormatter(Locale.US);

        LocalDateTime parse = LocalDateTime.parse(timestring, formatter);

        return new mp4Candidates(parse, path);
    }

    class mp4Candidates {
        private Path mp4Path;
        private LocalDateTime dateTime;

        public mp4Candidates(LocalDateTime dateTime, Path mp4Path) {
            this.dateTime = dateTime;
            this.mp4Path = mp4Path;
        }

        public mp4Candidates() {
        }

        public Path getMp4Path() {
            return mp4Path;
        }

        public void setMp4Path(Path mp4Path) {
            this.mp4Path = mp4Path;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public void setDateTime(LocalDateTime dateTime) {
            this.dateTime = dateTime;
        }
    }
}
