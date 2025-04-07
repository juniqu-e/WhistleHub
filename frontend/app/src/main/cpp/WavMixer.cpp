
#include "dr_wav.h"
#include "WhistleHubAudioEngine.h"
#include <android/log.h>

#define LOG_TAG "WhistleHubAudioEngine"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)

bool WhistleHubAudioEngine::renderToFile(const std::string &outputPath, int32_t totalFrames) {

    if (mLayers.empty()) {
        LOGE("❌ 믹싱 실패: 활성 레이어가 없습니다.");
        return false;
    }

    std::vector<float> mixBuffer(totalFrames * 2, 0.0f);  // 스테레오 float 버퍼
    constexpr
    int32_t kBufferSize = 512;

    // 기존 renderAudio 로직 복사 → loopLength 대신 총 frame 수만큼 반복
    mTotalFrameRendered = 0;
    for (int32_t offset = 0; offset < totalFrames; offset += kBufferSize) {
        int32_t framesToRender = std::min(kBufferSize, totalFrames - offset);
        renderAudio(mixBuffer.data() + offset * 2, framesToRender);
    }

    // float → int16 변환
    std::vector<int16_t> finalPCM(totalFrames * 2);
    for (size_t i = 0; i < finalPCM.size(); ++i) {
        float sample = std::clamp(mixBuffer[i], -1.0f, 1.0f);
        finalPCM[i] = static_cast<int16_t>(sample * 32767.0f);
    }

    // dr_wav로 쓰기
    drwav_data_format format;
    format.container = drwav_container_riff;
    format.format = DR_WAVE_FORMAT_PCM;
    format.channels = 2;
    format.sampleRate = mSampleRate;
    format.bitsPerSample = 16;

    drwav wav;
    if (!drwav_init_file_write(&wav, outputPath.c_str(), &format, nullptr)) {
        return false;
    }

    drwav_uint64 framesWritten = drwav_write_pcm_frames(&wav, totalFrames, finalPCM.data());
    drwav_uninit(&wav);

    return framesWritten == totalFrames;
}