package io.kellermann.services.video;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.github.kokorin.jaffree.ffmpeg.OutputListener;
import com.github.kokorin.jaffree.ffmpeg.UrlInput;
import com.github.kokorin.jaffree.ffmpeg.UrlOutput;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import io.kellermann.config.VideoConfiguration;
import io.kellermann.model.gd.StatusKeys;
import io.kellermann.model.gdVerwaltung.WorshipMetaData;
import io.kellermann.services.StatusService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JaffreeFFmpegService {

    private final VideoConfiguration videoConfiguration;
    private final StatusService statusService;
    private Pattern LOG_TIME_REGEX = Pattern.compile("time=(?<time>\\d+:\\d+:\\d+(\\.\\d*))");

    public JaffreeFFmpegService(VideoConfiguration videoConfiguration, StatusService statusService) {
        this.videoConfiguration = videoConfiguration;
        this.statusService = statusService;
    }

    public void imageToVideo(Path imagePath, Path videoOutput, double duration) {
        FFmpeg
                .atPath()
                .addInput(UrlInput.fromPath(imagePath)
                        .addArguments("-f", "lavfi")
                        .addArguments("-i", "anullsrc=r=44100:cl=stereo")
                        .addArguments("-loop", "1")
                        .addArguments("-pix_fmt", "yuv420p")
                        .addArguments("-framerate", "30")
                        .addArguments("-t", String.valueOf(duration)))
                .addOutput(UrlOutput.toPath(videoOutput)
                        .setFrameRate(30)
                        .setCodec(StreamType.VIDEO, "libx264")
                        .setCodec(StreamType.AUDIO, "aac")
                        .addArguments("-vf", "fade=out:st=" + (duration - 0.5) + ":d=0.5")
                        .setFormat("mp4")
                        .addArgument("-shortest")
                )
                .setOverwriteOutput(true)
                .setOutputListener(new OutputListener() {
                    @Override
                    public void onOutput(String s) {
                        statusService.sendLogUpdate(s);
                    }
                })
                .execute();
    }

    public void concatVideoAndMergeAudio(Path output, Path audio, List<Path> videos) {
        LocalTime loc = LocalTime.MIN;
        for (Path video : videos) {
            loc = loc.plusNanos(getDurationLocalTime(video).toNanoOfDay());
        }
        FFmpeg fFmpeg = FFmpeg.atPath();
        videos.forEach(s -> fFmpeg.addInput(UrlInput.fromPath(s)));
        fFmpeg.addInput(UrlInput.fromPath(audio));
        LocalTime finalLoc = loc;
        fFmpeg
                .setComplexFilter(concatVideoFilterBuilder(videos.size()) + ";[a][" + videos.size() + ":a] amix=inputs=2:duration=longest [aOut] ")
                .addArguments("-map", "[aOut]")
                .addArguments("-map", "[v]")
                .addOutput(UrlOutput.toPath(output)
                        .setCodec(StreamType.VIDEO, "libx264")
                        .setCodec(StreamType.AUDIO, "aac"))
                .setOutputListener(new OutputListener() {
                    @Override
                    public void onOutput(String s) {
                        statusService.sendLogUpdate(s);
                        LocalTime currentTime = getLocalTimeFromLog(s);
                        if (currentTime != LocalTime.MIN) {
                            double progress = ((double) currentTime.toNanoOfDay() / finalLoc.toNanoOfDay());
                            statusService.sendFullDetail(StatusKeys.VIDEO_INTRO, progress, "");
                        }
                    }
                })
                .execute();
        statusService.sendFullDetail(StatusKeys.VIDEO_INTRO, 1.0, "Finished Intro Generation");
    }

    public void concatVideoAndMergeAudio(Path output, Path audio, Path... videos) {
        concatVideoAndMergeAudio(output, audio, Arrays.asList(videos));
    }

    /**
     * Cut video and extract video and audio from a file (or exclusively audio)
     *
     * @param start     the start time of the desired part within the original file
     * @param end       the endpoint of the desired part within the original file
     * @param input     the input file path Video
     * @param output    the output file path for the new Video or Audio
     * @param onlyAudio define if only the Audio should be extracted
     */
    public void cutAudioVideo(LocalTime start, LocalTime end, Path input, Path output, boolean onlyAudio) {
        LocalTime finalLoc = getDurationLocalTime(input);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
        FFmpeg ffmpegPack = FFmpeg
                .atPath()
                .addInput(UrlInput.fromPath(input)
                        .addArguments("-ss", start.format(dateTimeFormatter))
                        .addArguments("-to", end.format(dateTimeFormatter)));
        if (onlyAudio) {
            ffmpegPack
                    .addOutput(UrlOutput.toPath(output));
//                            .setCodec(StreamType.AUDIO, "copy")
//                            .addArguments("-b:a", "192k"));
        } else {
            ffmpegPack
                    .addOutput(UrlOutput.toPath(output)
                            .setCodec(StreamType.VIDEO, "copy")
                            .setCodec(StreamType.AUDIO, "copy")
                            .addArguments("-b:a", "192k"));
        }

        ffmpegPack
                .setOutputListener(s -> {
                    statusService.sendLogUpdate(s);
                    LocalTime currentTime = getLocalTimeFromLog(s);
                    if (currentTime != LocalTime.MIN) {
                        double progress = ((double) currentTime.toNanoOfDay() / finalLoc.toNanoOfDay());
                        if (onlyAudio) {
//                            statusService.sendDetailStatus("Podcast Schnitt", progress);
                            statusService.sendFullDetail(StatusKeys.VIDEO_CUT, progress, "");
                        } else {
//                            statusService.sendDetailStatus("Video Schnitt", progress);

                        }
                    }
                })
                .execute();
    }

    /**
     * Concat List of videos and fade the individual sections out and in
     *
     * @param output       target output file
     * @param videos       number of Paths to videos, each video requires one audio and one video track
     * @param fadeDuration the fade duration in seconds
     */
    public void concatVideo(Path output, double fadeDuration, Path... videos) {
        concatVideo(output, Arrays.asList(videos), fadeDuration);
    }

    /**
     * Concat List of videos and fade the individual sections out and in
     *
     * @param output       target output file
     * @param videos       the List of paths to the videos, each video requires one audio and one video track
     * @param fadeDuration the fade duration in seconds
     */
    public void concatVideo(Path output, List<Path> videos, double fadeDuration) {
        LocalTime loc = LocalTime.MIN;
        for (Path video : videos) {
            loc = loc.plusNanos(getDurationLocalTime(video).toNanoOfDay());
        }
        LocalTime finalLoc = loc;
        FFmpeg fFmpeg = FFmpeg
                .atPath()
                .setOverwriteOutput(true);

        for (Path video : videos) {
            fFmpeg.addInput(UrlInput.fromPath(video));

        }
        fFmpeg
                .addArguments("-filter_complex", concatFadeVideoFilterBuilder(videos, fadeDuration) + ";[a]loudnorm=" + videoConfiguration.getLoudnormParameter() + " [aud]");

        fFmpeg.addOutput(UrlOutput.toPath(output)
                .addArguments("-vcodec", videoConfiguration.getCodec())
                .addArguments("-r", "30/1")
                .addArguments("-b:v", "10000000")
                .addArguments("-acodec", "aac")
                .addArguments("-b:a", "192000")
                .addArguments("-map", "[aud]")
                .addArguments("-map", "[v]")
                .addArguments("-preset", "fast")
        );

        fFmpeg.setOutputListener(new OutputListener() {
            @Override
            public void onOutput(String s) {
                statusService.sendLogUpdate(s);
                LocalTime currentTime = getLocalTimeFromLog(s);
                if (currentTime != LocalTime.MIN) {
                    double progress = ((double) currentTime.toNanoOfDay() / finalLoc.toNanoOfDay());
//                    statusService.sendDetailStatus("Video und Audio Zusammensetzen", progress);
                    statusService.sendFullDetail(StatusKeys.VIDEO_GENERATION, progress, "");
                }
            }
        });
        fFmpeg.execute();
    }


    /**
     * Concat List of audioSegments and fade the individual sections out and in
     *
     * @param output            target output file
     * @param audioSegments     the List of paths to the audioSegments, each video requires one audio and one video track
     * @param crossFadeDuration the fade duration in seconds
     */
    public void concatAudio(Path output, List<Path> audioSegments, double crossFadeDuration) {
        FFmpeg fFmpeg = FFmpeg
                .atPath()
                .setOverwriteOutput(true);
        for (Path video : audioSegments) {
            fFmpeg.addInput(UrlInput.fromPath(video));
        }
        fFmpeg
                .addArguments("-filter_complex", concatAudioCrossFadeFilterBuilder(audioSegments.size(), crossFadeDuration) + ";[a]loudnorm=" + videoConfiguration.getLoudnormParameter() + " [aud]");

        fFmpeg.addOutput(UrlOutput.toPath(output)
//                .addArguments("-acodec", "libmp3lame")
//                .addArguments("-b:a", "192000")
                        .addArguments("-map", "[aud]")
                        .addArguments("-preset", "fast")
        );

        fFmpeg.setOutputListener(new OutputListener() {
            @Override
            public void onOutput(String s) {
                System.out.println(s);
            }
        });
        fFmpeg.execute();
    }

    public void convertToWav(Path input, Path output) {
        FFmpeg fFmpeg = FFmpeg
                .atPath()
                .setOverwriteOutput(true)
                .addInput(UrlInput.fromPath(input))
                .addOutput(UrlOutput.toPath(output));

        fFmpeg.execute();
    }


    public LocalTime getDurationLocalTime(Path path) {
        if (Files.exists(path)) {
            FFprobeResult res = FFprobe.atPath()
                    .setShowStreams(true)
                    .setInput(path)
                    .execute();
            for (com.github.kokorin.jaffree.ffprobe.Stream stream : res.getStreams()) {
                if (stream.getCodecType().equals(StreamType.VIDEO)) {
                    float l = stream.getDuration() * 1000000000;
                    return LocalTime.ofNanoOfDay((long) l);
                }
            }
        }
        return LocalTime.MIN.plusNanos(1);
    }


    public Double getFrameRate(Path path) {
        FFprobeResult res = FFprobe.atPath()
                .setShowStreams(true)
                .setInput(path)
                .execute();
        for (com.github.kokorin.jaffree.ffprobe.Stream stream : res.getStreams()) {
            if (stream.getCodecType().equals(StreamType.VIDEO)) {
                return stream.getAvgFrameRate().doubleValue();
            }
        }
        return null;
    }

    /**
     * Command builder for complex filter to concat video and audio
     *
     * @param number number of videos to concat
     * @return returns the concat String
     */
    private String concatVideoFilterBuilder(int number) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < number; i++) {
            sb
                    .append("[").append(i).append(":v]")
                    .append("[").append(i).append(":a]");
        }

        sb.append(" concat=n=").append(number).append(":v=1:a=1 [v] [a]");

        return sb.toString();
    }

    /**
     * Command builder for concating video with fade in and out on the middle parts
     *
     * @param paths    list of paths for the videos
     * @param duration the fade duration in seconds
     * @return retunrns the complex filter concatfade String
     */
    public String concatFadeVideoFilterBuilder(List<Path> paths, double duration) {
        String sr = "";
        List<LocalTime> list = paths.stream().map(this::getDurationLocalTime).toList();
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                sr += "[" + i + ":v]fade=type=out:duration=" + duration + ":start_time=" + (getSecondsWithPeriod(list.get(i)) - duration) + ",setpts=PTS-STARTPTS[v" + i + "];";
                sr += "[" + i + ":a]afade=t=out:st=" + (getSecondsWithPeriod(list.get(i)) - duration) + ":d=" + duration + "[a" + i + "];";
            } else if (i == list.size() - 1) {
                sr += "[" + i + ":v]fade=type=in:duration=" + duration + ",setpts=PTS-STARTPTS[v" + i + "];";
                sr += "[" + i + ":a]afade=t=in:d=" + duration + "[a" + i + "];";
            } else {
                sr += "[" + i + ":v]fade=type=in:duration=" + duration + ",fade=type=out:duration=" + duration + ":start_time=" + (getSecondsWithPeriod(list.get(i)) - duration) + ",setpts=PTS-STARTPTS[v" + i + "];";
                sr += "[" + i + ":a]afade=t=in:d=" + duration + ",afade=t=out:st=" + (getSecondsWithPeriod(list.get(i)) - duration) + ":d=" + duration + "[a" + i + "];";
            }
        }


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.size(); i++) {
            sb
                    .append("[v").append(i).append("]")
                    .append("[a").append(i).append("]");
        }

        sb.append(" concat=n=").append(paths.size()).append(":v=1:a=1 [v] [a]");


        return sr + sb.toString();
    }

    /**
     * Command builder for complex filter for concating audio with crossfade
     *
     * @param numberOfFiles the number of audio segments
     * @param duration      the corssfade duration
     * @return the complex filter input
     */
    public String concatAudioCrossFadeFilterBuilder(int numberOfFiles, double duration) {
        StringBuilder command = new StringBuilder();
        if (numberOfFiles > 1) {
            command = new StringBuilder("[0][1]acrossfade=d=" + duration + ":c1=tri:c2=tri");
            for (int i = 2; i < numberOfFiles; i++) {
                command.append("[a").append(i - 1).append("];")
                        .append("[a").append(i - 1).append("][").append(i).append("]acrossfade=d=").append(duration).append(":c1=tri:c2=tri");
            }
            command.append("[a]");
        }
        return command.toString();
    }


    /**
     * Turns LocalTime to Seconds
     *
     * @param localTime the to convert LocalTime
     * @return seconds
     */
    private double getSecondsWithPeriod(LocalTime localTime) {
        return (localTime.toNanoOfDay() / 1000000000.0);
    }


    public void generateImageFromVideo(LocalTime start, LocalTime end, Path video, Path outDir, double frameRate) {
        LocalTime finalLoc = end.minusNanos(start.toNanoOfDay());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
        FFmpeg
                .atPath()
                .addInput(UrlInput.fromPath(video)
                        .addArguments("-ss", start.format(dateTimeFormatter))
                        .addArguments("-to", end.format(dateTimeFormatter)))
                .addOutput(UrlOutput.toPath(outDir.resolve("out000%d.bmp"))
//                        .addArguments("-vsync", "0")
                        .addArguments("-qscale:v", "4")
//                        .addArgument("-copyts")
                        .addArguments("-vf", "fps=" + frameRate))
                .setOutputListener(s -> {
                    statusService.sendLogUpdate(s);
                    LocalTime currentTime = getLocalTimeFromLog(s);
                    if (currentTime != LocalTime.MIN) {
                        double progress = ((double) currentTime.toNanoOfDay() / finalLoc.toNanoOfDay());
                        statusService.sendFullDetail(StatusKeys.VIDEO_THUMBNAIL_PREPARE, progress, "");
                    }
                })
                .execute();
    }

    public void convertToPodcastMp3WithMetadata(Path input, Path albumart, Path output, WorshipMetaData worshipMetaData) throws IOException {
        Files.deleteIfExists(output);
        FFmpeg
                .atPath()
                .addInput(UrlInput.fromPath(input))
                .addInput(UrlInput.fromPath(albumart))
                .addOutput(UrlOutput.toPath(output)
                        .addArguments("-map", "0:0")
                        .addArguments("-map", "1:0")
                        .addArguments("-c", "copy")
                        .addArguments("-id3v2_version", "4")
                        .addArguments("-codec:a", "libmp3lame")
                        .addArguments("-b:a", "128k")
                        .addArguments("-metadata", "artist=" + worshipMetaData.getPerson().getFirstName() + " " + worshipMetaData.getPerson().getLastName())
                        .addArguments("-metadata", "title=" + worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()))
                        .addArguments("-metadata", "genre=" + "Predigt")
                        .addArguments("-metadata", "album=" + worshipMetaData.getCampusShortname())
                        .addArguments("-metadata", "year=" + worshipMetaData.getStartDate().getYear())

                        .addArguments("-metadata:s:v", "title=" + "Album cover")
                        .addArguments("-metadata:s:v", "title=" + "Cover (front)")
                        .addArguments("-pix_fmt", "yuv420p")
                        .addArguments("-metadata", "TIT3=" + worshipMetaData.getServiceTitle(worshipMetaData.getServiceLanguage()) + " (" + worshipMetaData.getPerson().getFirstName() + " " + worshipMetaData.getPerson().getLastName() + ")")
                )
                .setOutputListener(new OutputListener() {
                    @Override
                    public void onOutput(String s) {
                        System.out.println(s);
                    }
                })
                .execute();

    }

    public LocalTime getLocalTimeFromLog(String log) {
        Matcher matcher = LOG_TIME_REGEX.matcher(log);
        if (matcher.find()) {
            return LocalTime.parse(matcher.group("time"));
        }
        return LocalTime.MIN;
    }

}
