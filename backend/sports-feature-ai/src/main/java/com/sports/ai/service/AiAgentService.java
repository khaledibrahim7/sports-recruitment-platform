package com.sports.ai.service;

import com.sports.ai.dto.AiChatRequest;
import com.sports.ai.dto.AiChatResponse;

/**
 * AI Scouting Agent Service — core contract for all AI-powered operations.
 */
public interface AiAgentService {

    /**
     * General-purpose chat with the scouting AI agent.
     * The agent is automatically enriched with real platform data (players, stats).
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * Generate a comprehensive scouting report for a specific player.
     *
     * @param playerId the player's ID in the platform database
     */
    AiChatResponse analyzePlayer(Long playerId);

    /**
     * Recommend the best-matching players based on textual criteria
     * (e.g. "نادٍ يبحث عن مهاجم يساري عمره أقل من 22 سنة، سريع وسداد ممتاز").
     */
    AiChatResponse recommend(AiChatRequest request);
}
