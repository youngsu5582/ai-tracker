package youngsu5582.tool.ai_tracker.presentation.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 루트 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaptureRequest {

    public String getPayload() {
        return message.content.parts.getFirst();
    }

    /**
     * 하위 노드들 (타입 미정 -> Object로 수용)
     */
    private List<String> children;

    @JsonProperty("conversation_id")
    private String conversationId;

    /**
     * 현재 노드 id
     */
    private String id;

    /**
     * 본문 메시지
     */
    private Message message;

    /**
     * Unix epoch seconds (예: 1759226559.526345)
     */
    @JsonProperty("create_time")
    private Instant createTime;

    @JsonProperty("end_turn")
    private Boolean endTurn;

    /**
     * 실행/메타 정보
     */
    private Metadata metadata;

    /**
     * 수신 대상 (예: "all")
     */
    private String recipient;

    /**
     * 처리 상태 (예: "finished_successfully")
     */
    private String status;

    /**
     * 갱신 시간 (null/number/string 등 가변)
     */
    @JsonProperty("update_time")
    private Instant updateTime;

    /**
     * 가중치 (숫자)
     */
    private Integer weight;

    /**
     * 상위 메시지 id
     */
    private String parent;

    // ---------- Nested Types ----------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {

        private Author author;
        private String channel;
        private Content content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {

        private Map<String, Object> metadata;   // 구조 미정 → Object
        private String name;       // null 가능
        private String role;       // "assistant" | "user" | "system" 등
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {

        @JsonProperty("content_type")
        private String contentType;   // "text" 등
        private List<String> parts;   // 코드블록 등 문자열 파트 리스트
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {

        @JsonProperty("content_references")
        private List<ContentReference> contentReferences;

        @JsonProperty("default_model_slug")
        private String defaultModelSlug;

        @JsonProperty("finish_details")
        private FinishDetails finishDetails;

        @JsonProperty("is_complete")
        private Boolean isComplete;

        @JsonProperty("message_type")
        private String messageType;

        @JsonProperty("model_slug")
        private String modelSlug;

        @JsonProperty("parent_id")
        private String parentId;

        @JsonProperty("request_id")
        private String requestId;

        @JsonProperty("thinking_effort")
        private String thinkingEffort;

        /**
         * 원 키 이름이 'timestamp_' 이므로 명시 매핑
         */
        @JsonProperty("timestamp_")
        private String timestamp;

        @JsonProperty("turn_exchange_id")
        private String turnExchangeId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentReference {

        private String alt;

        @JsonProperty("end_idx")
        private Integer endIdx;

        @JsonProperty("has_images")
        private Boolean hasImages;

        @JsonProperty("matched_text")
        private String matchedText;

        @JsonProperty("prompt_text")
        private String promptText;

        @JsonProperty("start_idx")
        private Integer startIdx;

        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FinishDetails {

        @JsonProperty("stop_tokens")
        private List<Integer> stopTokens;
        private String type;
    }
}