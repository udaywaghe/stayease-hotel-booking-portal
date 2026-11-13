package com.stayease.assistant;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AssistantIntent(String intent, String location, BigDecimal budget,
                              LocalDate checkIn, LocalDate checkOut, Integer guests,
                              List<String> amenities, String interests, Integer days) {
    public AssistantIntent {
        intent = intent == null ? "CHAT" : intent.toUpperCase();
        amenities = amenities == null ? List.of() : List.copyOf(amenities);
    }
}
