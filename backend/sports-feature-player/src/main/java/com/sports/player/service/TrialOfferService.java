package com.sports.player.service;

import com.sports.player.domain.TrialOfferStatus;
import com.sports.player.dto.CreateTrialOfferRequest;
import com.sports.player.dto.TrialOfferDto;

import java.util.List;

public interface TrialOfferService {
    TrialOfferDto sendTrialOffer(Long scoutUserId, String scoutName, String scoutRole, CreateTrialOfferRequest request);
    List<TrialOfferDto> getOffersForPlayer(Long playerUserId);
    List<TrialOfferDto> getOffersSentByScout(Long scoutUserId);
    TrialOfferDto updateOfferStatus(Long playerUserId, Long offerId, TrialOfferStatus status);
}
