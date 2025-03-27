//
// Created by SSAFY on 2025-03-25.
//
#pragma once

#ifndef WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H
#define WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H

#include <oboe/Oboe.h>
#include <android/log.h>
#include <memory>
#include "AudioLayer.h"


class WhistleHubAudioEngine : public oboe::AudioStreamCallback {
public :
    WhistleHubAudioEngine();

    ~WhistleHubAudioEngine();

    // Method to start the audio stream
    void startAudioStream();

    // Method to stop the audio stream
    void stopAudioStream();

    oboe::DataCallbackResult onAudioReady(
            oboe::AudioStream *audioStream,
            void *audioData,
            int32_t numFrames
    ) override;

private :
    std::shared_ptr<oboe::AudioStream> stream;  // Oboe audio stream
    int32_t currentFramePosition = 0;
    int bpm = 120;

    void log(const char *message);

    void logError(const char *message);

    std::vector<Layer> layers;
};


#endif //WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H
