package com.whistlehub.workstation.data

enum class InstrumentType(val label: String, val assetFolder: String) {
    DRUM("Drum", "drum"),
    BASS("Bass", "bass"),
    SYNTH("Synth", "synth"),
    FX("FX", "fx")
}
