package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.security.UserPrincipal;
import com.sports.player.domain.TrialOfferStatus;
import com.sports.player.dto.CreateTrialOfferRequest;
import com.sports.player.dto.TrialOfferDto;
import com.sports.player.service.TrialOfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trial-offers")
@Tag(name = "Trial Offers", description = "Endpoints for sending and managing trial & scouting invitations")
public class TrialOfferController {

    private final TrialOfferService trialOfferService;

    public TrialOfferController(TrialOfferService trialOfferService) {
        this.trialOfferService = trialOfferService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ACADEMY', 'CLUB')")
    @Operation(summary = "Send a trial offer to a player")
    public ResponseEntity<ApiResponse<TrialOfferDto>> sendOffer(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody CreateTrialOfferRequest request) {
        TrialOfferDto created = trialOfferService.sendTrialOffer(
                userPrincipal.getId(),
                userPrincipal.getFullName(),
                userPrincipal.getRoleName(),
                request
        );
        return ResponseEntity.ok(ApiResponse.ok("تم إرسال طلب المعايشة بنجاح", created));
    }

    @GetMapping("/my-offers")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Get all trial offers received by authenticated player")
    public ResponseEntity<ApiResponse<List<TrialOfferDto>>> getMyOffers(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TrialOfferDto> offers = trialOfferService.getOffersForPlayer(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(offers));
    }

    @GetMapping("/sent-offers")
    @PreAuthorize("hasAnyRole('ACADEMY', 'CLUB')")
    @Operation(summary = "Get all trial offers sent by authenticated scout (club or academy)")
    public ResponseEntity<ApiResponse<List<TrialOfferDto>>> getSentOffers(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<TrialOfferDto> offers = trialOfferService.getOffersSentByScout(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.ok(offers));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('PLAYER')")
    @Operation(summary = "Update trial offer status (ACCEPT or REJECT)")
    public ResponseEntity<ApiResponse<TrialOfferDto>> updateStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id,
            @RequestParam TrialOfferStatus status) {
        TrialOfferDto updated = trialOfferService.updateOfferStatus(userPrincipal.getId(), id, status);
        String msg = status == TrialOfferStatus.ACCEPTED ? "تم قبول طلب المعايشة بنجاح!" : "تم رفض طلب المعايشة.";
        return ResponseEntity.ok(ApiResponse.ok(msg, updated));
    }
}
