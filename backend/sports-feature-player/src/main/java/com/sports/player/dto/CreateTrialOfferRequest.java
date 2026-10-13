package com.sports.player.dto;

public record CreateTrialOfferRequest(
        Long playerId,
        String trialDate,
        String location,
        String details
) {}
