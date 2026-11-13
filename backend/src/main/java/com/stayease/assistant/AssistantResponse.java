package com.stayease.assistant;

import com.stayease.dto.HotelResponse;
import java.util.List;

public record AssistantResponse(String sessionId, String reply, String intent,
                                List<HotelResponse> hotels, List<Attraction> attractions,
                                List<ItineraryDay> itinerary, String estimateNote, SearchDetails search) {
    public record Attraction(String name, String description, String sourceName, String sourceUrl) {}
    public record ItineraryDay(int day, String title, List<String> activities) {}
    public record SearchDetails(String location, java.math.BigDecimal budget, java.time.LocalDate checkIn,
                                java.time.LocalDate checkOut, Integer guests) {}
}
