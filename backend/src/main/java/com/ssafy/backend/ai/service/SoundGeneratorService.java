package com.ssafy.backend.ai.service;

import com.ssafy.backend.ai.sound.dto.AIGeneratedSoundData;
import com.ssafy.backend.ai.sound.dto.request.SoundGenerationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sound.midi.*;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoundGeneratorService {

    private final AiService aiService;
    @Value("${FLUIDSYNTH_PATH}")
    private String FLUIDSYNTH_PATH;
    @Value("${SOUNDFONT_PATH}")
    private String SOUNDFONT_PATH;
    // 악기 이름과 MIDI 프로그램 번호 매핑 테이블
    private static final Map<String, Integer> INSTRUMENT_MAP = new HashMap<>();

    static {
        INSTRUMENT_MAP.put("piano", 0);
        INSTRUMENT_MAP.put("acoustic_piano", 0);
        INSTRUMENT_MAP.put("grand_piano", 0);
        INSTRUMENT_MAP.put("electric_piano", 4);
        INSTRUMENT_MAP.put("acoustic_guitar", 24);
        INSTRUMENT_MAP.put("electric_guitar", 27);
        INSTRUMENT_MAP.put("bass", 33);
        INSTRUMENT_MAP.put("acoustic_bass", 32);
        INSTRUMENT_MAP.put("electric_bass", 33);
        INSTRUMENT_MAP.put("violin", 40);
        INSTRUMENT_MAP.put("viola", 41);
        INSTRUMENT_MAP.put("cello", 42);
        INSTRUMENT_MAP.put("trumpet", 56);
        INSTRUMENT_MAP.put("saxophone", 65);
        INSTRUMENT_MAP.put("tenor_sax", 66);
        INSTRUMENT_MAP.put("alto_sax", 65);
        INSTRUMENT_MAP.put("flute", 73);
        INSTRUMENT_MAP.put("drums", 118);
    }

    public InputStream generateSound(SoundGenerationRequest request) {
        try {
            AIGeneratedSoundData soundData = aiService.generateSoundData(request);

            // 요청된 길이에 맞게 MIDI 데이터 검증 및 조정
            validateAndAdjustSoundData(soundData, request);

            Sequence sequence = createMidiSequence(soundData, request.getTempo());

            // MIDI 파일 생성
            File tempMidiFile = File.createTempFile("temp", ".mid");
            tempMidiFile.deleteOnExit();
            MidiSystem.write(sequence, MidiSystem.getMidiFileTypes(sequence)[0], tempMidiFile);

            // WAV 파일 생성
            File tempWavFile = File.createTempFile("temp", ".wav");
            tempWavFile.deleteOnExit();

            // FluidSynth를 사용하여 MIDI를 WAV로 변환 - 개선된 설정
            ProcessBuilder pb = new ProcessBuilder(
                    FLUIDSYNTH_PATH,
                    "-ni",
                    "-g", "1.0", // 게인 설정
                    SOUNDFONT_PATH,
                    tempMidiFile.getAbsolutePath(),
                    "-F",
                    tempWavFile.getAbsolutePath(),
                    "-r", "44100",
                    "--reverb", "0", // 리버브 비활성화
                    "--chorus", "0" // 코러스 비활성화
            );

            Process process = pb.start();
            process.waitFor();

            // 생성된 WAV 파일 길이 조정
            File adjustedWavFile = adjustWavFileDuration(tempWavFile, request.getDurationSeconds());

            // WAV 파일을 InputStream으로 변환
            return new FileInputStream(adjustedWavFile);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("음원 생성 실패", e);
        }
    }
    /**
     * AIGeneratedSoundData의 길이 검증 및 조정
     */
    private void validateAndAdjustSoundData(AIGeneratedSoundData soundData, SoundGenerationRequest request) {
        int pulsesPerQuarterNote = 480; // PPQ 값
        double beatsPerSecond = request.getTempo() / 60.0; // BPS
        long expectedTotalTicks = (long) (request.getDurationSeconds() * beatsPerSecond * pulsesPerQuarterNote);

        // 현재 생성된 데이터에서 가장 마지막 노트의 종료 틱 계산
        long actualEndTick = 0;

        for (AIGeneratedSoundData.Track track : soundData.getTracks()) {
            for (AIGeneratedSoundData.Note note : track.getNotes()) {
                long noteEndTick = note.getStartTick() + note.getDuration();
                if (noteEndTick > actualEndTick) {
                    actualEndTick = noteEndTick;
                }
            }
        }

        // 생성된 데이터가 예상 길이와 5% 이상 차이가 있는 경우 조정
        double difference = Math.abs(1.0 - (double)actualEndTick / expectedTotalTicks);
        if (difference > 0.05) {
            log.info("조정 필요: 예상 길이 {} 틱, 실제 길이 {} 틱", expectedTotalTicks, actualEndTick);

            // 스케일링 비율 계산
            double scaleFactor = (double) expectedTotalTicks / actualEndTick;

            // 모든 트랙의 노트 시작 시간과 길이 조정
            for (AIGeneratedSoundData.Track track : soundData.getTracks()) {
                for (AIGeneratedSoundData.Note note : track.getNotes()) {
                    note.setStartTick((long) (note.getStartTick() * scaleFactor));
                    note.setDuration((long) (note.getDuration() * scaleFactor));
                }

                // 컨트롤 체인지 조정
                if (track.getControlChanges() != null) {
                    for (AIGeneratedSoundData.ControlChange cc : track.getControlChanges()) {
                        cc.setTick((long) (cc.getTick() * scaleFactor));
                    }
                }
            }

            // 섹션 정보 조정
            if (soundData.getSections() != null) {
                for (AIGeneratedSoundData.Section section : soundData.getSections()) {
                    section.setStartTick((long) (section.getStartTick() * scaleFactor));
                    section.setEndTick((long) (section.getEndTick() * scaleFactor));
                }
            }

            log.info("노트 데이터 조정 완료. 스케일링 비율: {}", scaleFactor);
        }
    }
    /**
     * WAV 파일 길이 조정
     */
    private File adjustWavFileDuration(File wavFile, int requestedDurationSeconds) throws Exception {
        // 현재 WAV 파일의 길이 확인
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(wavFile);
        AudioFormat format = audioInputStream.getFormat();
        long frameLength = audioInputStream.getFrameLength();
        double durationInSeconds = frameLength / format.getFrameRate();
        audioInputStream.close();

        log.info("WAV 파일 길이: {} 초, 요청 길이: {} 초", durationInSeconds, requestedDurationSeconds);

        // 허용 오차 범위 내에 있으면 조정하지 않음
        double tolerance = 0.1; // 0.1초 오차 허용
        if (Math.abs(durationInSeconds - requestedDurationSeconds) <= tolerance) {
            return wavFile;
        }

        // 정확한 길이로 WAV 파일 조정 필요
        File adjustedWavFile = File.createTempFile("adjusted", ".wav");
        adjustedWavFile.deleteOnExit();

        // 오디오 처리를 통한 파일 길이 조정
        AudioInputStream originalStream = AudioSystem.getAudioInputStream(wavFile);
        int targetFrameLength = (int) (requestedDurationSeconds * format.getFrameRate());

        // 원본보다 짧게 조정하는 경우
        if (targetFrameLength < frameLength) {
            AudioInputStream trimmedStream = new AudioInputStream(
                    originalStream,
                    format,
                    targetFrameLength
            );
            AudioSystem.write(trimmedStream, AudioFileFormat.Type.WAVE, adjustedWavFile);
            trimmedStream.close();
        }
        // 원본보다 길게 조정하는 경우
        else if (targetFrameLength > frameLength) {
            // 무음을 추가하는 방식
            byte[] audioData = new byte[(int) frameLength * format.getFrameSize()];
            originalStream.read(audioData);

            byte[] adjustedData = new byte[targetFrameLength * format.getFrameSize()];
            System.arraycopy(audioData, 0, adjustedData, 0, audioData.length);

            ByteArrayInputStream bais = new ByteArrayInputStream(adjustedData);
            AudioInputStream adjustedStream = new AudioInputStream(
                    bais,
                    format,
                    targetFrameLength
            );

            AudioSystem.write(adjustedStream, AudioFileFormat.Type.WAVE, adjustedWavFile);
            adjustedStream.close();
        }

        originalStream.close();
        return adjustedWavFile;
    }

    private Sequence createMidiSequence(AIGeneratedSoundData soundData, int tempo) throws InvalidMidiDataException {
        Sequence sequence = new Sequence(Sequence.PPQ, 480);
        Track tempoTrack = sequence.createTrack();
        setTempo(tempoTrack, tempo);

        for (int i = 0; i < soundData.getTracks().size(); i++) {
            AIGeneratedSoundData.Track trackData = soundData.getTracks().get(i);
            Track track = sequence.createTrack();
            int channel = determineChannel(trackData, i);
            setInstrument(track, channel, trackData.getInstrument());
            setVolume(track, channel, trackData.getVolume());
            setPan(track, channel, trackData.getPan());
            addControlChanges(track, channel, trackData.getControlChanges());
            addNotes(track, channel, trackData.getNotes());
        }

        return sequence;
    }

    private void setTempo(Track track, int tempo) throws InvalidMidiDataException {
        int mpqn = 60000000 / tempo;
        MetaMessage tempoMessage = new MetaMessage(0x51, new byte[]{
                (byte) ((mpqn >> 16) & 0xFF),
                (byte) ((mpqn >> 8) & 0xFF),
                (byte) (mpqn & 0xFF)
        }, 3);
        track.add(new MidiEvent(tempoMessage, 0));
    }

    private int determineChannel(AIGeneratedSoundData.Track trackData, int trackIndex) {
        int channel = trackData.getMidiChannel();
        if (channel == 9 && !trackData.getInstrument().toLowerCase().contains("drum")) {
            channel = trackIndex % 15;
            log.warn("채널 9는 드럼용으로 예약되어 있어 {} 악기에 채널 {}를 할당합니다.", trackData.getInstrument(), channel);
        }
        return channel;
    }

    private void setInstrument(Track track, int channel, String instrumentName) throws InvalidMidiDataException {
        int instrumentNumber = getInstrumentNumber(instrumentName);
        ShortMessage programChange = new ShortMessage(ShortMessage.PROGRAM_CHANGE, channel, instrumentNumber, 0);
        track.add(new MidiEvent(programChange, 0));
        log.info("악기 {} 설정: MIDI 프로그램 번호 {}", instrumentName, instrumentNumber);
    }

    private void setVolume(Track track, int channel, int volume) throws InvalidMidiDataException {
        ShortMessage volumeChange = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 7, volume);
        track.add(new MidiEvent(volumeChange, 0));
    }

    private void setPan(Track track, int channel, int pan) throws InvalidMidiDataException {
        ShortMessage panChange = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, 10, pan + 64);
        track.add(new MidiEvent(panChange, 0));
    }

    private void addControlChanges(Track track, int channel, java.util.List<AIGeneratedSoundData.ControlChange> controlChanges) throws InvalidMidiDataException {
        if (controlChanges != null) {
            for (AIGeneratedSoundData.ControlChange cc : controlChanges) {
                ShortMessage controlChange = new ShortMessage(ShortMessage.CONTROL_CHANGE, channel, cc.getType(), cc.getValue());
                track.add(new MidiEvent(controlChange, cc.getTick()));
            }
        }
    }

    private void addNotes(Track track, int channel, java.util.List<AIGeneratedSoundData.Note> notes) throws InvalidMidiDataException {
        for (AIGeneratedSoundData.Note note : notes) {
            ShortMessage noteOn = new ShortMessage(ShortMessage.NOTE_ON, channel, note.getPitch(), note.getVelocity());
            track.add(new MidiEvent(noteOn, note.getStartTick()));
            ShortMessage noteOff = new ShortMessage(ShortMessage.NOTE_OFF, channel, note.getPitch(), 0);
            track.add(new MidiEvent(noteOff, note.getStartTick() + note.getDuration()));
        }
    }

    private int getInstrumentNumber(String instrumentName) {
        if (instrumentName == null || instrumentName.isEmpty()) {
            log.warn("악기 이름이 비어 있습니다. 기본값으로 피아노(0)를 사용합니다.");
            return 0;
        }

        String normalizedName = instrumentName.toLowerCase().replaceAll("[\\s-_]", "");

        // 정확히 일치하는 악기 찾기
        Integer instrumentNumber = INSTRUMENT_MAP.get(normalizedName);
        if (instrumentNumber != null) {
            return instrumentNumber;
        }

        // 부분 문자열로 찾기
        for (Map.Entry<String, Integer> entry : INSTRUMENT_MAP.entrySet()) {
            if (normalizedName.contains(entry.getKey()) || entry.getKey().contains(normalizedName)) {
                return entry.getValue();
            }
        }

        // 유사한 악기 찾기
        if (normalizedName.contains("string")) return 48; // String Ensemble 1
        if (normalizedName.contains("brass")) return 61; // Brass Section
        if (normalizedName.contains("wind")) return 73; // Flute
        if (normalizedName.contains("synth")) return 80; // Lead 1 (square)

        log.warn("알 수 없는 악기 이름: {}. 기본값으로 피아노(0)를 사용합니다.", instrumentName);
        return 0; // 기본값으로 피아노 설정
    }
}
/* 악기 매핑 참고.
000-000 Piano 1
000-001 Piano 2
000-002 Piano 3
000-003 Honky-Tonk
000-004 E Piano 1
000-005 E Piano 2
000-006 Harpsichord
000-007 Clavinet
000-008 Celesta
000-009 Glockenspiel
000-010 Music Box
000-011 Vibraphone
000-012 Marimba
000-013 Xylophone
000-014 Tubular Bell
000-015 Santur
000-016 Organ 1
000-017 Organ 2
000-018 Organ 3
000-019 Church Org 1
000-020 Reed Organ
000-021 Accordian
000-022 Harmonica
000-023 Bandneon
000-024 Nylon str.Gt
000-025 Steel-str.Gt
000-026 Jazz Gt.
000-027 Clean Gt.
000-028 Muted Gt.
000-029 Overdrive Gt
000-030 Distortion G
000-031 Gt.Harmonics
000-032 Accoustic Bs
000-033 Fingered Bs.
000-034 Picked Bs.
000-035 Fretless Bs.
000-036 Slap Bass 1
000-037 Slap Bass 2
000-038 Synth Bass 1
000-039 Synth Bass 2
000-040 Violin
000-041 Viola
000-042 Cello
000-043 Contrabass
000-044 Tremelo Stri
000-045 Pizz Strings
000-046 Harp
000-047 Timpani
000-048 Strings
000-049 Slow Strings
000-050 Synth String
000-051 Synth Str 2
000-052 Choir Aahs
000-053 Voice Oohs
000-054 Synth Voice
000-055 Orchestra Ht
000-056 Trumpet
000-057 Trombone
000-058 Tuba
000-059 Muted Trump
000-060 French Horn
000-061 Brass 1
000-062 Synth Brass1
000-063 Synth Brass2
000-064 Soprano Sax
000-065 Alto Sax
000-066 Tenor Sax
000-067 Baritone Sax
000-068 Oboe
000-069 English Horn
000-070 Bassoon
000-071 Clarinet
000-072 Piccolo
000-073 Flute
000-074 Recorder
000-075 Pan Flute
000-076 Blown Bottle
000-077 Shakuhachi
000-078 Whistle
000-079 Ocarina
000-080 Square Wave
000-081 Saw Wave
000-082 Syn Calliope
000-083 Chiffer Lead
000-084 Charang
000-085 Solo Vox
000-086 5th Saw Wave
000-087 Bass & Lead
000-088 Fantasia
000-089 Warm Pad
000-090 Polysynth
000-091 Space Voice
000-092 Bowed Glass
000-093 Metal Pad
000-094 Halo Pad
000-095 Sweep Pad
000-096 Ice Rain
000-097 Soundtrack
000-098 Crystal
000-099 Atmosphere
000-100 Brightness
000-101 Goblin
000-102 Echo Drops
000-103 Star Theme
000-104 Sitar
000-105 Banjo
000-106 Shamisen
000-107 Koto
000-108 Kalimba
000-109 Bag Pipe
000-110 Fiddle
000-111 Shanai
000-112 Tinker Bell
000-113 Agogo
000-114 Steel Drums
000-115 Wood Block
000-116 Taiko Drum
000-117 Melo Tom 1
000-118 Synth Drum
000-119 Reverse Cymb
000-120 Gt FretNoise
000-121 Breath No
000-122 SeaShore
000-123 Bird Tweet
000-124 Telephone
000-125 Helicopter
000-126 Applause
000-127 Gun Shot
128-000 Standard
 */