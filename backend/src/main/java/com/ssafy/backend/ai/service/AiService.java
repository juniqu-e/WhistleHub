package com.ssafy.backend.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.ai.sound.dto.AIGeneratedSoundData;
import com.ssafy.backend.ai.sound.dto.request.SoundGenerationRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LangChain4J를 활용하여 AI 모델과 통신하고, 음원 데이터를 생성하는 서비스를 구현
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
    private final ChatLanguageModel chatLanguageModel;
    private final ObjectMapper objectMapper;
    // AI 프롬프트 템플릿
    private static final String SYSTEM_PROMPT = """
            You are a professional music composer, sound designer, and MIDI programmer, with advanced knowledge in harmony theory and genre-specific musical composition.
            
            Your task is to generate a **musically coherent**, **harmonically valid**, and **genre-appropriate** MIDI composition specification based on the user's requirements. Your composition must reflect fundamental principles of Western music theory, such as proper chord progressions, melodic phrasing, rhythm, and voice leading.
            
             You MUST ensure that:
             - Chord progressions follow tonal harmony rules (e.g., diatonic progressions, cadences like V-I, II-V-I, etc.)
             - Melodies are built over the chords with appropriate scales (major, minor, modes, depending on the genre)
             - Each track has logical musical phrases (not random pitches)
             - The output reflects realistic music (as played by humans) in terms of rhythm and variation
            
             You must provide your response as a valid JSON object that strictly follows this structure:
            
             {
               "title": "Generated song title",
               "description": "Brief description of the music",
               "bpm": 120,
               "keySignature": "C Major",
               "tracks": [
                 {
                   "instrument": "Piano",
                   "midiChannel": 0,
                   "volume": 100,
                   "pan": 0,
                   "notes": [
                     {"pitch": 60, "velocity": 80, "startTick": 0, "duration": 240},
                     {"pitch": 64, "velocity": 80, "startTick": 240, "duration": 240}
                   ],
                   "controlChanges": [
                     {"type": 7, "value": 100, "tick": 0}
                   ]
                 }
               ],
               "sections": [
                 {
                   "name": "Intro",
                   "startTick": 0,
                   "endTick": 1920,
                   "repeatCount": 1
                 }
               ],
               "mixingSettings": {
                 "masterVolume": 100,
                 "reverb": 30,
                 "delay": 20,
                 "chorus": 10
               }
             }
            
             Guidelines:
             - Base the music on the user's specified **genre**, **mood**, **duration**, **tempo**, and **instruments**
             - Use **realistic harmonic and melodic rules** (e.g., no random notes, chord tones on strong beats)
             - Implement **sectional structure** (e.g., intro, verse, chorus, bridge) with musical continuity
             - Provide enough **note density** to match duration and tempo (assuming 480 ticks per quarter note)
             - Use **General MIDI program numbers** and channel rules properly
             - All rhythms and harmonies must sound natural and expressive for human listeners
            
             Respond only with a single valid JSON object that strictly follows the structure above.
            """;


    public AIGeneratedSoundData generateSoundData(SoundGenerationRequest request) {
        try {
            String userPrompt = createUserPrompt(request);

            // AI 모델에서 응답 얻기
            SystemMessage systemMessage = new SystemMessage(SYSTEM_PROMPT);
            UserMessage userMessage = new UserMessage(userPrompt);
            AiMessage aiMessage = chatLanguageModel.generate(systemMessage, userMessage).content();
            String response = aiMessage.toString();

            log.info("AI 응답: {}", response);

            // JSON 응답을 AIGeneratedSoundData 객체로 파싱
            return objectMapper.readValue(extractJsonFromResponse(response), AIGeneratedSoundData.class);
        } catch (Exception e) {
            log.error("AI로 음원 데이터 생성 실패", e);
            throw new RuntimeException("AI로 음원 데이터 생성 실패", e);
        }
    }

    private String createUserPrompt(SoundGenerationRequest request) {
        // 요청된 길이(초)에 해당하는 MIDI 틱 계산
        int pulsesPerQuarterNote = 480; // PPQ 값
        double beatsPerSecond = request.getTempo() / 60.0; // BPS
        int totalTicks = (int) (request.getDurationSeconds() * beatsPerSecond * pulsesPerQuarterNote);

        return String.format("""
                        You are a professional music composer and MIDI programmer with advanced knowledge in Western music theory, harmony, and rhythm. Your job is to generate a full-length musical composition that meets the exact specifications below and follows real-world compositional techniques.
                        
                         Please generate a musical composition with the following specifications:
                         - Genre: %s
                         - Mood: %s
                         - Duration: %d seconds (approximately %d MIDI ticks at %d BPM and PPQ %d)
                         - Tempo: %d BPM
                         - Instruments: %s
                         - Additional notes: %s
    
                         ### Timing & Format Rules:
                         - Each quarter note = %d ticks (PPQ = %d)
                         - At %d BPM, %d ticks = %d seconds
                         - Composition must be EXACTLY %d ticks long
                         - All notes must END at or before tick %d
                         - DO NOT exceed the maximum tick length
    
                         ### Musical Requirements:
                         - Use harmonically valid chord progressions and cadences for the genre (e.g., II-V-I in jazz, I-IV-V-I in pop)
                         - Melodies must be based on the key and scale implied by chords
                         - Rhythm patterns should reflect realistic musical phrasing for each instrument
                         - Include musical sections (Intro, Verse, Chorus, Bridge, etc.) with defined tick ranges
                         - Follow voice-leading and scale-conforming note choices
                         - Human-like dynamics: vary velocity and avoid robotic repetition
                         - Assign proper General MIDI instrument names and their program numbers (e.g., "Acoustic Grand Piano", 0)
    
                         ### Output Format:
                         Respond with a **single valid JSON object** using this structure:
                         {
                           "title": "...",
                           "description": "...",
                           "bpm": ...,
                           "keySignature": "...",
                           "tracks": [...],
                           "sections": [...],
                           "mixingSettings": {
                             "masterVolume": ...,
                             "reverb": ...,
                             "delay": ...,
                             "chorus": ...
                           }
                         }
    
                         You must ensure the composition is **musically coherent**, structurally complete, and playable as a real MIDI arrangement.
                         Respond only with a JSON object. Do not include explanations or markdown.
                        """,
                request.getGenre(),
                request.getMood(),
                request.getDurationSeconds(),
                totalTicks,
                request.getTempo(),
                pulsesPerQuarterNote,
                request.getTempo(),
                request.getInstruments(),
                request.getAdditionalNotes(),
                pulsesPerQuarterNote,
                pulsesPerQuarterNote,
                request.getTempo(),
                totalTicks,
                request.getDurationSeconds(),
                totalTicks,
                totalTicks
        );
    }

    private String extractJsonFromResponse(String response) {
        // 마크다운 코드 블록에서 JSON 추출
        if (response.contains("```json")) {
            response = response.substring(response.indexOf("```json") + 7);
            response = response.substring(0, response.indexOf("```"));
        } else if (response.contains("```")) {
            response = response.substring(response.indexOf("```") + 3);
            response = response.substring(0, response.indexOf("```"));
        }
        return response.trim();
    }

}
