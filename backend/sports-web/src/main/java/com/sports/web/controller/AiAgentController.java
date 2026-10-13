package com.sports.web.controller;

import com.sports.ai.dto.AiChatRequest;
import com.sports.ai.dto.AiChatResponse;
import com.sports.ai.service.AiAgentService;
import com.sports.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST entry-point for the AI Scouting Agent.
 * Delegates all logic to the isolated sports-feature-ai module.
 */
@RestController
@RequestMapping("/api/v1/ai-agent")
@Tag(name = "AI Scouting Agent", description = "AI-powered scouting assistant using Google Gemini — Arabic NLP")
public class AiAgentController {

    private final AiAgentService aiAgentService;

    public AiAgentController(AiAgentService aiAgentService) {
        this.aiAgentService = aiAgentService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with the AI scouting agent in Arabic")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiAgentService.chat(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/analyze-player/{playerId}")
    @Operation(summary = "Generate a full AI scouting report for a player")
    public ResponseEntity<ApiResponse<AiChatResponse>> analyzePlayer(@PathVariable Long playerId) {
        AiChatResponse response = aiAgentService.analyzePlayer(playerId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/recommend")
    @Operation(summary = "AI recommends best-matching players based on criteria")
    public ResponseEntity<ApiResponse<AiChatResponse>> recommend(@RequestBody AiChatRequest request) {
        AiChatResponse response = aiAgentService.recommend(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
