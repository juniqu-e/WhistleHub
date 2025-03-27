//
// Created by SSAFY on 2025-03-27.
//

#include "DrumSynth.h"
#include <cmath>
#include <cstdlib>
#include <random>

constexpr float PI = 3.1415927f;

void DrumSynth::generateKick(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.8f;
    const int totalFrames = static_cast<int>(sampleRate * duration);

    layer.sampleBuffer.resize(totalFrames * 2);

    for (int i = 0; i < totalFrames; ++i) {
        float t = static_cast<float>(i) / sampleRate;

        //드롭킥 느낌: 주파수 점점 낮아짐
        float freq = 150.0f * expf(-t * 8.0f);  // 초반은 높고 점점 낮아짐
        float env = expf(-t * 6.0f);            // 느린 감쇠로 묵직하게

        //Fade-in (앞 256 프레임)
        float fadeIn = (i < 256) ? static_cast<float>(i) / 256.0f : 1.0f;

        //Fade-out (뒤 512 프레임)
        float fadeOut = (i > totalFrames - 512)
                        ? 1.0f - static_cast<float>(i - (totalFrames - 512)) / 512.0f
                        : 1.0f;

        float wave = sinf(2.0f * PI * freq * t) * env * fadeIn * fadeOut;

        // clipping 방지
        auto sample = static_cast<int16_t>(std::clamp(wave * 32767.0f, -32768.0f, 32767.0f));
        layer.sampleBuffer[i * 2] = sample;
        layer.sampleBuffer[i * 2 + 1] = sample;
    }
    layer.lengthSeconds = duration;
    layer.sampleRate = sampleRate;
    layer.numChannels = 2;
}

void DrumSynth::generateSnare(Layer &layer) {
    const int sampleRate = 44100;
    const float duration = 0.4f;
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
