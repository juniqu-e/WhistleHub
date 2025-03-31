//
// Created by SSAFY on 2025-03-27.
//
#pragma once

#include <vector>
#include <string>

#ifndef WHISTLEHUB_AUDIOLAYER_H
#define WHISTLEHUB_AUDIOLAYER_H


struct PatternBlock {
    float start;   // 시작 마디 번호
    float length;  // 마디 길이
};

struct Layer {
    int id;
    std::string samplePath;                 // 샘플 경로
    float lengthSeconds = 0.0f;

    std::vector<PatternBlock> patternBlocks;
    std::vector<int16_t> sampleBuffer;      // 오디오 PCM 데이터
    int sampleRate = 44100;
    int numChannels = 2;

    // 재생 상태 관리
    bool isActive = false;
    int currentSampleIndex = 0;
};

struct LayerAudioInfo {
    std::string path;
    std::vector <PatternBlock> patternBlock;
};

#endif //WHISTLEHUB_AUDIOLAYER_H
