package com.sysdatec.tickets.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AiClassificationService {

    private static final Logger log = LoggerFactory.getLogger(AiClassificationService.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    // 1. Abstracción declarativa de una regla de clasificación
    private record ClassificationRule(List<String> keywords, String category, String priority) {
        public boolean matches(String text) {
            return keywords.stream().anyMatch(text::contains);
        }
    }

    // 2. Definición inmutable de las reglas (Reglas de negocio como datos)
    private static final List<ClassificationRule> RULES = List.of(
            new ClassificationRule(List.of("factura", "pago", "cobro"), "Facturación", "ALTA"),
            new ClassificationRule(List.of("urgente", "caído", "error"), "Operaciones", "ALTA"),
            new ClassificationRule(List.of("ayuda", "configurar"), "Soporte Técnico", "MEDIA")
    );

    private static final ClassificationRule DEFAULT_RULE =
            new ClassificationRule(List.of(), "Soporte General", "BAJA");

    public AiClassificationService(
            @Value("${llm.api.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${llm.api.key:}") String apiKey,
            @Value("${llm.api.model:gpt-4o-mini}") String model) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.model = model;
    }

    public Map<String, Object> classifyAndSummarize(String requestText) {
        Map<String, Object> fallback = fallbackClassification(requestText);

        if (apiKey == null || apiKey.isBlank() || "sk-fake-key".equals(apiKey)) {
            return fallback;
        }

        try {
            Map<String, Object> aiResult = callLlmClassification(requestText);
            if (aiResult != null && !aiResult.isEmpty()) {
                return Map.of(
                        "category", sanitizeValue(aiResult.get("category"), String.valueOf(fallback.get("category"))),
                        "priority", sanitizeValue(aiResult.get("priority"), String.valueOf(fallback.get("priority"))),
                        "summary", sanitizeValue(aiResult.get("summary"), String.valueOf(fallback.get("summary")))
                );
            }
        } catch (RestClientException | JsonProcessingException ex) {
            log.warn("Fallo al consultar la API de LLM. Se usará la clasificación local.", ex);
        }

        return fallback;
    }

    private Map<String, Object> callLlmClassification(String requestText) throws JsonProcessingException {
        String prompt = "Analiza el siguiente ticket de soporte y responde únicamente en JSON con este formato: " +
                "{\"category\":\"...\",\"priority\":\"ALTA|MEDIA|BAJA\",\"summary\":\"resumen breve\"}. " +
                "No agregues texto extra. El texto del ticket es: " + requestText;

        Map<String, Object> payload = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "Eres un asistente para clasificar tickets de soporte en español."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.2
        );

        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(payload)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response == null || response.get("choices") == null) {
            return Map.of();
        }

        List<?> choices = (List<?>) response.get("choices");
        if (choices.isEmpty()) {
            return Map.of();
        }

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
        if (message == null || message.get("content") == null) {
            return Map.of();
        }

        String content = String.valueOf(message.get("content"));
        String jsonContent = extractJson(content);

        if (jsonContent.isBlank()) {
            return Map.of();
        }

        return OBJECT_MAPPER.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
    }

    private String extractJson(String content) {
        String cleaned = content.replace("```json", "")
                .replace("```", "")
                .trim();
        if (cleaned.startsWith("{")) {
            return cleaned;
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }
        return "";
    }

    private Map<String, Object> fallbackClassification(String requestText) {
        String normalizedText = Optional.ofNullable(requestText)
                .map(String::toLowerCase)
                .orElse("");

        ClassificationRule matchedRule = RULES.stream()
                .filter(rule -> rule.matches(normalizedText))
                .findFirst()
                .orElse(DEFAULT_RULE);

        String summary = "Solicitud analizada exitosamente (clasificación local). Resumen: " +
                Optional.ofNullable(requestText)
                        .filter(t -> t.length() > 60)
                        .map(t -> t.substring(0, 60) + "...")
                        .orElse(requestText == null ? "Sin descripción" : requestText);

        return Map.of(
                "category", matchedRule.category(),
                "priority", matchedRule.priority(),
                "summary", summary
        );
    }

    private String sanitizeValue(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        String text = String.valueOf(value).trim();
        return text.isBlank() ? fallback : text;
    }
}