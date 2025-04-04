//
// Created by SSAFY on 2025-03-25.
//
#pragma once

#ifndef WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H
#define WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H

#include <oboe/Oboe.h>
#include <android/log.h>
#include <memory>
#include <jni.h>
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

    std::vector <LayerAudioInfo> parseLayerList(JNIEnv *env, jobject layerList);

    void setLayers(const std::vector <LayerAudioInfo> &layers);

    void renderAudio(float *outputBuffer, int32_t numFrames);

    bool renderToFile(const std::string &outputPath, int32_t totalFrames);

private :
    std::shared_ptr<oboe::AudioStream> stream;  // Oboe audio stream
    int32_t currentFramePosition = 0;
    int bpm = 120;
    bool isFirstRender = true;

    void log(const char *message);

    void logError(const char *message);

    std::vector <Layer> mLayers;
    int mSampleRate = 44100;
    float mBpm = 120.0f;
    double mTotalFrameRendered = 0;
    float mLoopLengthInBeats = 64.0f;

    float mMaxBars = 64.0f;
    int mPreviousBar = -1;
};


#endif //WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H
