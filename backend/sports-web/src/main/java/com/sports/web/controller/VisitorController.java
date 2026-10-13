package com.sports.web.controller;

import com.sports.common.dto.ApiResponse;
import com.sports.common.service.VisitorCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/visitors")
@Tag(name = "Platform Visitor Counter", description = "Real-time Visitor Counter Endpoints")
public class VisitorController {

    private final VisitorCounterService visitorCounterService;

    public VisitorController(VisitorCounterService visitorCounterService) {
        this.visitorCounterService = visitorCounterService;
    }

    @PostMapping("/visit")
    @Operation(summary = "Register a new visitor view and increment counter")
    public ResponseEntity<ApiResponse<Long>> registerVisit() {
        long currentCount = visitorCounterService.incrementAndGet();
        return ResponseEntity.ok(ApiResponse.ok("Visit counted", currentCount));
    }

    @GetMapping("/count")
    @Operation(summary = "Get current total visitor count")
    public ResponseEntity<ApiResponse<Long>> getVisitorCount() {
        long currentCount = visitorCounterService.getCount();
        return ResponseEntity.ok(ApiResponse.ok(currentCount));
    }
}
