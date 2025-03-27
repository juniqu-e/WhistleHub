#include <jni.h>
#include "WhistleHubAudioEngine.h"
#include <android/log.h>

#define LOG_TAG "whistlehub"
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

//전역 오디오 엔진 인스턴스
static WhistleHubAudioEngine engine;

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