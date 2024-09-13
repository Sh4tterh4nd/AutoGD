package io.kellermann.services.video;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.github.kokorin.jaffree.ffprobe.FFprobe;
import com.github.kokorin.jaffree.ffprobe.FFprobeResult;
import io.kellermann.config.VideoConfiguration;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class JaffreeFFmpegService {

    private VideoConfiguration videoConfiguration;

    public JaffreeFFmpegService(VideoConfiguration videoConfiguration) {
        this.videoConfiguration = videoConfiguration;
    }

    public void imageToVideo(Path imagePath, Path videoOutput, double fadeDuration) {
        FFmpeg
                .atPath()
                .addInput(UrlInput.fromPath(imagePath)
                        .addArguments("-f", "lavfi")
                        .addArguments("-i", "anullsrc=r=44100:cl=stereo")
                        .addArguments("-loop", "1")
                        .addArguments("-pix_fmt", "yuv420p")
                        .addArguments("-framerate", "30")
                        .addArguments("-t", String.valueOf(fadeDuration)))
                .addOutput(UrlOutput.toPath(videoOutput)
                        .setFrameRate(30)
                        .setCodec(StreamType.VIDEO, "libx264")
                        .setCodec(StreamType.AUDIO, "aac")
                        .addArguments("-vf", "fade=out:st=" + (fadeDuration - 0.5) + ":d=0.5")
                        .setFormat("mp4")
                        .addArgument("-shortest")
                )
                .setOverwriteOutput(true)
                .setOutputListener(new OutputListener() {
                    @Override
                    public void onOutput(String s) {
                        System.out.println(s);
                    }
                })
                .execute();
    }

    public void concatVideoAndMergeAudio(Path output, Path audio, List<Path> videos) {
        System.out.println(concatFadeFilter(videos, 5));
        FFmpeg fFmpeg = FFmpeg.atPath();
        videos.forEach(s -> fFmpeg.addInput(UrlInput.fromPath(s)));
        fFmpeg.addInput(UrlInput.fromPath(audio));
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
                        System.out.println(s);
                    }
                })
                .execute();
    }

    public void concatVideoAndMergeAudio(Path output, Path audio, Path... videos) {
        concatVideoAndMergeAudio(output, audio, Arrays.asList(videos));
    }


    public void cutVideo(LocalTime start, LocalTime end, Path input, Path output) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SS");
        FFmpeg
                .atPath()
                .addInput(UrlInput.fromPath(input)
                        .addArguments("-ss", start.format(dateTimeFormatter))
                        .addArguments("-to", end.format(dateTimeFormatter)))
                .addOutput(UrlOutput.toPath(output)
                        .setCodec(StreamType.VIDEO, "copy")
                        .setCodec(StreamType.AUDIO, "copy")
                        .addArguments("-b:a", "192k"))
                .setOutputListener(new OutputListener() {
                    @Override
                    public void onOutput(String s) {
                        System.out.println(s);
                    }
                })
                .execute();

    }

    public void concatVideo(Path output, double fadeDuration, Path... videos) {
        concatVideo(output, Arrays.asList(videos), fadeDuration);
    }

    public void concatVideo(Path output, List<Path> videos, double fadeDuration) {
        FFmpeg fFmpeg = FFmpeg
                .atPath()
                .setOverwriteOutput(true);
        fFmpeg.addInput(UrlInput.fromPath(videos.get(0))
                .addArguments("-hwaccel", "cuda")
                .addArguments("-hwaccel_output_format", "cuda"));
        for (int i = 1; i < videos.size(); i++) {
            fFmpeg.addInput(UrlInput.fromPath(videos.get(i)));
        }
        fFmpeg
                .addArguments("-filter_complex", concatFadeFilter(videos, fadeDuration) + ";[a]loudnorm=" + videoConfiguration.getLoudnormParameter() + " [aud]");

        fFmpeg.addOutput(UrlOutput.toPath(output)
                .addArguments("-vcodec", videoConfiguration.getCodec())
                .addArguments("-r", "30/1")
                .addArguments("-b:v", "14000000")
                .addArguments("-acodec", "aac")
                .addArguments("-b:a", "192000")
                .addArguments("-map", "[aud]")
                .addArguments("-map", "[v]")
                .addArguments("-preset", "fast")
        );
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
        return null;
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

    public String concatFadeFilter(List<Path> paths, double duration) {
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

    private double getSecondsWithPeriod(LocalTime localTime) {
        return (localTime.toNanoOfDay() / 1000000000.0);
    }
}
