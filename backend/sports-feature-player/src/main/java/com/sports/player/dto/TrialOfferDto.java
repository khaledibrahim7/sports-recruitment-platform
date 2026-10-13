package com.sports.player.dto;

import com.sports.player.domain.TrialOffer;
import com.sports.player.domain.TrialOfferStatus;

import java.time.LocalDateTime;

public record TrialOfferDto(
        Long id,
        Long scoutUserId,
        String scoutName,
        String scoutRole,
        Long playerId,
        String playerName,
        String trialDate,
        String location,
        String details,
        TrialOfferStatus status,
        LocalDateTime createdAt
) {
    public static TrialOfferDto fromEntity(TrialOffer offer) {
        return new TrialOfferDto(
                offer.getId(),
                offer.getScoutUserId(),
                offer.getScoutName(),
                offer.getScoutRole(),
                offer.getPlayer() != null ? offer.getPlayer().getId() : null,
                offer.getPlayer() != null ? offer.getPlayer().getFullName() : null,
                offer.getTrialDate(),
                offer.getLocation(),
                offer.getDetails(),
                offer.getStatus(),
                offer.getCreatedAt()
        );
    }
}
