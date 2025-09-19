package com.tn.maktba.dto.order;

import java.util.Map;

public record OrderRequests() {
    public record ModifyOrderRequest(Long userId, Map<Long, Integer> updates) {}
}
