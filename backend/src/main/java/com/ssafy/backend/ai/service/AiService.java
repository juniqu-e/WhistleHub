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

    // 개선된 AI 시스템 프롬프트
    private static final String SYSTEM_PROMPT = """
            You are a professional music composer, sound designer, and MIDI programmer with advanced knowledge in music theory and composition across multiple genres.
            
            TASK: Generate a musically coherent, harmonically valid, and genre-appropriate MIDI composition specification based on user requirements.
            
            MUSICAL REQUIREMENTS:
            - Use harmonically correct chord progressions based on Western music theory (e.g., V-I, II-V-I)
            - Construct melodies using appropriate scales relative to the underlying chords
            - Create logical musical phrases with proper tension and resolution
            - Apply genre-specific rhythm patterns, articulations, and dynamics
            - Maintain proper voice leading between chord transitions
            
            PERCUSSION INSTRUMENTATION:
            - For drum tracks, ALWAYS use MIDI channel 9 (zero-indexed)
            - Use standard General MIDI drum map for percussion sounds:
              * 35: Acoustic Bass Drum
              * 36: Bass Drum 1
              * 38: Acoustic Snare
              * 40: Electric Snare
              * 42: Closed Hi-Hat
              * 44: Pedal Hi-Hat
              * 46: Open Hi-Hat
              * 49: Crash Cymbal 1
              * 51: Ride Cymbal 1
            - When creating 16-bit rhythm patterns, use appropriate combinations of:
              * Bass drum (35/36) on beats 1 and 3 or with genre-appropriate variations
              * Snare (38/40) on beats 2 and 4 or with genre-appropriate variations
              * Hi-hats (42/46) for 16th note subdivisions
              * Crash cymbals (49) for section transitions and accents
              * Ride cymbals (51) for sustained rhythmic patterns
            
            OUTPUT FORMAT:
            Provide ONLY a valid JSON object with this structure:
            {
            "title": "Generated song title",
            "description": "Brief description of the music",
            "bpm": <tempo>,
            "keySignature": "<key>",
            "tracks": [
                {
                    "instrument": "<instrument name>",
                    "midiChannel": <0-15>,
                    "volume": <0-127>,
                    "pan": <-64 to 63>,
                    "notes": [
                        {"pitch": <0-127>, "velocity": <1-127>, "startTick": <tick>, "duration": <tick>}
                    ],
                    "controlChanges": [
                        {"type": <cc type>, "value": <0-127>, "tick": <tick>}
                    ]
                }
            ],
            "sections": [
                {
                    "name": "<section name>",
                    "startTick": <tick>,
                    "endTick": <tick>,
                    "repeatCount": <repeat>
                }
            ],
            "mixingSettings": {
                "masterVolume": <0-100>,
                "reverb": <0-100>,
                "delay": <0-100>,
                "chorus": <0-100>
            }
            }
            
            IMPORTANT GUIDELINES:
            - Base your composition on the user's specified genre, mood, duration, tempo, and instruments
            - Follow genre-specific harmonic patterns (e.g., jazz: II-V-I, pop: I-IV-V)
            - Structure the composition with appropriate sections (intro, verse, chorus, etc.)
            - Provide sufficient note density to fill the requested duration (using 480 ticks per quarter note)
            - When drums/percussion is requested, ALWAYS create a proper drum track on MIDI channel 9 with appropriate 16-bit rhythm patterns based on the provided DRUM PATTERN REQUIREMENTS and examples.
            - Use General MIDI program numbers correctly for instrument assignments
            - Ensure all musical elements sound natural and expressive for human listeners
            
            Respond ONLY with a valid JSON object. Do not include explanations, markdown formatting, or text outside the JSON structure.
            NEVER WRITE COMMENTS IN RESULT LIKE COMMENT BLOCK (/**/)
            NEVER WRITE COMMENTS IN RESULT LIKE COMMENT BLOCK (/**/)
            NEVER WRITE COMMENTS IN RESULT LIKE COMMENT BLOCK (/**/)
            NEVER WRITE COMMENTS IN RESULT LIKE COMMENT BLOCK (/**/)
            """;

    /**
     * 사용자 요청에 따라 AI 모델을 활용하여 음원 데이터 생성
     *
     * @param request 음원 생성 요청 정보
     * @return 생성된 AI 음원 데이터
     */
    public AIGeneratedSoundData generateSoundData(SoundGenerationRequest request) {
        try {
            String userPrompt = createUserPrompt(request);

            log.debug("사용자 프롬프트: {}", userPrompt);

            // AI 모델에서 응답 얻기
            SystemMessage systemMessage = new SystemMessage(SYSTEM_PROMPT);
            UserMessage userMessage = new UserMessage(userPrompt);
            AiMessage aiMessage = chatLanguageModel.generate(systemMessage, userMessage).content();
            // Langchain4j AiMessage에는 text() 메서드가 표준입니다. toString()은 객체 표현을 반환할 수 있습니다.
            String response = aiMessage.text();

            log.info("AI 응답 수신 완료: {} 자", response.length());
            log.info(response);
            log.debug("AI 응답 전문: {}", response);

            // JSON 응답 파싱 시도
            String jsonResponse = extractJsonFromResponse(response);
            log.debug("추출된 JSON: {}", jsonResponse);

            try {
                // JSON 응답을 AIGeneratedSoundData 객체로 파싱
                return objectMapper.readValue(jsonResponse, AIGeneratedSoundData.class);
            } catch (Exception e) {
                log.error("JSON 파싱 오류: {}", e.getMessage());
                // 실패 시 JSON 형식 정제 재시도
                jsonResponse = cleanupJson(jsonResponse);
                log.debug("정제된 JSON: {}", jsonResponse);
                return objectMapper.readValue(jsonResponse, AIGeneratedSoundData.class);
            }
        } catch (Exception e) {
            log.error("AI로 음원 데이터 생성 실패", e);
            throw new RuntimeException("AI로 음원 데이터 생성 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 사용자 요청을 바탕으로 AI 모델에 전달할 프롬프트 생성
     *
     * @param request 음원 생성 요청 정보
     * @return 생성된 사용자 프롬프트
     */
    private String createUserPrompt(SoundGenerationRequest request) {
        // 요청된 길이(초)에 맞는 MIDI 틱 계산
        int pulsesPerQuarterNote = 480;
        double beatsPerSecond = request.getTempo() / 60.0;
        int totalTicks = (int) (request.getDurationSeconds() * beatsPerSecond * pulsesPerQuarterNote);

        // 드럼 관련 요청에 대한 추가 지침
        String guidance = "";
        if (request.getInstruments().toLowerCase().contains("drum")) {
            // --- 드럼 패턴 보고서 내용 반영 시작 ---
            guidance = """
                    DRUM PATTERN REQUIREMENTS:
                    - The term "16-bit" in drumming refers to rhythmic subdivision based on sixteenth notes within a 4/4 measure, creating more detailed and rhythmically complex feels compared to 8-bit patterns.
                    - Generate genre-appropriate 16-bit drum patterns using the General MIDI (GM) standard drum map (pitches 35-81).
                    - The drum track MUST be placed on MIDI channel 9 (zero-indexed).
                    - Include fills and transitions between sections to provide variation and musical interest.
                    - Apply varied velocities (e.g., range 70-127) to mimic a human drummer's dynamics and make the pattern feel less robotic.
                    
                    16-BIT PATTERN DETAILS:
                    - Basic Pattern Structure: Often involves continuous 16th-note hi-hats, snare drum on beats 2 & 4 (backbeat), and kick drum on beats 1 & 3, with variations depending on the genre.
                    - Pattern Variation: Introduce variations using elements like floor toms, additional syncopated snares, open hi-hats, ride cymbal patterns, etc.
                    - Fills: Add 16th-note based fills periodically, typically leading into a new section (e.g., in the last measure of a 4 or 8-bar phrase).
                    - Accents: Utilize accents (higher velocity, e.g., 100-127) to emphasize specific notes, especially kick drums on strong beats (1 & 3) and snare drums on the backbeat (2 & 4), to create rhythmic drive and definition.
                    - Ghost Notes: Incorporate ghost notes (very low velocity, e.g., 10-40) especially on the snare drum between main beats to add subtle rhythmic complexity and enhance the groove.
                    - Rhythmic Structure: Build patterns in logical loops (e.g., 2 or 4 bars), often introducing slight variations in the even-numbered bars (e.g., bar 2 or 4) for musicality.
                    - Hi-Hat Patterns: Vary hi-hat patterns beyond constant 16ths. Use open hi-hats (pitch 46) for accents (often on off-beats), pedal hi-hats (pitch 44), or vary the velocity to simulate down/up strokes to enhance the groove. Consider using two hands alternating for faster tempos if applicable in programming context.
                    - Velocity Dynamics: Implement natural-sounding dynamics with varying velocities for different drum components. Suggested ranges (adjust for genre/mood):
                      * Kick: 100-127 (accented beats), 80-99 (regular beats)
                      * Snare: 100-127 (backbeat accents), 90-109 (regular hits), 10-40 (ghost notes)
                      * Hi-hat: 90-110 (main beats/accents), 70-89 (subdivisions/off-beats), 40-69 (softer subdivisions)
                      * Cymbals (Crash/Ride): 100-127 (accents), 80-99 (standard ride pattern)
                    
                    GENRE-SPECIFIC DRUM CHARACTERISTICS (Apply these when relevant):
                      * Chillhop/Lo-fi Hip Hop: Often features a loose, slightly behind-the-beat 16th-note feel, sometimes incorporating triplet rhythms or swing. Moderate use of ghost notes.
                      * Hip-Hop/Trap: Can feature very fast hi-hat rolls (use multiple consecutive 16th or 32nd notes), heavily syncopated kick drum patterns often avoiding strong downbeats to create 'bounce', possibly with a swing feel. Strong, prominent backbeat snare.
                      * R&B: Smooth and often intricate grooves. Steady 16th hi-hats are common, but with dynamic variation. Kicks are carefully placed to support the bassline. Prominent use of snare ghost notes is characteristic.
                      * Rock: Often uses straight and driving 16th-note hi-hat patterns (or 8th notes for simpler rock). Powerful backbeat on snare. Kick drum often plays on downbeats and syncopated 8th/16th-note subdivisions for drive. Less emphasis on ghost notes than R&B/Funk.
                      * Funk: Emphasizes syncopation heavily. Snare hits frequently occur on off-beats ('e', 'a') over steady 16th hi-hats. Creates complex rhythmic interplay between kick, snare, and hi-hat. Ghost notes are common.
                      * Disco: Typically features a "four-on-the-floor" kick pattern (kick drum on every beat: 1, 2, 3, 4) combined with continuous 16th-note hi-hats. Open hi-hats often used on the off-beats ('+') for characteristic sizzle. Steady snare backbeat on 2 & 4.
                      * Pop Ballad (Slow Tempo): May use wider-spaced 16th-note hi-hats (e.g., only on the '1' and '+' of each beat, or just 8th notes) for a less busy feel. Clear, defined snare on 2 & 4. Kick drum primarily on downbeats or simple patterns to provide foundation without clutter.
                    
                    DRUM SOUND PLACEMENT (Standard GM Drum Map - use these MIDI pitch numbers):
                    - Kick Drum: 35 (Acoustic Bass Drum), 36 (Bass Drum 1) - Choose based on desired sound.
                    - Snare Drum: 38 (Acoustic Snare - main backbeat), 40 (Electric Snare - alternative sound), 37 (Side Stick/Rimshot - for variation/accents).
                    - Hi-Hat: 42 (Closed Hi-Hat - standard), 46 (Open Hi-Hat - for accents/sizzle), 44 (Pedal Hi-Hat - short closing sound).
                    - Toms: 41 (Low Floor Tom), 43 (High Floor Tom), 45 (Low Tom), 47 (Low-Mid Tom), 48 (Hi-Mid Tom), 50 (High Tom) - Use for fills.
                    - Cymbals: 49 (Crash Cymbal 1), 57 (Crash Cymbal 2) - Use for accents, section changes. 51 (Ride Cymbal 1), 59 (Ride Cymbal 2) - Use for alternative sustained patterns instead of hi-hat. 55 (Splash Cymbal) - Short accent. 53 (Ride Bell) - Accent on ride patterns.
                    
                    TEXT-BASED NOTATION EXAMPLES FOR REFERENCE (HH=HiHat, SD=Snare, BD=Kick, RD=Ride, OHH=OpenHH, CC=Crash; X=Hit, -=Rest; Each group is one beat with four 16th note positions: 1e+a 2e+a 3e+a 4e+a):
                    
                    1. Modern Pop Drive (Example based on description):
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: ----X---|----X---|----X---|----X---|  (Corrected: Snare on 2 & 4)
                       BD: X-------|X---X---|X-------|X-------|  (Example variation)
                       Corrected Example Pattern:
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: ----X---|----X---|----X---|----X---|
                       BD: X---X---|----X---|X---X---|----X---|
                    
                    2. HipHop/Trap HiHat Roll Example:
                       HH: XXXXXXXXXXXXXXXX|XXXXXXXXXXXXXXXX| (Rapid 16ths or 32nds)
                       SD: ----X---|----X---|----X---|----X---|
                       BD: X-----X-|--------|X-----X-|--------| (Syncopated Kick)
                    
                    3. R&B Smooth Groove Example (with ghost note 'g'):
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: ----X--g|----X---|----X--g|----X--g|
                       BD: X---X---|--X-----|X---X---|--X-----|
                    
                    4. Funk 16-Bit Emphasis Example:
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: --X--X-X|---X-X--|--X--X-X|---X-X--| (Syncopated Snare)
                       BD: X----X--|X-------|X----X--|X-------|
                    
                    5. Rock Power Drive (Straight 16ths) Example:
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: ----X---|----X---|----X---|----X---|
                       BD: X-X---X-|X-X-----|X-X---X-|X-X-----|
                    
                    6. Disco Four-on-the-Floor (16th HiHat w/ Open HH) Example:
                       HH: X-XoX-Xo|X-XoX-Xo|X-XoX-Xo|X-XoX-Xo| (o = Open HiHat on '+')
                       SD: ----X---|----X---|----X---|----X---|
                       BD: X-------|X-------|X-------|X-------| (Kick on every beat)
                    
                    7. Half-Time Shuffle Feel Example (conceptual notation):
                       HH: X.x.X.x.|X.x.X.x.|X.x.X.x.|X.x.X.x.| (Swung/shuffled 16ths)
                       SD: --------|----X---|--------|----X---| (Snare on 3)
                       BD: X-------|----X---|X-------|--------|
                    
                    8. Modern HipHop Bounce (Syncopated Kick) Example:
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: ----X---|----X---|----X---|----X---|
                       BD: X----X--|-X--X---|X----X--|-X------|
                    
                    9. Funk/Fusion Complexity Example:
                       HH: X-X-X-X-|X-X-X-X-|X-X-X-X-|X-X-X-X-|
                       SD: -X--X-X-|-X--X---|-X--X-X-|--X-----| (Highly syncopated snare)
                       BD: X---X---|-X-X----|X------X|--X-X---| (Complex kick pattern)
                    
                    10. Pop Ballad Slow 16-Bit Example (Often simplified):
                       HH: X---X---|X---X---|X---X---|X---X---| (Spaced hi-hats, or just 8ths: X-------|X-------|...)
                       SD: ----X---|----X---|----X---|----X---| (Clear snare on 2 & 4)
                       BD: X-------|--------|X-------|--------| (Simple kick pattern)
                    - Example1 :
                       {
                      "title": "Groovy Hip-Hop Pulse",
                      "description": "An 8-second, 120 BPM hip-hop drum composition featuring detailed 16-bit grooves with dynamic hi-hats, syncopated kicks, snare backbeats with ghost notes, and an exciting fill in the final measure.",
                      "bpm": 120,
                      "keySignature": "C",
                      "tracks": [
                        {
                          "instrument": "Drum Kit",
                          "midiChannel": 9,
                          "volume": 127,
                          "pan": 0,
                          "notes": [
                            {"pitch": 36, "velocity": 120, "startTick": 0, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 0, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 120, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 240, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 360, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 480, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 480, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 600, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 720, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 840, "duration": 100},
                            {"pitch": 36, "velocity": 120, "startTick": 960, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 960, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 1080, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 1200, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 1320, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 1440, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 1440, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 1560, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 1680, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 1800, "duration": 100},
                            {"pitch": 36, "velocity": 120, "startTick": 1920, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 1920, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 2040, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 2160, "duration": 100},
                            {"pitch": 38, "velocity": 30, "startTick": 2280, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 2280, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 2400, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 2400, "duration": 100},
                            {"pitch": 36, "velocity": 100, "startTick": 2520, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 2520, "duration": 100},
                            {"pitch": 36, "velocity": 120, "startTick": 2880, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 2880, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3000, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3120, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3240, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 3360, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 3360, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3480, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3600, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3720, "duration": 100},
                            {"pitch": 36, "velocity": 120, "startTick": 3840, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 3840, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 3960, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 4080, "duration": 100},
                            {"pitch": 46, "velocity": 110, "startTick": 4200, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 4320, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 4320, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 4440, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 4560, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 4680, "duration": 100},
                            {"pitch": 38, "velocity": 30, "startTick": 4680, "duration": 100},
                            {"pitch": 36, "velocity": 120, "startTick": 4800, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 4800, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 4920, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 5040, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 5160, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 5280, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 5280, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 5400, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 5520, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 5640, "duration": 100},
                            {"pitch": 36, "velocity": 120, "startTick": 5760, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 5760, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 5880, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 6000, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 6120, "duration": 100},
                            {"pitch": 38, "velocity": 110, "startTick": 6240, "duration": 100},
                            {"pitch": 42, "velocity": 100, "startTick": 6240, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 6360, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 6480, "duration": 100},
                            {"pitch": 42, "velocity": 80, "startTick": 6600, "duration": 100},
                            {"pitch": 45, "velocity": 100, "startTick": 6720, "duration": 100},
                            {"pitch": 47, "velocity": 100, "startTick": 6840, "duration": 100},
                            {"pitch": 50, "velocity": 100, "startTick": 6960, "duration": 100},
                            {"pitch": 48, "velocity": 100, "startTick": 7080, "duration": 100},
                            {"pitch": 49, "velocity": 127, "startTick": 7200, "duration": 100},
                            {"pitch": 45, "velocity": 100, "startTick": 7320, "duration": 100},
                            {"pitch": 47, "velocity": 100, "startTick": 7440, "duration": 100},
                            {"pitch": 50, "velocity": 100, "startTick": 7560, "duration": 100}
                          ],
                          "controlChanges": []
                        }
                      ],
                      "sections": [
                        {
                          "name": "Intro",
                          "startTick": 0,
                          "endTick": 1920,
                          "repeatCount": 1
                        },
                        {
                          "name": "Verse",
                          "startTick": 1920,
                          "endTick": 5760,
                          "repeatCount": 1
                        },
                        {
                          "name": "Outro",
                          "startTick": 5760,
                          "endTick": 7680,
                          "repeatCount": 1
                        }
                      ],
                      "mixingSettings": {
                        "masterVolume": 90,
                        "reverb": 30,
                        "delay": 20,
                        "chorus": 10
                      }
                    }
                    - EXAMPLE2: {
                      "title": "Groovy Hip-Hop Pulse",
                      "description": "An 8-second, 120 BPM hip-hop drum composition featuring detailed 16-bit grooves with dynamic hi-hats, syncopated kicks, strong snare backbeats with ghost notes, and an exciting tom fill in the final measure.",
                      "bpm": 120,
                      "keySignature": "C",
                      "tracks": [
                        {
                          "instrument": "Drum Kit",
                          "midiChannel": 9,
                          "volume": 127,
                          "pan": 0,
                          "notes": [
                            { "pitch": 42, "velocity": 105, "startTick": 0, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 120, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 240, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 360, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 480, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 600, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 720, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 840, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 960, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 1080, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 1200, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 1320, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 1440, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 1560, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 1680, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 1800, "duration": 100 },
                            { "pitch": 36, "velocity": 120, "startTick": 0, "duration": 100 },
                            { "pitch": 36, "velocity": 110, "startTick": 960, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 480, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 1440, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 360, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 600, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 1920, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 2040, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 2160, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 2280, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 2400, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 2520, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 2640, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 2760, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 2880, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 3000, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 3120, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 3240, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 3360, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 3480, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 3600, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 3720, "duration": 100 },
                            { "pitch": 36, "velocity": 120, "startTick": 1920, "duration": 100 },
                            { "pitch": 36, "velocity": 90, "startTick": 2520, "duration": 100 },
                            { "pitch": 36, "velocity": 110, "startTick": 2880, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 2400, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 3360, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 2280, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 2520, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 3840, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 3960, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 4080, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 4200, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 4320, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 4440, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 4560, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 4680, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 4800, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 4920, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 5040, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 5160, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 5280, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 5400, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 5520, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 5640, "duration": 100 },
                            { "pitch": 36, "velocity": 120, "startTick": 3840, "duration": 100 },
                            { "pitch": 36, "velocity": 90, "startTick": 4200, "duration": 100 },
                            { "pitch": 36, "velocity": 110, "startTick": 4800, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 4320, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 5280, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 3960, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 4440, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 5760, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 5880, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 6000, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 6120, "duration": 100 },
                            { "pitch": 42, "velocity": 105, "startTick": 6240, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 6360, "duration": 100 },
                            { "pitch": 42, "velocity": 95, "startTick": 6480, "duration": 100 },
                            { "pitch": 42, "velocity": 80, "startTick": 6600, "duration": 100 },
                            { "pitch": 36, "velocity": 120, "startTick": 5760, "duration": 100 },
                            { "pitch": 38, "velocity": 110, "startTick": 6240, "duration": 100 },
                            { "pitch": 38, "velocity": 35, "startTick": 6120, "duration": 100 },
                            { "pitch": 45, "velocity": 110, "startTick": 6720, "duration": 100 },
                            { "pitch": 47, "velocity": 110, "startTick": 6840, "duration": 100 },
                            { "pitch": 48, "velocity": 110, "startTick": 6960, "duration": 100 },
                            { "pitch": 50, "velocity": 110, "startTick": 7080, "duration": 100 },
                            { "pitch": 47, "velocity": 110, "startTick": 7200, "duration": 100 },
                            { "pitch": 45, "velocity": 110, "startTick": 7320, "duration": 100 },
                            { "pitch": 48, "velocity": 110, "startTick": 7440, "duration": 100 },
                            { "pitch": 50, "velocity": 110, "startTick": 7560, "duration": 100 },
                            { "pitch": 49, "velocity": 127, "startTick": 7680, "duration": 100 }
                          ],
                          "controlChanges": []
                        }
                      ],
                      "sections": [
                        {
                          "name": "Intro",
                          "startTick": 0,
                          "endTick": 1920,
                          "repeatCount": 1
                        },
                        {
                          "name": "Verse",
                          "startTick": 1920,
                          "endTick": 5760,
                          "repeatCount": 1
                        },
                        {
                          "name": "Outro",
                          "startTick": 5760,
                          "endTick": 7680,
                          "repeatCount": 1
                        }
                      ],
                      "mixingSettings": {
                        "masterVolume": 90,
                        "reverb": 30,
                        "delay": 20,
                        "chorus": 10
                      }
                    }
                    """;
            // --- 드럼 패턴 보고서 내용 반영 끝 ---
        } else if ((request.getInstruments().toLowerCase().contains("piano"))) {
            guidance = """
                    ###  **PIANO PATTERN REQUIREMENTS:**
                    
                    - The term **“16-bit” in piano** composition refers to rhythmic subdivision emphasizing sixteenth-note articulation, voicing detail, and expressive timing within a 4/4 measure.
                    - Generate **genre-appropriate 16th-note piano rhythm patterns** using **MIDI note numbers (21–108)**.
                    - The piano track **MUST be placed on MIDI channel 0** (default channel for piano).
                    - **Include expressive techniques** such as grace notes, arpeggios, pedal marks, and hand movement for realism.
                    - Apply **varied velocities (range 30–127)** to mimic human dynamics, emotion, and articulation for expressive realism.
                    
                    ---
                    
                    ###  **16-BIT PIANO PATTERN DETAILS:**
                    
                    - **Pattern Structure**: Utilize 16th-note subdivisions for right-hand rhythmic motifs or arpeggios, while left-hand provides harmonic foundation (chords, ostinatos, walking bass, etc.)
                    - **Voicing Dynamics**: Emphasize melody or rhythmic motif with higher velocity (90–127), while support notes (inner voice, left hand) stay moderate (50–89).
                    - **Pedaling**: Use sustain pedal (CC 64) where appropriate for legato phrasing or harmonic blending. Avoid overuse to prevent blurring of fast 16ths.
                    - **Hand Independence**: Maintain rhythmic independence between hands, with left-hand often slower (quarter/8th notes), and right-hand in 16ths.
                    - **Variation**: Introduce rhythmic, harmonic, and registral variation every 2 or 4 bars. Use voice leading techniques or register changes to provide interest.
                    - **Ornamentation**: Include grace notes (acciaccatura), trills, turns, or mordents in the right hand for expressive color, especially in classical/jazz styles.
                    - **Fills & Transitions**: At the end of phrases (4 or 8 bars), add transitional phrases like fast scales, chord flutters, chromatic runs, or dramatic dynamic swells.
                    
                    ---
                    
                    ###  **GENRE-SPECIFIC PIANO CHARACTERISTICS (Apply accordingly):**
                    
                    - **Lo-fi/Chillhop Piano**: Relaxed tempo, often swung 16ths. Use jazzy chords (7ths, 9ths), mellow voicing, lots of reverb. Ghosted inner notes (low velocity) and slightly “off-grid” timing.
                    - **Neo-Soul/R&B Piano**: Complex chord extensions, syncopated right-hand rhythms. Ghosted grace notes, heavy use of rhythmic motif repetition and lush voicing.
                    - **Pop Piano**: Clearly structured patterns. RH often plays rhythmic chord stabs or arpeggios in 16ths. LH provides stable root motion. Clear melodic hooks.
                    - **Rock Piano**: Straightforward rhythms, often power chords or rhythmic octaves. Driving 16ths in RH for intensity, strong backbeat implied.
                    - **Funk Piano**: Clavinet-style stabs or syncopated 16th note funk rhythms. Use percussive articulation, short note lengths. Interplay between hands.
                    - **Jazz Piano**: Swing 16ths or triplets, complex voicings, altered tensions. Use walking bass LH, RH comping or melodic runs. Include bluesy licks, reharmonization.
                    - **Ballad Piano**: Arpeggiated chords, slow 16ths or 8ths in RH, lush sustained harmonies with pedal. Expressive melody with rubato.
                    
                    ---
                    
                    ###  **PIANO SOUND PLACEMENT (MIDI Note Numbers):**
                    
                    - **Low Register (Bass)**: 21 (A0) to 47 (B2) – Left hand patterns, bass movement
                    - **Middle Register (Harmony)**: 48 (C3) to 72 (C5) – Primary chords, voicings
                    - **High Register (Melody)**: 73 (C#5) to 108 (C8) – Right hand melody, embellishments
                    
                    ---
                    
                    ###  **TEXT-BASED NOTATION EXAMPLES (16ths in 4/4 - One measure = 1e+a 2e+a 3e+a 4e+a):**
                    
                    1. **Pop Arpeggio Pattern**:
                       RH: C-E-G-E|C-E-G-E|C-E-G-E|C-E-G-E
                       LH: C---C---|C---C---|C---C---|C---C---
                    
                    2. **R&B Syncopation Example (g = ghost note)**:
                       RH: ----Eb--|g--F---G|---g-A--|----C---
                       LH: F---g---|D---G---|F---g---|D---G---
                    
                    3. **Funk Stab Rhythms**:
                       RH: C--x--x-|---x-x--|x--x--x-|---x-x--
                       LH: Bb------|---C----|---D---|--Bb----
                    
                    4. **Lo-fi Swinged Feel**:
                       RH: X---x---|X---x---|X---x---|X---x--- (triplet feel implied)
                       LH: ----C---|----A---|----D---|----B---
                    
                    5. **Jazz Walking Bass with Comping**:
                       RH: -x--x---|--x---x-|x-----x-|--x---x-
                       LH: C-D-E-F|G-A-B-C|D-E-F-G|G-A-Bb-C
                    """;
        } else if ((request.getInstruments().toLowerCase().contains("flute"))) {
            guidance = """
                    ### **FLUTE PATTERN REQUIREMENTS:**
                    
                    - The term **“16-bit” in flute** composition refers to rhythmic subdivision emphasizing sixteenth-note articulation, melodic phrasing, and expressive timing within a 4/4 measure.
                    - Generate **genre-appropriate 16th-note flute rhythm patterns** using **MIDI note numbers (60–96)**.
                    - The flute track **MUST be placed on MIDI channel 1** (common default channel for woodwinds like flute).
                    - **Include expressive techniques** such as trills, slurs, staccato articulation, and breath marks for realism.
                    - Apply **varied velocities (range 30–127)** to mimic human dynamics, emotion, and articulation for expressive realism.
                    
                    ---
                    
                    ### **16-BIT FLUTE PATTERN DETAILS:**
                    
                    - **Pattern Structure**: Utilize 16th-note subdivisions for melodic runs, arpeggios, or rhythmic motifs. Avoid chordal harmony (flute is monophonic) and focus on single-line phrasing.
                    - **Voicing Dynamics**: Emphasize melodic peaks with higher velocity (90–127), while subtler passing tones or ornaments use moderate velocity (50–89).
                    - **Breath Control**: Indicate breath marks (') where appropriate for natural phrasing. Avoid overly long 16th-note runs without breaks to reflect realistic flute breathing.
                    - **Hand Independence**: Not applicable; focus instead on melodic contour and rhythmic variation within a single line.
                    - **Variation**: Introduce rhythmic, melodic, and registral variation every 2 or 4 bars. Use scalar passages, leaps, or register shifts for interest.
                    - **Ornamentation**: Include trills, grace notes (acciaccatura), turns, or mordents for expressive color, especially in classical/jazz styles.
                    - **Fills & Transitions**: At the end of phrases (4 or 8 bars), add transitional phrases like fast scales, chromatic runs, flutter-tonguing, or dynamic swells.
                    
                    ---
                    
                    ### **GENRE-SPECIFIC FLUTE CHARACTERISTICS (Apply accordingly):**
                    
                    - **Lo-fi/Chillhop Flute**: Relaxed tempo, slightly swung 16ths. Use mellow, breathy tones, jazzy pentatonic or modal scales, and subtle reverb. Add ghosted notes (low velocity) with off-grid timing.
                    - **Neo-Soul/R&B Flute**: Syncopated 16th-note rhythms, soulful melodic motifs. Use slurred grace notes, repeated phrases, and expressive bends or slides.
                    - **Pop Flute**: Clear, structured patterns with catchy 16th-note melodic hooks. Emphasize rhythmic articulation (staccato or legato) and bright tone.
                    - **Rock Flute**: Driving, straightforward 16ths for intensity. Use sharp articulation, octave leaps, and a bold, piercing tone (e.g., Jethro Tull style).
                    - **Funk Flute**: Syncopated 16th-note funk rhythms with percussive staccato. Include rhythmic interplay with implied grooves and short, punchy phrases.
                    - **Jazz Flute**: Swing 16ths or triplets, bluesy licks, and altered scales. Use walking melodic lines, chromatic runs, and improvisatory flair.
                    - **Ballad Flute**: Lyrical, flowing 16th-note runs or arpeggios, slow and expressive. Emphasize legato phrasing, subtle vibrato, and rubato timing.
                    
                    ---
                    
                    ### **FLUTE SOUND PLACEMENT (MIDI Note Numbers):**
                    
                    - **Low Register**: 60 (C4) to 71 (B4) – Warm, foundational melodic lines.
                    - **Middle Register**: 72 (C5) to 84 (C6) – Primary melodic range, expressive and versatile.
                    - **High Register**: 85 (C#6) to 96 (C7) – Bright, piercing embellishments or climactic phrases.
                    
                    *(Note: Flute’s practical range is roughly C4 to C7; higher notes are possible but less common.)*
                    
                    ---
                    
                    ### **TEXT-BASED NOTATION EXAMPLES (16ths in 4/4 - One measure = 1e+a 2e+a 3e+a 4e+a):**
                    
                    1. **Pop Melodic Run**: 
                       Flute: C-D-E-G|E-D-C-E|G-E-D-C|C-E-G-E 
                       *(Bright, rhythmic, and hook-driven)*
                    
                    2. **R&B Syncopation Example (g = ghost note)**: 
                       Flute: ----Eb--|g--F---G|---g-A--|----C--- 
                       *(Slurred, soulful phrasing with ghosted notes)*
                    
                    3. **Funk Stab Rhythms**:
                       Flute: C--x--x-|---x-x--|x--x--x-|---x-x--
                       *(Staccato, percussive, and groove-oriented)*
                    
                    4. **Lo-fi Swinged Feel**:
                       Flute: X---x---|X---x---|X---x---|X---x---
                       *(Triplet feel implied, mellow and breathy)*
                    
                    5. **Jazz Melodic Run**:
                       Flute: C-D-E-F|G-Ab-Bb-C|D-Eb-F-G|Ab-Bb-C-D
                       *(Swinging 16ths with chromatic flair)*
                    """;
        }else if ((request.getInstruments().toLowerCase().contains("guitar"))) {
            guidance = """
                    ###  **GUITAR PATTERN REQUIREMENTS:**
                    
                    - The term **“16-bit” in guitar** refers to strumming or picking patterns that subdivide each beat into four 16th-note segments, often resulting in groove-heavy, rhythmically complex parts.
                    - Generate **genre-appropriate 16th-note rhythm guitar patterns** using **Standard MIDI Guitar Channel (Channel 0)** or **Realistic TAB notation** with fretboard positions.
                    - Include **fills, percussive elements, chord embellishments**, and **transitions** to mimic expressive, human guitar playing.
                    - Use **velocity variations (range: 30–127)** for dynamics and articulation — simulate strumming direction, accents, palm-mutes, and ghost strums.
                    
                    ---
                    
                    ###  **16-BIT GUITAR PATTERN DETAILS:**
                    
                    - **Rhythmic Structure**: Use 16th-note strumming or picking patterns per 4/4 bar. Combine downstrokes (D) and upstrokes (U), palm mutes (P.M.), mutes (X), and accents.
                    - **Voicing**: Use chord inversions, extensions (7, 9, sus), or power chords depending on genre. Move voicings up/down the neck across phrases.
                    - **Strumming Notation**:
                      - D = Downstroke
                      - U = Upstroke
                      - X = Muted strum (percussive)
                      - P.M. = Palm Mute
                      - g = Ghost stroke (very soft or implied)
                    - **Fingerstyle or Picking**: Use individual note picking patterns (Travis picking, arpeggios, hybrid picking) especially in ballads or acoustic contexts.
                    - **Fill-ins**: End of 4/8-bar phrases should include hammer-ons, pull-offs, double stops, or melodic fills in higher frets.
                    - **Groove Realism**: Slight rhythmic offsets, swing feel, dynamic accenting help mimic real human guitarists.
                    
                    ---
                    
                    ###  **GENRE-SPECIFIC GUITAR CHARACTERISTICS (Apply as needed):**
                    
                    - **Funk**: Tight 16th-note muted strums (chika-chika), syncopation, ghost notes, and percussive X strokes dominate.
                    - **Pop/Rock**: Open chords or power chords with driving 8th/16th strumming. Choruses may have broader strums, verses more restrained.
                    - **Neo-Soul/R&B**: Smooth voicings, broken chords with embellishments (hammer-ons, slides). Often rhythmically sparse but intricate.
                    - **Hip-Hop/Lo-fi**: Jazzy chords, mellow picking, muted phrases. Groove-based — more about rhythm than harmonic movement.
                    - **Reggae**: Emphasis on off-beats ("and" counts). Short, syncopated upstroke stabs.
                    - **Metal**: Palm-muted 16ths, alternate picking, tremolo riffs. Use low-register power chords, harmonics, and chugs.
                    - **Ballads**: Arpeggiated picking, suspended chords, reverb-heavy tone. Slow 16ths or mixed with 8ths for emotive feel.
                    - **Latin/Fusion**: Use syncopation, hybrid picking, and rhythmic complexity. Chord comping often matches percussion.
                    
                    ---
                    
                    ###  **GUITAR PATTERN NOTATION EXAMPLES (Strumming: D=Down, U=Up, X=Mute, g=Ghost):**
                    
                    1. **Pop-Rock 16th Strumming** (4/4, 1e+a...):
                       - Pattern: D - D U - U D U | D - D U - U D U
                       - Accent:   >     >         >     > \s
                       - Chords: C     G     Am    F \s
                       - Tip: Use slight velocity increase on accented downs.
                    
                    2. **Funk Groove**:
                       - Pattern: X X D U | X X D U | X X D U | X X D U \s
                       - Notes: All muted except strong syncopated D on beat 2e and 4e.
                       - Chords: 9th chords (e.g. E9, A9)
                    
                    3. **Neo-Soul Riff Example**:
                       - Picked pattern: p-i-m-a | a-m-i-p (fingers)
                       - Chord: Cmaj7 shape with hammer-ons to 9ths.
                       - Techniques: Slide into chord, ghost notes in between.
                    
                    4. **Metal Gallop Picking**:
                       - Pattern: D D DU | D D DU | D D DU | D D DU
                       - Chords: Low E5 power chord (MIDI pitch ~40–43)
                       - P.M.: Apply palm-mute to all but final note in each burst.
                    
                    5. **Reggae Upstroke Feel**:
                       - Pattern: - - U - | - - U - | - - U - | - - U - \s
                       - Chords: Barre chords (e.g. Bb, Eb), short duration.
                       - Technique: Sharp, bright tone, cut off after each upstroke.
                    
                    6. **Ballad Fingerstyle (Arpeggiated)**:
                       - RH: p-i-m-a | p-i-m-a | p-i-m-a | p-i-m-a
                       - LH: Cmaj | G/B | Am | F
                       - Add reverb, expressive dynamics, and rubato at ends.
                    
                    ---
                    
                    ###  **VELOCITY GUIDELINES (Suggested Ranges):**
                    
                    - **Downstroke (Accent)**: 90–120
                    - **Upstroke**: 70–100
                    - **Palm-Mute**: 60–80 (dull tone)
                    - **Ghost/Mute Strums (X, g)**: 20–50
                    - **Melodic Fills**: 90–127 (expressive)
                    - **Sustain Chords**: 80–110
                    """;
        }

        return String.format("""
                        MUSIC COMPOSITION REQUEST
                        
                        COMPOSITION SPECIFICATIONS:
                        - Genre: %s
                        - Mood: %s
                        - Duration: %f seconds
                        - Tempo: %d BPM
                        - Instruments: %s
                        - Additional requirements: %s
                        
                        TECHNICAL PARAMETERS:
                        - PPQ (ticks per quarter note): %d
                        - Total composition length: %d ticks
                        
                        %s
                        
                        GENRE-SPECIFIC GUIDANCE (Apply these generally, and use DRUM details above if drums requested):
                        %s
                        
                        COMPOSITION STRUCTURE:
                        - Include appropriate musical sections (e.g., intro, verse, chorus, bridge, outro) suitable for a %s piece in a %s mood.
                        - Ensure logical development of musical ideas throughout the piece.
                        - All requested instruments should have musically meaningful parts.
                        - Use appropriate harmonic progressions and cadences for the specified genre.
                        - Apply a 16-bit rhythmic feel or structure where appropriate for the genre (especially for drums, bass, and rhythmic accompaniment), ensuring proper quantization unless a specific 'human' or 'swing' feel is requested.
                        
                        Please generate a complete MIDI composition specification that follows these instructions precisely.
                        Respond ONLY with the valid JSON object as specified in the system prompt. Do not include any text, explanations, or markdown formatting outside the JSON structure itself.
                        """,
                request.getGenre(),
                request.getMood(),
                request.getDurationSeconds(),
                request.getTempo(),
                request.getInstruments(),
                request.getAdditionalNotes(),
                pulsesPerQuarterNote,
                totalTicks,
                guidance, // Include the detailed drum guidance here
                getGenreSpecificGuidance(request.getGenre()), // General genre guidance
                request.getGenre(),
                request.getMood());
    }

    /**
     * 장르별 특화된 음악 작곡 지침 제공 (드럼 제외 일반 가이드라인)
     *
     * @param genre 음악 장르
     * @return 장르별 작곡 지침
     */
    private String getGenreSpecificGuidance(String genre) {
        // This provides general musical guidance. Drum-specific guidance is in drumGuidance.
        return switch (genre.toLowerCase()) {
            case "classical" ->
                    "Use proper counterpoint, classical forms (e.g., sonata, theme and variations). Employ expressive dynamics and articulations suitable for orchestral or chamber instruments.";
            case "jazz" ->
                    "Incorporate jazz harmony (7ths, 9ths, altered chords). Use common progressions like II-V-I. Feature improvisation sections if appropriate. Apply swing rhythm unless specified otherwise.";
            case "rock" ->
                    "Focus on a strong rhythm section (guitar, bass, drums). Use power chords, riffs, and typical rock progressions. Structure often includes verse-chorus form.";
            case "electronic", "edm", "techno", "house" ->
                    "Build evolving rhythmic and melodic patterns. Use synth sounds, effects (reverb, delay), and automation. Focus on builds, drops, and rhythmic energy. Drum patterns are crucial (see DRUM section).";
            case "pop" ->
                    "Use contemporary chord progressions (e.g., I-V-vi-IV). Focus on catchy melodies and clear verse-chorus structure. Production should sound modern and polished.";
            case "hip-hop", "trap", "chillhop", "lo-fi" ->
                    "Focus on strong beats and rhythmic groove. Often built around loops or samples (conceptually). Leave space for potential vocals. Basslines are important. Drum patterns define the subgenre (see DRUM section).";
            case "r&b", "soul" ->
                    "Smooth chord progressions, often using 7th and 9th chords. Focus on melodic vocals (implied). Rhythm section provides a solid but nuanced groove. See DRUM section for rhythmic details.";
            case "funk" ->
                    "Emphasis on rhythmic groove and syncopation, especially in bass and drums. Often uses static harmony (one or two chords) for extended periods. Horn stabs are common.";
            case "disco" ->
                    "Danceable, consistent tempo. Four-on-the-floor drums, active basslines (often octaves), string and horn arrangements. Verse-chorus structure common.";
            default ->
                    "Apply standard Western harmony and develop rhythmic patterns appropriate for the specified genre and mood.";
        };
    }


    /**
     * AI 응답에서 JSON 데이터 추출
     *
     * @param response AI 응답 텍스트
     * @return 추출된 JSON 문자열
     */
    private String extractJsonFromResponse(String response) {
        // AI 응답에서 JSON 코드 블록을 찾거나, 전체가 JSON이라고 가정
        String json = response.trim();

        // 마크다운 코드 블록 ```json ... ``` 또는 ``` ... ``` 에서 JSON 추출 시도
        if (json.startsWith("```")) {
            int firstBlockStart = json.indexOf("```") + 3;
            // Optional language specifier (like "json")
            if (json.substring(firstBlockStart).trim().startsWith("json")) {
                firstBlockStart = json.indexOf('\n', firstBlockStart) + 1; // Skip 'json' line
            } else if (json.charAt(firstBlockStart) == '\n') {
                firstBlockStart += 1; // Skip newline after ```
            }
            int firstBlockEnd = json.indexOf("```", firstBlockStart);
            if (firstBlockEnd != -1) {
                json = json.substring(firstBlockStart, firstBlockEnd).trim();
            } else {
                // If closing ``` is missing, assume rest of string is JSON
                json = json.substring(firstBlockStart).trim();
            }
        }
        // Sometimes the response might just be the JSON object without backticks
        // Ensure it starts with { and ends with }
        if (!json.startsWith("{") && json.contains("{")) {
            json = json.substring(json.indexOf("{"));
        }
        if (!json.endsWith("}") && json.contains("}")) {
            json = json.substring(0, json.lastIndexOf("}") + 1);
        }


        return json;
    }

    /**
     * JSON 형식 오류 수정을 위한 정제 작업 (기본적인 정리)
     *
     * @param json 정제할 JSON 문자열
     * @return 정제된 JSON 문자열
     */
    private String cleanupJson(String json) {
        // 기본적인 정리 시도: 불필요한 후행 쉼표 제거 등
        return json.replaceAll(",\\s*}", "}")  // 객체 끝 불필요한 쉼표 제거
                .replaceAll(",\\s*]", "]")  // 배열 끝 불필요한 쉼표 제거
                .trim(); // 앞뒤 공백 제거
        // 더 복잡한 오류는 이 함수로 처리하기 어려울 수 있음
    }
}