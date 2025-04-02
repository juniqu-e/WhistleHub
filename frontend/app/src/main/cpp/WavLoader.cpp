#define DR_WAV_IMPLEMENTATION

#include "dr_wav.h"

#include "WavLoader.h"
#include <android/log.h>

bool WavLoader::load(const std::string& path, Layer& layer) {
    drwav wav;
    if (!drwav_init_file(&wav, path.c_str(), nullptr)) {
        return false;
    }

    size_t totalFrames = wav.totalPCMFrameCount;
    size_t totalSamples = totalFrames * wav.channels;

    layer.sampleBuffer.resize(totalSamples);
    drwav_read_pcm_frames_s16(&wav, totalFrames, layer.sampleBuffer.data());
    drwav_uninit(&wav);

    layer.sampleRate = wav.sampleRate;
    layer.numChannels = wav.channels;
    layer.lengthSeconds = static_cast<float>(totalFrames) / wav.sampleRate;

    return true;
}
