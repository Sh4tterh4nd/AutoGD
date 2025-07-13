package io.kellermann.services;

import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class UtilityComponent {

    private VideoConfiguration videoConfiguration;

    public UtilityComponent(VideoConfiguration videoConfiguration) {
        this.videoConfiguration = videoConfiguration;
    }


    public boolean doesPodcastRecordingExist(Integer worshipMetaData) {
        Path podcastRecording = findPodcastRecording(worshipMetaData);
        System.out.println(podcastRecording);
        return Objects.nonNull(podcastRecording);
    }


    public Path findPodcastRecording(Integer serviceId) {
        try (Stream<Path> pathStream = Files.walk(videoConfiguration.getPodcastData())) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .filter(s -> s.toString().contains(serviceId.toString()))
                    .findFirst()
                    .orElse(null);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void clearDirectory(Path toClear) {
        try (Stream<Path> pathStream = Files.walk(toClear)) {
            pathStream
                    .filter(s -> !s.equals(toClear))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    /**
     * Converts Paths matching the vMix recording pattern mp4Candidate objects with the filename parsed to a DateTime Object to easy filter / search for the needed recording.
     *
     * @param path path to potential mp4 recordings
     * @return parsed mp4Candidate object for submitted path
     */
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

    /**
     * Returns true a file has the .mp4 file ending
     *
     * @param thePath path to the file
     * @return boolean value if file ending is mp4
     */
    public boolean isMp4(Path thePath) {
        return thePath.getFileName().toString().endsWith(".mp4");
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
