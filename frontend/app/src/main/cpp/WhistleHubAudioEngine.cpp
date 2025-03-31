//
// Created by SSAFY on 2025-03-25.
//

#include "WhistleHubAudioEngine.h"
#include "DrumSynth.h"
#include "WavLoader.h"
#include <oboe/Oboe.h>
#include <memory>
#include <jni.h>
#include <cmath>

#define LOG_TAG "WhistleHubAudioEngine"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

WhistleHubAudioEngine::WhistleHubAudioEngine() = default; //생성자

WhistleHubAudioEngine::~WhistleHubAudioEngine() {     //소멸자
    stopAudioStream();
}

void WhistleHubAudioEngine::startAudioStream() {

    mTotalFrameRendered = 0;
    mPreviousBar = -1;

    for (auto& layer : mLayers) {
        layer.isActive = false;
        layer.currentSampleIndex = 0;
    }

    std::shared_ptr<oboe::AudioStreamBuilder> builder = std::make_shared<oboe::AudioStreamBuilder>();

    builder->setPerformanceMode(oboe::PerformanceMode::LowLatency)
            ->setDirection(oboe::Direction::Output)  // 출력 스트림
            ->setChannelCount(2)  // 스테레오
            ->setSampleRate(44100)  // CD 품질
            ->setFormat(oboe::AudioFormat::Float)
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

oboe::DataCallbackResult WhistleHubAudioEngine::onAudioReady(oboe::AudioStream *oboeStream,
                                                             void *audioData,
                                                             int32_t numFrames) {
   if(stream == nullptr) return oboe::DataCallbackResult::Continue;


   auto* outputBuffer = static_cast<float *>(audioData);
    renderAudio(outputBuffer, numFrames);

    return oboe::DataCallbackResult::Continue;
}


void WhistleHubAudioEngine::log(const char *message) {
    LOGI("%s", message);
}

void WhistleHubAudioEngine::logError(const char *message) {
    LOGE("%s", message);
}


std::vector<LayerAudioInfo> WhistleHubAudioEngine::parseLayerList(JNIEnv *env, jobject layerList) {
    std::vector<LayerAudioInfo> layers;

    jclass listClass = env->GetObjectClass(layerList);
    jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
    jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");

    jint listSize = env->CallIntMethod(layerList, sizeMethod);

    for (int i = 0; i < listSize; i++) {
        jobject layerObj = env->CallObjectMethod(layerList, getMethod, i);
        jclass layerClass = env->GetObjectClass(layerObj);

        jmethodID getPathMethod = env->GetMethodID(layerClass, "getWavPath", "()Ljava/lang/String;");
        jstring jPath = (jstring) env->CallObjectMethod(layerObj, getPathMethod);
        const char *pathChars = env->GetStringUTFChars(jPath, nullptr);
        std::string path(pathChars);
        env->ReleaseStringUTFChars(jPath, pathChars);

        // Get patternBlocks
        jmethodID getPatternBlocksMethod = env->GetMethodID(layerClass, "getPatternBlocks","()Ljava/util/List;");
        jobject patternList = env->CallObjectMethod(layerObj, getPatternBlocksMethod);

        std::vector<PatternBlock> patternBlocks;

        jclass plistClass = env->GetObjectClass(patternList);
        jmethodID psizeMethod = env->GetMethodID(plistClass, "size", "()I");
        jmethodID pgetMethod = env->GetMethodID(plistClass, "get", "(I)Ljava/lang/Object;");

        jint psize = env->CallIntMethod(patternList, psizeMethod);

        for (int j = 0; j < psize; j++) {
            jobject pbObj = env->CallObjectMethod(patternList, pgetMethod, j);
            jclass pbClass = env->GetObjectClass(pbObj);

            jmethodID getStartMethod = env->GetMethodID(pbClass, "getStart", "()I");
            jmethodID getLengthMethod = env->GetMethodID(pbClass, "getLength", "()I");

            int start = env->CallIntMethod(pbObj, getStartMethod);
            int length = env->CallIntMethod(pbObj, getLengthMethod);

            patternBlocks.push_back({static_cast<float>(start), static_cast<float>(length)});
            env->DeleteLocalRef(pbObj);
        }
        env->DeleteLocalRef(jPath);
        env->DeleteLocalRef(layerObj);
        layers.push_back({path, patternBlocks});
    }

    return layers;
}

void WhistleHubAudioEngine::setLayers(const std::vector<LayerAudioInfo> &layers) {
    mLayers.clear();
    mTotalFrameRendered = 0;

    int layerId = 0;
    for(const auto& info : layers) {
        Layer layer;
        layer.id = layerId++;
        layer.samplePath = info.path;
        layer.patternBlocks = info.patternBlock;

        LOGI("📦 Loading Layer ID: %d", layer.id);
        LOGI("📄 Path: %s", layer.samplePath.c_str());
        LOGI("📊 Pattern Count: %zu", layer.patternBlocks.size());

        for (const auto &pb : layer.patternBlocks) {
            LOGI("🎵 PatternBlock: start = %.2f, length = %.2f", pb.start, pb.length);
        }

        if (WavLoader::load(info.path, layer)) {
            LOGI("✅ WAV loaded. Samples: %zu", layer.sampleBuffer.size());
            layer.isActive = false;
            layer.currentSampleIndex = 0;
            mLayers.push_back(std::move(layer));
        } else {
            LOGE("❌ Failed to load WAV: %s", info.path.c_str());
        }
    }
}

void WhistleHubAudioEngine::renderAudio(float *outputBuffer, int32_t numFrames) {
    std::fill(outputBuffer, outputBuffer + numFrames * 2, 0.0f); //스테레오 초기화

    float seconds = mTotalFrameRendered / static_cast<float>(mSampleRate);
    float barsPerSecond = mBpm / 60.0f / 4.0f;
    float currentBar = seconds * barsPerSecond;

    if (currentBar >= mLoopLengthInBeats) return;

    int barIndex = static_cast<int>(currentBar);
    if (barIndex != mPreviousBar && barIndex < mLoopLengthInBeats) {
        LOGI("🎼 현재 마디 = %d", barIndex);
        mPreviousBar = barIndex;
    }

    for (auto& layer: mLayers) {
        for (const auto& block : layer.patternBlocks) {
            if (currentBar >= block.start && currentBar < block.start + block.length) {
                float barOffset = currentBar - block.start;
                float secondsOffset = barOffset * 4.0f * 60.0f / mBpm;
                int startSample = static_cast<int>(secondsOffset * mSampleRate);
                int bufferIndex = startSample * layer.numChannels;

                for (int i = 0; i < numFrames; ++i) {
                    for (int ch = 0; ch < layer.numChannels && ch < 2; ++ch) {
                        int idx = bufferIndex + i * layer.numChannels + ch;
                        if (idx < layer.sampleBuffer.size()) {
                            outputBuffer[i * 2 + ch] += layer.sampleBuffer[idx] / 32768.0f;
                        }
                    }
                }

//                LOGI("▶️ Layer %d: block %.2f ~ %.2f 재생 중", layer.id, block.start, block.start + block.length);
            }
        }
    }

    for (int i = 0; i < numFrames * 2; ++i) {
        outputBuffer[i] = std::clamp(outputBuffer[i], -1.0f, 1.0f);
    }

    mTotalFrameRendered += numFrames;
}
