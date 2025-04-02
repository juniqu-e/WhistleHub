//
// Created by SSAFY on 2025-03-28.
//

#ifndef WHISTLEHUB_WAVLOADER_H
#define WHISTLEHUB_WAVLOADER_H

#pragma once

#include "AudioLayer.h"
#include <string>

class WavLoader {
public:
    static bool load(const std::string& path, Layer& layer);
};

#endif //WHISTLEHUB_WAVLOADER_H
