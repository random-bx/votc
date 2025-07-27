package com.voiceofthecity.server.controller;

import com.voiceofthecity.server.dto.QueryResponse;
import com.voiceofthecity.server.service.GeminiService;
import com.voiceofthecity.server.service.SpeechToTextService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class TravelController {

    private final SpeechToTextService speechToTextService;
    private final GeminiService geminiService;

    public TravelController(SpeechToTextService speechToTextService, GeminiService geminiService) {
        this.speechToTextService = speechToTextService;
        this.geminiService = geminiService;
    }

    @PostMapping("/transcribe")
    public Mono<QueryResponse> transcribeAndRecommend(@RequestParam("audio") MultipartFile audioFile) throws IOException {
        // Step 1: Transcribe the audio to get text and detected language.
        return speechToTextService.transcribeAudio(audioFile)
            .flatMap(transcriptionResult -> {
                // If transcription is empty, return an empty response to the frontend.
                if (transcriptionResult.getText() == null || transcriptionResult.getText().isEmpty()) {
                    return Mono.just(new QueryResponse(null)); 
                }
                // Step 2: Use the transcription result to call the Gemini service.
                return geminiService.getTravelRecommendation(
                        transcriptionResult.getText(),
                        transcriptionResult.getLanguageCode()
                );
            });
    }
}