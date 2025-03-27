//
// Created by SSAFY on 2025-03-27.
//
#pragma once

#include "AudioLayer.h"

#ifndef WHISTLEHUB_DRUMSYNTH_H
#define WHISTLEHUB_DRUMSYNTH_H


class DrumSynth {
public:
    static void generateKick(Layer &layer);

    static void generateSnare(Layer &layer);

    static void generateHiHat(Layer &layer);
};


#endif //WHISTLEHUB_DRUMSYNTH_H
