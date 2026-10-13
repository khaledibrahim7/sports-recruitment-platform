package com.sports.ai.dto;

/**
 * AI Agent response returned to the frontend.
 */
public record AiChatResponse(
    /** The AI-generated reply in Arabic */
    String reply,

    /** True if the call to Gemini succeeded */
    boolean success
) {}
