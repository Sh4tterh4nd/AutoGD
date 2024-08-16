package io.kellermann.services;

import io.kellermann.config.VideoConfiguration;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FfmpegService {
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;

    private VideoConfiguration videoConfiguration;

    public FfmpegService(FFmpeg ffmpeg, FFprobe ffprobe, VideoConfiguration videoConfiguration) {
        this.ffmpeg = ffmpeg;
        this.ffprobe = ffprobe;
        this.videoConfiguration = videoConfiguration;
    }


    public Path generateIntroImageVideo(Path imagePath) {
        System.out.println("Generating Intro Image Video");
        Path outputPath = imagePath.getParent().resolve(videoConfiguration.getTempWorkspace()).resolve("tmp_intro.mp4");
        imageToVideo(imagePath, outputPath, 3);
        return outputPath;
    }


    private void imageToVideo(Path theImagePath, Path outputPath, double duration) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .setInput(theImagePath.toFile().toString())
                .addExtraArgs("-f", "lavfi")
                .addExtraArgs("-i", "anullsrc=r=44100:cl=stereo")
                .addExtraArgs("-loop", "1")
                .addExtraArgs("-pix_fmt", "yuv420p")
                .addExtraArgs("-framerate", "30")
                .addExtraArgs("-t", String.valueOf(duration))
//                .addExtraArgs("-strict","experimental")
                .addOutput(outputPath.toFile().toString())
                .setVideoFrameRate(FFmpeg.FPS_30)
                .setVideoCodec("libx264")
                .addExtraArgs("-shortest")
                .addExtraArgs("-vf", "fade=out:st=" + (duration - 0.5) + ":d=0.5")
//                .addExtraArgs("-af", "afade=out:st=" + (duration - 0.5) + ":d=0.5")
                .setFormat("mp4")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // Run a one-pass encode
        executor.createJob(builder).run();
    }

    public void concatVideoWithReEncode(Path outputPath, Path... videos) {
        concatVideoWithReEncode(Arrays.asList(videos), outputPath);
    }

    public void concatVideoWithReEncode(List<Path> videos, Path output) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true);
        for (Path video : videos) {
            builder
                    .addInput(video.toFile().toString());
        }
        builder
                .setComplexFilter(concatVideoFilterBuilder(videos.size()))
                .addExtraArgs("-hwaccel", "cuda")
                .addExtraArgs("-hwaccel_output_format", "cuda")
                .addOutput(output.toFile().toString())
                .addExtraArgs("-map", "[a]")
                .addExtraArgs("-map", "[v]")
                .setVideoCodec("h264_nvenc")
                .addExtraArgs("-preset", "fast")
                .setAudioCodec("aac")
                .setAudioBitRate(192_000)
                .setVideoBitRate(20_000_000)
                .setVideoFrameRate(30)
                .done();


        ProgressListener listener = new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                printProgress("Merge Videos:", progress);
            }
        };


        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // Run a one-pass encode
        executor.createJob(builder, listener).run();
    }

    public Path concatVideoMergeAudio(Path audioToMerge, Path... videos) {
        System.out.println("Generating intro video");
        List<Path> list = Arrays.asList(videos);
        return concatVideoMergeAudio(list, audioToMerge);
    }

    public Path concatVideoMergeAudio(List<Path> videos, Path audioMerge) {
        Path output = Paths.get(videoConfiguration.getInputWorkspace()).resolve(videoConfiguration.getTempWorkspace()).resolve("0_intro.mp4");

        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true);
        for (Path video : videos) {
            builder
                    .addInput(video.toFile().toString());
        }
        builder.addInput(audioMerge.toFile().toString());

        builder.setComplexFilter(concatVideoFilterBuilder(videos.size()) + ";[a][" + videos.size() + ":a] amix=inputs=2:duration=longest [aOut] ")
                .addOutput(output.toFile().toString())
                .addExtraArgs("-map", "[aOut]")
                .addExtraArgs("-map", "[v]")
                .setVideoCodec("libx264")
                .setAudioCodec("aac");
        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);


        // Run a one-pass encode
        executor.createJob(builder).run();

        return output;
    }

    public void concatVideoNoEncoding(Path output, Path... videos) {
        Path jobFile = Paths.get(videoConfiguration.getInputWorkspace()).resolve(videoConfiguration.getTempWorkspace()).resolve("jobFile.txt");
        List<String> jobList = new ArrayList<>();
        for (Path video : videos) {
            jobList.add("file '"+video.toFile().toString()+"'");
        }
        try {
            Files.write(jobFile,jobList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .setInput(jobFile.toFile().toString())
                .addExtraArgs("-f", "concat")
                .addExtraArgs("-safe", "0")
                .addOutput(output.toFile().toString())
                .addExtraArgs("-c", "copy")
                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        ProgressListener listener = new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                printProgress("Merge Videos:", progress);
            }
        };

        // Run a one-pass encode
        executor.createJob(builder,listener).run();
    }

    private void addAudio(Path inputVideo, Path inputAudio, String outputName) {
        Path video = Paths.get(videoConfiguration.getInputWorkspace()).resolve("tmp").resolve(outputName + ".mp4");
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addInput(inputVideo.toFile().toString())
                .addInput(inputAudio.toFile().toString())
                .setComplexFilter("[0:a][1:a] amix=inputs=2:duration=longest [aOut]")
                .addOutput(video.toFile().toString())
                .addExtraArgs("-map", "0:v")
                .addExtraArgs("-map", "[aOut]")

                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // Run a one-pass encode
        executor.createJob(builder).run();
    }

    private void mergeAudio(List<Path> audios) {
        Path video = Paths.get(videoConfiguration.getInputWorkspace()).resolve("tmp").resolve(".mp4");
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true);
        for (Path audio : audios) {
            builder
                    .addOutput(audio.toFile().toString());
        }

        builder
                .addOutput(video.toFile().toString())

                .done();

        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);

        // Run a one-pass encode
        executor.createJob(builder).run();
    }

    public Path cutVideoAndAudioGain(Path video, String start, String end, String loudnormTarget) {
        Path output = Paths.get(videoConfiguration.getInputWorkspace()).resolve(videoConfiguration.getTempWorkspace()).resolve("1_gdPartGain.mp4");
        FFmpegBuilder builder = new FFmpegBuilder()
                .overrideOutputFiles(true)
                .addInput(video.toFile().toString())
                .addExtraArgs("-ss", start)
                .addExtraArgs("-to", end)
                .addOutput(output.toFile().toString())
                .setVideoCodec("copy")
                .setAudioFilter("loudnorm=" + loudnormTarget)
                .setAudioCodec("aac")
                .setAudioBitRate(192_000)
                .done();
        ProgressListener listener = new ProgressListener() {
            @Override
            public void progress(Progress progress) {
                printProgress("Audio Gain Analysis:", progress);
            }
        };


        FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
        // Run a one-pass encode
        executor.createJob(builder, listener).run();

        return output;
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


    private void printProgress(String prefix, Progress progress) {
        try {
            System.out.println(prefix + " Frame: " + progress.frame + " Time: " + formatTimeNs(progress.out_time_ns) + " Speed: " + progress.speed);
        } catch (Exception ex) {
            progress.toString();
        }
    }

    private String formatTimeNs(Long i) {
        LocalTime localTime = LocalTime.ofNanoOfDay(i);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        return dtf.format(localTime);
    }
}
