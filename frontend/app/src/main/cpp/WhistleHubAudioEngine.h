//
// Created by SSAFY on 2025-03-25.
//

#ifndef WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H
#define WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H

#include <oboe/Oboe.h>
#include <android/log.h>
#include <memory>


class WhistleHubAudioEngine {
public :
    WhistleHubAudioEngine();

    ~WhistleHubAudioEngine();

    // Method to start the audio stream
    void startAudioStream();

    // Method to stop the audio stream
    void stopAudioStream();

    // Method to play a specific layer (instrument)
    void playLayer(const std::string &layerName);

private :
    std::shared_ptr<oboe::AudioStream> stream;  // Oboe audio stream
};


#endif //WHISTLEHUB_WHISTLEHUBAUDIOENGINE_H
