#include <jni.h>
#include "WhistleHubAudioEngine.h"
#include "WavLoader.h"
#include "dr_wav.h"
#include <android/log.h>

#define LOG_TAG "whistlehub"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//전역 오디오 엔진 인스턴스
static WhistleHubAudioEngine engine;
jobject g_callback = nullptr;
JavaVM* g_vm = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_whistlehub_MainActivity_startAudioEngine(JNIEnv *env, jobject) {
    engine.startAudioStream();
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_whistlehub_MainActivity_stopAudioEngine(JNIEnv *env, jobject) {
    engine.stopAudioStream();
    return 0;
}

//extern "C" JNIEXPORT jint
//
//JNICALL
//Java_com_whistlehub_MainActivity_startAudioEngine(JNIEnv *env, jobject) {
//    oboe::AudioStreamBuilder builder;
//    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
//
//    std::shared_ptr<oboe::AudioStream> stream;
//    oboe::Result result = builder.openStream(stream);
//    if (result != oboe::Result::OK) {
//        LOGE("Failed to open stream: %s", oboe::convertToText(result));
//        return -1;
//    }
//    result = stream->requestStart();
//    if (result != oboe::Result::OK) {
//        LOGE("Failed to start stream: %s", oboe::convertToText(result));
//        return -1;
//    }
//    // stream 객체는 실제 프로젝트에서는 전역 또는 적절한 클래스 멤버로 관리해야 합니다.
//    return 0;
//}
//
//extern "C" JNIEXPORT jint
//
//JNICALL
//Java_com_whistlehub_MainActivity_stopAudioEngine(JNIEnv *env, jobject /* this */) {
//    // 오디오 스트림 정지 및 종료 로직 구현
//    return 0;
//}
extern "C"
JNIEXPORT jint

JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_startAudioEngine(JNIEnv *env, jobject thiz) {
    engine.startAudioStream();
    return 0;
}

extern "C"
JNIEXPORT jint

JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_stopAudioEngine(JNIEnv *env, jobject thiz) {
    engine.stopAudioStream();
    return 0;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_setLayers(JNIEnv *env, jobject thiz, jobject layers, jint maxUsedBars) {
    std::vector<LayerAudioInfo> parsed = engine.parseLayerList(env, layers);
    engine.setLayers(parsed, static_cast<float>(maxUsedBars));
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_renderMixToWav(JNIEnv* env, jobject, jstring jPath, jint totalFrames) {
    const char* path = env->GetStringUTFChars(jPath, nullptr);
    bool result = engine.renderToFile(path, totalFrames);
    env->ReleaseStringUTFChars(jPath, path);
    return result;
}

extern "C"
JNIEXPORT jfloat JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_getWavDurationSeconds(
        JNIEnv *env,
        jobject /* this */,
        jstring path) {

    const char *cPath = env->GetStringUTFChars(path, nullptr);

    drwav wav;
    float duration = -1.0f;

    if (drwav_init_file(&wav, cPath, nullptr)) {
        duration = wav.totalPCMFrameCount / static_cast<float>(wav.sampleRate);
        drwav_uninit(&wav);
    }

    env->ReleaseStringUTFChars(path, cPath);
    return duration;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_setCallback(JNIEnv *env, jobject thiz, jobject listener) {
    if (g_callback) {
        env->DeleteGlobalRef(g_callback);
        g_callback = nullptr;
    }
    g_callback = env->NewGlobalRef(listener);
}