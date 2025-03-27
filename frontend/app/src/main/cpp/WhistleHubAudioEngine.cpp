//
// Created by SSAFY on 2025-03-25.
//

#include "WhistleHubAudioEngine.h"
#include "DrumSynth.h"
#include <oboe/Oboe.h>
#include <memory>

#define LOG_TAG "WhistleHubAudioEngine"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

WhistleHubAudioEngine::WhistleHubAudioEngine() = default; //생성자

WhistleHubAudioEngine::~WhistleHubAudioEngine() {     //소멸자
    stopAudioStream();
}

void WhistleHubAudioEngine::startAudioStream() {
    //샘플 오디오
//    if (layers.empty()) {
//        Layer testLayer;
//        testLayer.id = 1;
//        testLayer.samplePath = "sine";
//
//        const int sampleRate = 44100;
//        const float freq = 440.0f;
//        int numFrames = sampleRate;
//
//        testLayer.sampleBuffer.resize(numFrames * 2);
//
//        for (int i = 0; i < numFrames; i++) {
//            float t = static_cast<float>(i) / sampleRate;
//            auto sample = static_cast<int16_t>(sin(t * 2 * M_PI * freq) * 32767.0f);
//
//            testLayer.sampleBuffer[i * 2] = sample;
//            testLayer.sampleBuffer[i * 2 + 1] = sample;
//        }
//
//        testLayer.lengthSeconds = 1.0f;
//        testLayer.sampleRate = sampleRate;
//        testLayer.numChannels = 2;
//
//        for (int i = 0; i < 60; i += 4) {
//            testLayer.patternBlocks.push_back(PatternBlock{i, 2});
//        }
//
//        layers.push_back(testLayer);
//    }
    const int totalBeats = 60;  // 총 마디 수

    // Kick Layer 생성
    Layer kick;
    kick.id = 1;
    DrumSynth::generateKick(kick);

    //  Snare Layer 생성
    Layer snare;
    snare.id = 2;
    DrumSynth::generateSnare(snare);

    //  Hi-hat Layer 생성
    Layer hihat;
    hihat.id = 3;
    DrumSynth::generateHiHat(hihat);

    //붐뱁
    std::vector<int> kickPattern = {0, 4};
    std::vector<int> snarePattern = {3, 7};
    std::vector<int> hihatPattern = {0, 1, 2, 3, 4, 5, 6, 7};


    // 반복 적용 (8박씩 마디 반복)
    for (int base = 0; base < totalBeats; base += 8) {
        for (int k: kickPattern) kick.patternBlocks.push_back({base + k, 1});
        for (int s: snarePattern) snare.patternBlocks.push_back({base + s, 1});
        for (int h: hihatPattern) hihat.patternBlocks.push_back({base + h, 1});
    }

    // 🎛 레이어 등록
    layers.push_back(kick);
    layers.push_back(snare);
    layers.push_back(hihat);


    std::shared_ptr<oboe::AudioStreamBuilder> builder = std::make_shared<oboe::AudioStreamBuilder>();

    builder->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDirection(oboe::Direction::Output)  // 출력 스트림
            ->setChannelCount(2)  // 스테레오
            ->setSampleRate(44100)  // CD 품질
            ->setFormat(oboe::AudioFormat::I16)  // 16비트 정수 형식
            ->setCallback(this);
    // 오디오 스트림 열기
    oboe::Result result = builder->openStream(stream);
    if (result != oboe::Result::OK) {
        LOGE("Failed to open stream: %s", oboe::convertToText(result));
        return;
    }

    // 스트림 시작
    result = stream->requestStart();
    if (result != oboe::Result::OK) {
        LOGE("Failed to start stream: %s", oboe::convertToText(result));
        return;
    }

    LOGE("Audio stream started successfully!");
}

void WhistleHubAudioEngine::stopAudioStream() {
    if (stream) {
        // 스트림을 멈추고 종료
        stream->stop();
        stream->close();
        stream.reset(); // 메모리 안전 해제
        LOGE("Audio stream stopped and closed");

    }
}

oboe::DataCallbackResult WhistleHubAudioEngine::onAudioReady(
        oboe::AudioStream *audioStream,
        void *audioData,
        int32_t numFrames
) {
    const float beatDuration = 60.0f / static_cast<float>(bpm);

    // 누적된 프레임으로 현재 시간 계산
    currentFramePosition += numFrames;
    float currentTimeSec = static_cast<float>(currentFramePosition) / stream->getSampleRate();

    // 루프용: 60마디 안에서 반복
    const int maxBeats = 60;
    const float loopDurationSec = maxBeats * beatDuration;

    float loopedTime = fmod(currentTimeSec, loopDurationSec);  // 반복 시간
    int currentBeat = static_cast<int>(loopedTime / beatDuration);

    const float masterVolume = 0.8f;

    auto *output = static_cast<int16_t *>(audioData);
    memset(output, 0, sizeof(int16_t) * numFrames * 2);  // 스테레오 silence 초기화

    for (auto &layer: layers) {
        // 루프 안에서 패턴이 활성화되는지 확인
        bool shouldBeActive = false;

        for (const auto &block: layer.patternBlocks) {
            float beatStart = static_cast<float>(block.start) * beatDuration;
            float beatEnd = beatStart + layer.lengthSeconds;
            //            float beatEnd = (block.start + block.length) * beatDuration;

            if (loopedTime >= beatStart && loopedTime < beatEnd) {
                shouldBeActive = true;

                if (!layer.isActive) {
                    layer.isActive = true;
                    layer.currentSampleIndex = 0;  // 루프 시작 시 초기화
                    LOGI("[Layer %d] Loop START @ beat %d", layer.id, currentBeat);
                }
                break;
            }
        }

        if (!shouldBeActive && layer.isActive) {
            layer.isActive = false;
            layer.currentSampleIndex = 0;
            LOGI("[Layer %d] Loop END @ beat %d", layer.id, currentBeat);
        }
    }

    // 오디오 믹싱
    for (int i = 0; i < numFrames; ++i) {
        float sampleL = 0.0f;
        float sampleR = 0.0f;

        for (auto &layer: layers) {
            if (!layer.isActive) continue;

            int idx = layer.currentSampleIndex + i * 2;
            if (idx + 1 >= layer.sampleBuffer.size()) continue;

            sampleL += static_cast<float>(layer.sampleBuffer[idx]) * 0.5f;
            sampleR += static_cast<float>(layer.sampleBuffer[idx + 1]) * 0.5f;
        }

        // 마스터 볼륨 + 클리핑 후 저장
        sampleL *= masterVolume;
        sampleR *= masterVolume;

        output[i * 2] = static_cast<int16_t>(std::clamp(sampleL, -32768.0f, 32767.0f));
        output[i * 2 + 1] = static_cast<int16_t>(std::clamp(sampleR, -32768.0f, 32767.0f));
    }

    // 각 레이어의 sample index 업데이트
    for (auto &layer: layers) {
        if (layer.isActive) {
            layer.currentSampleIndex += numFrames * 2;

            if (layer.currentSampleIndex >= layer.sampleBuffer.size()) {
                layer.isActive = false;
                layer.currentSampleIndex = 0;
                LOGI("[Layer %d] DONE @ beat %d", layer.id, currentBeat);
            }
        }
    }

    return oboe::DataCallbackResult::Continue;
}


void WhistleHubAudioEngine::log(const char *message) {
    LOGI("%s", message);
}

void WhistleHubAudioEngine::logError(const char *message) {
    LOGE("%s", message);
}