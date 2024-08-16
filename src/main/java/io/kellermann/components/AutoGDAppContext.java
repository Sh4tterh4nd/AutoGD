package io.kellermann.components;

import io.kellermann.config.VideoConfiguration;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AutoGDAppContext {
    private VideoConfiguration videoConfiguration;

    public AutoGDAppContext(VideoConfiguration videoConfiguration) {
        this.videoConfiguration = videoConfiguration;
    }

    @Bean
    public FFmpeg generateFfmpeg() {
        try {
            return new FFmpeg(videoConfiguration.getFfmpegLocation());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    @Bean
    public FFprobe generateFfprobe() {
        try {
            return new FFprobe(videoConfiguration.getFfprobeLocation());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

}
