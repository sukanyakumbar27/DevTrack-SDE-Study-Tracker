package com.devtrack.controller;

import com.devtrack.dto.ResponseDtos.AnalyticsDto;
import com.devtrack.dto.ResponseDtos.DashboardDto;
import com.devtrack.entity.User;
import com.devtrack.service.AnalyticsService;
import com.devtrack.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DashboardController {
    private final DashboardService dashboard;
    private final AnalyticsService analytics;

    public DashboardController(DashboardService dashboard, AnalyticsService analytics) {
        this.dashboard = dashboard;
        this.analytics = analytics;
    }

    @GetMapping("/dashboard")
    public DashboardDto dashboard(@AuthenticationPrincipal User user) {
        return dashboard.build(user.getId());
    }

    @GetMapping("/analytics")
    public AnalyticsDto analytics(@AuthenticationPrincipal User user) {
        return analytics.build(user.getId());
    }
}
