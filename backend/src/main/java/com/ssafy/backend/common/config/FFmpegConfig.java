package com.ssafy.backend.common.config;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class FFmpegConfig {
    @Value("${FFMPEG_LOCATION}")
    private String ffmpegLocation;
    @Value("${FFPROBE_LOCATION}")
    private String ffprobeLocation;

    @Bean(name = "ffmpeg")
    public FFmpeg ffmpeg() throws IOException {
        FFmpeg ffmpeg = new FFmpeg(ffmpegLocation);
        return ffmpeg;
    }

    @Bean(name = "ffprobe")
    public FFprobe ffprobe() throws IOException {
        FFprobe ffprobe = new FFprobe(ffprobeLocation);
        return ffprobe;
    }

}
