package com.sports.player.service;

import com.sports.common.service.NotificationService;
import com.sports.player.domain.PlayerProfile;
import com.sports.player.domain.TrialOffer;
import com.sports.player.domain.TrialOfferStatus;
import com.sports.player.dto.CreateTrialOfferRequest;
import com.sports.player.dto.TrialOfferDto;
import com.sports.player.repository.PlayerProfileRepository;
import com.sports.player.repository.TrialOfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrialOfferServiceImpl implements TrialOfferService {

    private final TrialOfferRepository trialOfferRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final NotificationService notificationService;

    public TrialOfferServiceImpl(
            TrialOfferRepository trialOfferRepository,
            PlayerProfileRepository playerProfileRepository,
            NotificationService notificationService) {
        this.trialOfferRepository = trialOfferRepository;
        this.playerProfileRepository = playerProfileRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public TrialOfferDto sendTrialOffer(Long scoutUserId, String scoutName, String scoutRole, CreateTrialOfferRequest request) {
        PlayerProfile player = playerProfileRepository.findById(request.playerId())
                .orElseThrow(() -> new IllegalArgumentException("Player profile not found with id: " + request.playerId()));

        TrialOffer offer = new TrialOffer(
                scoutUserId,
                scoutName,
                scoutRole,
                player,
                request.trialDate(),
                request.location(),
                request.details()
        );

        TrialOffer saved = trialOfferRepository.save(offer);

        // Send notification to player
        String roleTitle = "ACADEMY".equals(scoutRole) ? "أكاديمية" : "نادي";
        notificationService.createNotification(
                player.getUser().getId(),
                "عرض معايشة واختبار جديد ⚽",
                "أرسلت لك " + roleTitle + " (" + scoutName + ") طلب معايشة واختبار بتاريخ " + request.trialDate() + " في " + request.location() + ".",
                "TRIAL_OFFER"
        );

        return TrialOfferDto.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrialOfferDto> getOffersForPlayer(Long playerUserId) {
        return trialOfferRepository.findByPlayerUserIdWithPlayer(playerUserId)
                .stream()
                .map(TrialOfferDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrialOfferDto> getOffersSentByScout(Long scoutUserId) {
        return trialOfferRepository.findByScoutUserIdWithPlayer(scoutUserId)
                .stream()
                .map(TrialOfferDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public TrialOfferDto updateOfferStatus(Long playerUserId, Long offerId, TrialOfferStatus status) {
        TrialOffer offer = trialOfferRepository.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Trial offer not found with id: " + offerId));

        if (!offer.getPlayer().getUser().getId().equals(playerUserId)) {
            throw new IllegalStateException("You are not authorized to update this trial offer.");
        }

        offer.setStatus(status);
        TrialOffer saved = trialOfferRepository.save(offer);

        // Notify the scout of player's response
        String statusText = status == TrialOfferStatus.ACCEPTED ? "قبول" : "رفض";
        notificationService.createNotification(
                offer.getScoutUserId(),
                "رد على طلب المعايشة " + (status == TrialOfferStatus.ACCEPTED ? "✅" : "❌"),
                "قام اللاعب (" + offer.getPlayer().getFullName() + ") بـ " + statusText + " طلب المعايشة الخاص بك.",
                "TRIAL_RESPONSE"
        );

        return TrialOfferDto.fromEntity(saved);
    }
}
