package com.example.logistics.orders;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(UUID orderId, String customerEmail, BigDecimal amount) {}