package com.ssafy.backend.ai.sound.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIGeneratedSoundData {
    // 음악의 기본 정보
    private String title;
    private String description;
    private int bpm; // Beats per minute
    private String keySignature; // 조성 (예: C Major, A Minor)

    // 악기 및 트랙 정보
    private List<Track> tracks;

    // 전체 구조 정보
    private List<Section> sections;

    // 믹싱 및 마스터링 설정
    private MixingSettings mixingSettings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Track {
        private String instrument;
        private int midiChannel;
        private int volume; // 0-127
        private int pan; // -64~63(좌우)
        private List<Note> notes;
        private List<ControlChange> controlChanges;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Note {
        private int pitch; // MIDI 노트 번호(0-127)
        private int velocity; // 노트 강도(0-127)
        private long startTick; // 시작 위치(틱)
        private long duration; // 지속 시간(틱)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ControlChange {
        private int type; // 컨트롤 변경 유형(예: 7=볼륨)
        private int value; // 컨트롤 값(0-127)
        private long tick; // 위치(틱)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String name; // 섹션 이름(예: 인트로, 버스, 코러스)
        private long startTick; // 시작 위치(틱)
        private long endTick; // 종료 위치(틱)
        private int repeatCount; // 반복 횟수
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MixingSettings {
        private int masterVolume; // 마스터 볼륨(0-127)
        private int reverb; // 리버브 양(0-127)
        private int delay; // 딜레이 양(0-127)
        private int chorus; // 코러스 양(0-127)
    }
}
