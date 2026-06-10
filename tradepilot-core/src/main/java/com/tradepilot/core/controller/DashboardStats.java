package com.tradepilot.core.controller;

public record DashboardStats(
        long totalOrders,
        long quotedOrders,
        long confirmedOrders,
        long overduePayments,
        long pendingFollowUps
) {}
