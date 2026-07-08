package com.tikitaka.backend.question.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1}")
    private String model;

    @Value("${openai.responses-url:https://api.openai.com/v1/responses}")
    private String responsesUrl;

    public String generateAnswer(String questionContent, String lectureContext) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API 키가 설정되어 있지 않습니다.");
        }

        RestClient restClient = RestClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        String instructions = """
                너는 대학 강의 중 학생 질문에 답변하는 교수 보조 AI야.

                답변 규칙:
                1. 먼저 제공된 강의자료 내용을 가장 우선 기준으로 답변해.
                2. 질문이 강의자료에 직접적으로 나오지 않더라도, 강의자료와 관련 있는 내용이면 강의자료 맥락을 바탕으로 일반 지식 또는 웹 검색 결과를 보충해서 답변해.
                3. 강의자료에 없는 내용을 보충할 때는 "강의자료에는 직접 언급되지 않았지만"처럼 구분해서 말해.
                4. 확실하지 않은 내용은 단정하지 말고, 교수 확인이 필요하다고 말해.
                5. 답변은 한국어로 작성해.
                6. 답변은 너무 길지 않게 5문장 이상 10문장 이하로 작성해.
                7. 학생이 이해하기 쉽게 설명하되, 없는 내용을 지어내지 마.
                """;

        String input = """
                [학생 질문]
                %s

                [강의자료 내용]
                %s

                [요청]
                위 강의자료 내용을 우선 기준으로 학생 질문에 답변해줘.
                강의자료에 직접 없는 내용이면 강의자료와 연결해서 일반 지식 또는 웹 검색 결과로 보충해줘.
                """.formatted(
                questionContent,
                lectureContext == null || lectureContext.isBlank()
                        ? "현재 제공된 강의자료 텍스트가 없습니다."
                        : lectureContext
        );

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "instructions", instructions,
                "input", input,
                "tools", List.of(
                        Map.of(
                                "type", "web_search",
                                "search_context_size", "low"
                        )
                ),
                "tool_choice", "auto"
        );

        JsonNode response = restClient.post()
                .uri(responsesUrl)
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        return extractText(response);
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            throw new IllegalStateException("OpenAI 응답이 비어 있습니다.");
        }

        JsonNode outputText = response.get("output_text");
        if (outputText != null && !outputText.asText().isBlank()) {
            return outputText.asText();
        }

        JsonNode output = response.get("output");
        if (output != null && output.isArray()) {
            for (JsonNode outputItem : output) {
                JsonNode content = outputItem.get("content");

                if (content != null && content.isArray()) {
                    for (JsonNode contentItem : content) {
                        JsonNode text = contentItem.get("text");

                        if (text != null && !text.asText().isBlank()) {
                            return text.asText();
                        }
                    }
                }
            }
        }

        throw new IllegalStateException("OpenAI 응답에서 답변 텍스트를 찾을 수 없습니다.");
    }

    public String getModel() {
        return model;
    }
}