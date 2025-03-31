//
// Created by SSAFY on 2025-03-27.
//
#pragma once

#include "AudioLayer.h"
#include <android/log.h>

#ifndef WHISTLEHUB_DRUMSYNTH_H
#define WHISTLEHUB_DRUMSYNTH_H


class DrumSynth {
public:
    static void generateKick(Layer &layer);

    static void generateSnare(Layer &layer);

    static void generateHiHat(Layer &layer);

    static void generateHiHatStrong(Layer &layer);

    static void generateHiHatSoft(Layer &layer);

    static void addGroovyHiHatToBars(
            Layer& strong,
            Layer& soft,
            const std::vector<int>& bars,
            int bpm,
            int resolution = 8
    );
};


#endif //WHISTLEHUB_DRUMSYNTH_H
