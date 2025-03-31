//
// Created by SSAFY on 2025-03-27.
//

#include "DrumSynth.h"
#include <cmath>
#include <cstdlib>
#include <random>
#include <memory>
#include <android/log.h>

#define LOG_TAG "WhistleHubAudioEngine"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

constexpr float PI = 3.1415927f;

void DrumSynth::generateKick(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.3f;
    const int totalFrames = static_cast<int>(sampleRate * duration);

    layer.sampleBuffer.resize(totalFrames * 2);

    std::random_device rd;
    std::mt19937 gen(rd());
    std::normal_distribution<float> noiseDist(0.0f, 0.2f);

    for (int i = 0; i < totalFrames; ++i) {
        float t = static_cast<float>(i) / sampleRate;

        //드롭킥 느낌: 주파수 점점 낮아짐
        float freq = 150.0f * expf(-t * 8.0f);  // 초반은 높고 점점 낮아짐
        float env = expf(-t * 6.0f);            // 느린 감쇠로 묵직하게

        //Fade-in
        float fadeIn = std::pow(std::min(1.0f, static_cast<float>(i) / 512.0f), 2.0f);

        //Fade-out
        float fadeOut = std::pow(std::min(1.0f, static_cast<float>(totalFrames - i) / 512.0f), 2.0f);


        float tone = sinf(2.0f * PI * freq * t);        // 기본 킥 톤
        float sub = sinf(2.0f * PI * 50.0f * t) * 0.3f;  // 서브 톤 (저음 강화)
        float noise = noiseDist(gen);           // 부드러운 노이즈
        float wave = (tone + sub + noise) * env * fadeIn * fadeOut;

        // Soft Clipping (거친 디지털 clipping 방지)
        float softClipped = tanhf(wave) * 0.8f;

        auto sample = static_cast<int16_t>(std::clamp(softClipped * 32767.0f, -32768.0f, 32767.0f));
        layer.sampleBuffer[i * 2] = sample;
        layer.sampleBuffer[i * 2 + 1] = sample;

    }
    layer.lengthSeconds = duration;
    layer.sampleRate = sampleRate;
    layer.numChannels = 2;
}

void DrumSynth::generateSnare(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.3f;
    const int totalFrames = static_cast<int>(sampleRate * duration);

    layer.sampleBuffer.resize(totalFrames * 2);

    std::random_device rd;
    std::mt19937 gen(rd());
    std::normal_distribution<float> dist(0.0f, 1.0f);

    for (int i = 0; i < totalFrames; ++i) {
        float t = static_cast<float>(i) / sampleRate;
        float env = expf(-t * 25.0f);  // 빠른 감쇠

        // 노이즈 + 톤 있는 스냅
        float noise = dist(gen);
        float tone = sinf(2.0f * PI * 300.0f * t); // 저음 톤 추가
        float mixed = (0.6f * noise + 0.4f * tone) * env;

        auto sample = static_cast<int16_t>(mixed * 32767.0f);
        layer.sampleBuffer[i * 2] = sample;
        layer.sampleBuffer[i * 2 + 1] = sample;
    }

    layer.lengthSeconds = duration;
    layer.sampleRate = sampleRate;
    layer.numChannels = 2;
}

void DrumSynth::generateHiHat(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.07f;
    const int totalFrames = static_cast<int>(sampleRate * duration);

    layer.sampleBuffer.resize(totalFrames * 2);

    std::random_device rd;
    std::mt19937 gen(rd());
    std::normal_distribution<float> dist(0.0f, 0.7f);

    float lastSample = 0.0f;
    const float cutoff = 0.6f;  // Low-pass filter 강하게 적용

    for (int i = 0; i < totalFrames; ++i) {
        float t = static_cast<float>(i) / sampleRate;
        float env = expf(-t * 70.0f) * (1.0f - t);

        // 노이즈 + 로우패스 필터로 날카로움 제거
        float noise = dist(gen);

        float filtered = cutoff * noise + (1.0f - cutoff) * lastSample;
        lastSample = filtered;

        float tone = sinf(2.0f * PI * 8000.0f * t) * 0.2f;

        float final = (0.65f * filtered + 0.35f * tone) * env * 0.8f;

        auto sample = static_cast<int16_t>(final * 32767.0f);

        layer.sampleBuffer[i * 2] = sample;
        layer.sampleBuffer[i * 2 + 1] = sample;
    }

    layer.lengthSeconds = duration;
    layer.sampleRate = sampleRate;
    layer.numChannels = 2;
}

void DrumSynth::generateHiHatStrong(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.07f;
    const int totalFrames = static_cast<int>(sampleRate * duration);

    layer.sampleBuffer.resize(totalFrames * 2);

    std::random_device rd;
    std::mt19937 gen(rd());
    std::normal_distribution<float> dist(0.0f, 0.7f);

    float lastSample = 0.0f;
    const float cutoff = 0.4f;  // 더 날카롭게

    for (int i = 0; i < totalFrames; ++i) {
        float t = static_cast<float>(i) / sampleRate;
        float env = expf(-t * 70.0f) * (1.0f - t);

        float noise = dist(gen);
        float filtered = cutoff * noise + (1.0f - cutoff) * lastSample;
        lastSample = filtered;

        float tone = sinf(2.0f * PI * 9000.0f * t) * 0.2f;

        float final = (0.7f * filtered + 0.3f * tone) * env * 0.9f;

        auto sample = static_cast<int16_t>(final * 32767.0f);
        layer.sampleBuffer[i * 2] = sample;
        layer.sampleBuffer[i * 2 + 1] = sample;
    }

    layer.lengthSeconds = duration;
    layer.sampleRate = sampleRate;
    layer.numChannels = 2;
}

void DrumSynth::generateHiHatSoft(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.07f;
    const int totalFrames = static_cast<int>(sampleRate * duration);

    layer.sampleBuffer.resize(totalFrames * 2);

    std::random_device rd;
    std::mt19937 gen(rd());
    std::normal_distribution<float> dist(0.0f, 0.5f);  // 덜 거칠게

    float lastSample = 0.0f;
    const float cutoff = 0.7f;  // 더 부드럽게

    for (int i = 0; i < totalFrames; ++i) {
        float t = static_cast<float>(i) / sampleRate;
        float env = expf(-t * 60.0f) * (1.0f - t);

        float noise = dist(gen);
        float filtered = cutoff * noise + (1.0f - cutoff) * lastSample;
        lastSample = filtered;

        float tone = sinf(2.0f * PI * 7000.0f * t) * 0.15f;

        float final = (0.6f * filtered + 0.4f * tone) * env * 0.7f;

        auto sample = static_cast<int16_t>(final * 32767.0f);
        layer.sampleBuffer[i * 2] = sample;
        layer.sampleBuffer[i * 2 + 1] = sample;
    }

    layer.lengthSeconds = duration;
    layer.sampleRate = sampleRate;
    layer.numChannels = 2;
}

void DrumSynth::addGroovyHiHatToBars(Layer& strong, Layer& soft, const std::vector<int>& bars, int bpm, int resolution) {
    float beatsPerBar = 8.0f;
    float step = beatsPerBar / static_cast<float>(resolution);

    for (int bar: bars) {
        float baseBeat = static_cast<float>(bar) * beatsPerBar;
        for (int i = 0; i < resolution; ++i) {
            float beat = baseBeat + static_cast<float>(i) * step;
//            LOGI("[HiHat] bar %d → beat %.2f", bar, beat);
            if (i % 2 == 0)
                strong.patternBlocks.push_back({beat, step});
            else
                soft.patternBlocks.push_back({beat, step});
        }
    }
}

