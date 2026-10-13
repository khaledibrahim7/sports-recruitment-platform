package com.sports.ai.service;

import com.sports.ai.dto.AiChatRequest;
import com.sports.ai.dto.AiChatResponse;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.service.PlayerProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Production-grade AI Agent Service Implementation.
 * - All Gemini config externalized to application.yml (no hardcoded secrets/URLs)
 * - Player list cached to prevent DB scan on every AI request
 * - No internal details leaked in error messages
 */
@Service
public class AiAgentServiceImpl implements AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentServiceImpl.class);

    // ── Injected from application.yml — never hardcoded ──────────────────
    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.model:gemini-2.0-flash}")
    private String geminiModel;

    @Value("${app.gemini.api-version:v1beta}")
    private String geminiApiVersion;

    @Value("${app.gemini.temperature:0.7}")
    private double temperature;

    @Value("${app.gemini.max-output-tokens:2000}")
    private int maxOutputTokens;

    private static final String SYSTEM_PERSONA = """
        أنت "كاشف" — مساعد ذكاء اصطناعي متخصص في التكشيف الرياضي لكرة القدم،
        تعمل على منصة "الكورة لعبتنا" العربية المتخصصة في ربط اللاعبين بالأندية والأكاديميات.

        قواعد الاستجابة:
        - تحدث دائمًا باللغة العربية الفصحى المبسطة.
        - كن مهنيًا، دقيقًا، وموجزًا.
        - لا تخترع بيانات غير موجودة في السياق المُعطى.
        - إذا لم تجد معلومة، اعترف بذلك بدلاً من الاختراع.
        - استخدم التحليل الإحصائي الرياضي في تقاريرك.
        """;

    private final PlayerProfileService playerProfileService;
    private final RestTemplate restTemplate;

    public AiAgentServiceImpl(PlayerProfileService playerProfileService) {
        this.playerProfileService = playerProfileService;
        this.restTemplate = new RestTemplate();
    }

    // ─────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        try {
            List<PlayerProfileDto> players = getCachedPlayers();
            String context = buildPlatformSummary(players, request.context());
            String prompt = SYSTEM_PERSONA + "\n\n" + context
                + "\n\nسؤال المستخدم: " + request.message()
                + "\n\nالإجابة المهنية:";
            return new AiChatResponse(callGemini(prompt), true);
        } catch (Exception e) {
            log.error("AI chat error: {}", e.getMessage());
            return new AiChatResponse(fallback(request.message()), false);
        }
    }

    @Override
    public AiChatResponse analyzePlayer(Long playerId) {
        try {
            PlayerProfileDto p = playerProfileService.getPlayerById(playerId);
            String prompt = SYSTEM_PERSONA + "\n\n" + buildAnalysisPrompt(p);
            return new AiChatResponse(callGemini(prompt), true);
        } catch (Exception e) {
            log.error("Player analysis error for id={}: {}", playerId, e.getMessage());
            return new AiChatResponse("تعذّر إنشاء تقرير التكشيف لهذا اللاعب. يرجى المحاولة لاحقًا.", false);
        }
    }

    @Override
    public AiChatResponse recommend(AiChatRequest request) {
        try {
            List<PlayerProfileDto> players = getCachedPlayers();
            String prompt = SYSTEM_PERSONA + "\n\n" + buildRecommendationPrompt(players, request.message());
            return new AiChatResponse(callGemini(prompt), true);
        } catch (Exception e) {
            log.error("Recommendation error: {}", e.getMessage());
            return new AiChatResponse("تعذّر إنشاء قائمة التوصيات. يرجى المحاولة لاحقًا.", false);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Cached Player List — prevents full DB scan on every AI request
    // ─────────────────────────────────────────────────────────────

    @Cacheable(value = "players", key = "'all'")
    public List<PlayerProfileDto> getCachedPlayers() {
        return playerProfileService.getAllPlayers(null, null);
    }

    // ─────────────────────────────────────────────────────────────
    // Prompt Builders
    // ─────────────────────────────────────────────────────────────

    private String buildPlatformSummary(List<PlayerProfileDto> players, String extraContext) {
        var sb = new StringBuilder("=== بيانات منصة التكشيف المباشرة ===\n");
        sb.append("إجمالي اللاعبين المسجلين: ").append(players.size()).append("\n\n");

        if (!players.isEmpty()) {
            sb.append("اللاعبون:\n");
            for (PlayerProfileDto p : players) {
                sb.append("• ").append(p.getFullName())
                  .append(" | ").append(p.getAge()).append(" سنة")
                  .append(" | مركز: ").append(p.getPosition())
                  .append(" | سرعة:").append(safe(p.getPace(), 75))
                  .append(" تسديد:").append(safe(p.getShooting(), 70))
                  .append(" تمرير:").append(safe(p.getPassing(), 70))
                  .append(" مراوغة:").append(safe(p.getDribbling(), 75))
                  .append(" دفاع:").append(safe(p.getDefending(), 65))
                  .append(" بدنية:").append(safe(p.getPhysicality(), 70))
                  .append("\n");
            }
        }

        if (extraContext != null && !extraContext.isBlank()) {
            sb.append("\nسياق المستخدم: ").append(extraContext).append("\n");
        }

        return sb.toString();
    }

    private String buildAnalysisPrompt(PlayerProfileDto p) {
        return """
            أنشئ تقرير تكشيف رياضي شامل ومهني للاعب التالي:

            ━━ البيانات الشخصية ━━
            الاسم: %s | العمر: %d سنة | المركز: %s
            الطول: %d سم | الوزن: %d كجم | القدم: %s

            ━━ مؤشرات الأداء (من 99) ━━
            السرعة: %d | التسديد: %d | التمرير: %d
            المراوغة: %d | الدفاع: %d | القوة البدنية: %d

            ━━ النبذة ━━
            %s

            المطلوب في التقرير:
            ① ملخص تنفيذي (٣ أسطر)
            ② نقاط القوة الرئيسية
            ③ نقاط تحتاج تطوير
            ④ أفضل مركز له بناءً على المؤشرات
            ⑤ مقارنة بالمعايير العالمية لسنه
            ⑥ توصية التكشيف النهائية: مناسب للاستقطاب أم لا؟ ولماذا؟
            """.formatted(
                p.getFullName(),
                p.getAge() != null ? p.getAge() : 18,
                p.getPosition(),
                safe(p.getHeightCm(), 175),
                safe(p.getWeightKg(), 70),
                p.getPreferredFoot() != null ? p.getPreferredFoot() : "RIGHT",
                safe(p.getPace(), 75),
                safe(p.getShooting(), 70),
                safe(p.getPassing(), 70),
                safe(p.getDribbling(), 75),
                safe(p.getDefending(), 65),
                safe(p.getPhysicality(), 70),
                p.getBio() != null ? p.getBio() : "لا توجد نبذة مدخلة"
        );
    }

    private String buildRecommendationPrompt(List<PlayerProfileDto> players, String criteria) {
        var sb = new StringBuilder();
        sb.append("بناءً على المعايير التالية، رشّح أفضل ثلاثة لاعبين مع التبرير المفصل:\n\n");
        sb.append("المعايير: ").append(criteria).append("\n\n");
        sb.append("اللاعبون المتاحون في المنصة:\n");

        for (PlayerProfileDto p : players) {
            sb.append("• %s | %d سنة | %s | سرعة:%d تسديد:%d تمرير:%d مراوغة:%d\n"
                .formatted(
                    p.getFullName(),
                    p.getAge() != null ? p.getAge() : 18,
                    p.getPosition(),
                    safe(p.getPace(), 75),
                    safe(p.getShooting(), 70),
                    safe(p.getPassing(), 70),
                    safe(p.getDribbling(), 75)
                ));
        }

        sb.append("\nالتوصيات (مرتبة حسب الأولوية، مع تبرير واضح لكل لاعب):");
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────
    // Gemini API Call — URL built from config, no hardcoded values
    // ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String callGemini(String prompt) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            log.warn("Gemini API key is not configured.");
            return "⚠️ لم يتم تكوين مفتاح AI. يرجى التواصل مع إدارة المنصة.";
        }

        // URL built entirely from injected config — no hardcoded strings
        String url = String.format(
            "https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s",
            geminiApiVersion, geminiModel, geminiApiKey
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> part = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", List.of(part));
        Map<String, Object> generationConfig = Map.of(
            "temperature", temperature,
            "maxOutputTokens", maxOutputTokens
        );
        Map<String, Object> body = Map.of(
            "contents", List.of(content),
            "generationConfig", generationConfig
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getBody() != null) {
            var candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                var contentObj = (Map<String, Object>) candidates.get(0).get("content");
                if (contentObj != null) {
                    var parts = (List<Map<String, Object>>) contentObj.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        }
        return "لم أتمكن من الحصول على رد من المساعد الذكي. حاول مرة أخرى.";
    }

    // ─────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────

    private int safe(Integer value, int defaultVal) {
        return value != null ? value : defaultVal;
    }

    private String fallback(String message) {
        if (message != null && (message.contains("لاعب") || message.contains("تكشيف"))) {
            return "يمكنني مساعدتك في تحليل بيانات اللاعبين. تعذّر الاتصال حاليًا، حاول مرة أخرى.";
        }
        return "أنا مساعدك الذكي للتكشيف الرياضي 'كاشف'. تعذّر الاتصال حاليًا، يرجى المحاولة لاحقًا.";
    }
}
