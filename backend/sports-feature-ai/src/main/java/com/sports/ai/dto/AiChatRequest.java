package com.sports.ai.dto;

/**
 * Incoming chat/recommendation request from the frontend.
 */
public record AiChatRequest(
    /** The user's question or instruction in Arabic */
    String message,

    /** Optional extra context (e.g. "I am a club looking for a striker") */
    String context
) {}
