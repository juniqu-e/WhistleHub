#include <jni.h>
#include "WhistleHubAudioEngine.h"
#include "WavLoader.h"
#include "dr_wav.h"
#include <android/log.h>
#include <vector>
#include <sstream> // stringstream을 사용하여 출력

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
    }
    g_callback = env->NewGlobalRef(listener);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_whistlehub_common_util_AudioEngineBridge_generateWaveformPoints(JNIEnv *env, jobject thiz, jstring wavFilePath) {
    // C++에서 파일 경로를 받기
    const char *filePath = env->GetStringUTFChars(wavFilePath, nullptr);

    // WAV 파일을 읽어 파형 데이터를 생성
    std::vector<float> waveformPoints = engine.generateWaveformData(filePath);

    // 각 원소를 문자열로 변환하여 출력
    std::stringstream ss;
    for (size_t i = 0; i < waveformPoints.size(); ++i) {
        ss << waveformPoints[i] << " ";
    }

    // 문자열로 변환된 파형 데이터를 로그로 출력
    LOGE("Waveform Points: %s", ss.str().c_str());

    // List<Float>로 변환하기 위해 Java의 ArrayList 생성
    jclass listClass = env->FindClass("java/util/ArrayList");
    jmethodID listConstructor = env->GetMethodID(listClass, "<init>", "()V");
    jobject waveformPointsList = env->NewObject(listClass, listConstructor);

    // 파형 데이터 (std::vector<float>)를 Kotlin 리스트로 변환
    jmethodID listAddMethod = env->GetMethodID(listClass, "add", "(Ljava/lang/Object;)Z");
    for (float point : waveformPoints) {
        jfloat pointData = point;
        jobject floatObj = env->NewObject(env->FindClass("java/lang/Float"), env->GetMethodID(env->FindClass("java/lang/Float"), "<init>", "(F)V"), pointData);
        env->CallBooleanMethod(waveformPointsList, listAddMethod, floatObj);
        env->DeleteLocalRef(floatObj);  // 로컬 참조 해제
    }

    // Kotlin의 updateWaveformPoints 메서드 호출
    if (g_callback) {
        jclass callbackClass = env->GetObjectClass(g_callback);  // listener 객체 (Kotlin의 ViewModel)
        jmethodID updateWaveformPointsMethod = env->GetMethodID(callbackClass, "updateWaveformPoints", "(Ljava/util/List;)V");
        env->CallVoidMethod(g_callback, updateWaveformPointsMethod, waveformPointsList);

        // 메모리 해제
        env->DeleteLocalRef(callbackClass);
    }

    // 메모리 해제
    env->ReleaseStringUTFChars(wavFilePath, filePath);
    env->DeleteLocalRef(listClass);
}
