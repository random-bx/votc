package com.voiceofthecity.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceofthecity.server.dto.QueryResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    // MODIFIED: This method now accepts simple strings instead of a request object.
    public Mono<QueryResponse> getTravelRecommendation(String query, String language) {
        String prompt = buildPrompt(query, language);

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        return webClient.post()
                .uri(apiUrl + "?key=" + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseGeminiResponse);
    }

    private String buildPrompt(String query, String language) {
        return String.format(
            "Role: 'Voice of the City'. " +
            "Input: lang=%s, query='%s'. " +
            "Task: Generate a strict JSON response in language %s. " +
            "The JSON must contain a 'places' array with exactly two recommendation objects. " +
            "Schema: {\"places\": [{\"name\": \"string\", \"mapLink\": \"string\"}]}. " +
            "Output only the raw JSON, with no other text.",
            language, query, language
        );
    }
    
    private QueryResponse parseGeminiResponse(JsonNode responseNode) {
        try {
            String textResponse = responseNode
                .path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();
            
            JsonNode innerJson = objectMapper.readTree(textResponse);
            return objectMapper.treeToValue(innerJson, QueryResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new QueryResponse(new ArrayList<>());
        }
    }
}