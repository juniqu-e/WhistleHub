//
// Created by SSAFY on 2025-03-25.
//

#include "WhistleHubAudioEngine.h"
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
    if (layers.empty()) {
        Layer testLayer;
        testLayer.id = 1;
        testLayer.samplePath = "sine";

        const int sampleRate = 44100;
        const float freq = 440.0f;
        int numFrames = sampleRate;

        testLayer.sampleBuffer.resize(numFrames * 2);

        for (int i = 0; i < numFrames; i++){
            float t = static_cast<float>(i) / sampleRate;
            auto sample = static_cast<int16_t>(sin(t * 2 * M_PI * freq) * 32767.0f);

            testLayer.sampleBuffer[i * 2] = sample;
            testLayer.sampleBuffer[i * 2 + 1] = sample;
        }
    }

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
    LOGI("onAudioReady called - frames: %d", numFrames);
    // silence 출력
    memset(audioData, 0, sizeof(int16_t) * numFrames * 2); // stereo = 2채널

    // TODO: 재생할 샘플 계산 및 믹싱 처리
    currentFramePosition += numFrames;

    return oboe::DataCallbackResult::Continue;
}

void WhistleHubAudioEngine::log(const char *message) {
    LOGI("%s", message);
}

void WhistleHubAudioEngine::logError(const char *message) {
    LOGE("%s", message);
}