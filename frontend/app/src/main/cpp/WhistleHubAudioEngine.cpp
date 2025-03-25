//
// Created by SSAFY on 2025-03-25.
//

#include "WhistleHubAudioEngine.h"
#include <oboe/Oboe.h>
#include <memory>

#define LOG_TAG "WhistleHubAudioEngine"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

WhistleHubAudioEngine::WhistleHubAudioEngine() {
    //생성자
}

WhistleHubAudioEngine::~WhistleHubAudioEngine() {
    //소멸자
    stopAudioStream();
}

void WhistleHubAudioEngine::startAudioStream() {
    std::shared_ptr<oboe::AudioStreamBuilder> builder = std::make_shared<oboe::AudioStreamBuilder>();

    builder->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDirection(oboe::Direction::Output)  // 출력 스트림
            ->setChannelCount(2)  // 스테레오
            ->setSampleRate(44100)  // CD 품질
            ->setFormat(oboe::AudioFormat::I16);  // 16비트 정수 형식
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
        LOGE("Audio stream stopped and closed");
    }
}