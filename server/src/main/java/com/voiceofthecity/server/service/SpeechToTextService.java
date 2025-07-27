package com.voiceofthecity.server.service;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.cloud.translate.v3.*;
import com.google.protobuf.ByteString;
import com.voiceofthecity.server.service.SpeechToTextService.TranscriptionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
public class SpeechToTextService {

    private static final Logger log = LoggerFactory.getLogger(SpeechToTextService.class);

    @Value("file:${gcp.credentials.path}")
    private Resource gcpCredentials;

    public static class TranscriptionResult {
        private final String text;
        private final String languageCode;

        public TranscriptionResult(String text, String languageCode) {
            this.text = text;
            this.languageCode = languageCode;
        }

        public String getText() {
            return text;
        }

        public String getLanguageCode() {
            return languageCode;
        }
    }

    public Mono<TranscriptionResult> transcribeAudio(MultipartFile audioFile) {
        return Mono.fromCallable(() -> {
            ServiceAccountCredentials credentials = ServiceAccountCredentials.fromStream(gcpCredentials.getInputStream());
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            // Step 1: Speech-to-Text Transcription
            String initialTranscript = "";
            try (SpeechClient speechClient = SpeechClient.create(
                    SpeechSettings.newBuilder().setCredentialsProvider(credentialsProvider).build())) {

                ByteString audioBytes = ByteString.copyFrom(audioFile.getBytes());

                RecognitionConfig config = RecognitionConfig.newBuilder()
                        .setEncoding(RecognitionConfig.AudioEncoding.WEBM_OPUS)
                        .setSampleRateHertz(48000)
                        .setEnableAutomaticPunctuation(true)
                        .setLanguageCode("en-US")
                        .addAlternativeLanguageCodes("hi-IN")
                        .addAlternativeLanguageCodes("or-IN")
                        .addAlternativeLanguageCodes("es-ES")
                        .addAlternativeLanguageCodes("fr-FR")
                        .build();

                RecognitionAudio audio = RecognitionAudio.newBuilder().setContent(audioBytes).build();
                RecognizeResponse response = speechClient.recognize(config, audio);

                if (response != null && response.getResultsCount() > 0 &&
                        response.getResults(0).getAlternativesCount() > 0) {
                    initialTranscript = response.getResults(0).getAlternatives(0).getTranscript();
                }
            }

            if (initialTranscript.isEmpty()) {
                return new TranscriptionResult("", "");
            }

            // Step 2: Detect language using Translate API
            String detectedLanguage = "en-US";
            try (TranslationServiceClient translateClient = TranslationServiceClient.create(
                    TranslationServiceSettings.newBuilder().setCredentialsProvider(credentialsProvider).build())) {

                String parent = "projects/" + credentials.getProjectId() + "/locations/global";

                DetectLanguageRequest request = DetectLanguageRequest.newBuilder()
                        .setParent(parent)
                        .setMimeType("text/plain")
                        .setContent(initialTranscript)
                        .build();

                DetectLanguageResponse response = translateClient.detectLanguage(request);
                if (response != null && !response.getLanguagesList().isEmpty()) {
                    detectedLanguage = response.getLanguages(0).getLanguageCode();
                }
            }

            log.info("Transcript: {}", initialTranscript);
            log.info("Detected Language: {}", detectedLanguage);

            return new TranscriptionResult(initialTranscript, detectedLanguage);
        }).onErrorResume(ex -> {
            log.error("Error during transcription or language detection", ex);
            return Mono.just(new TranscriptionResult("", ""));
        });
    }
}
