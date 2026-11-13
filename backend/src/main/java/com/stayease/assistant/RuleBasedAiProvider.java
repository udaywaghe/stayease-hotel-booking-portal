package com.stayease.assistant;

import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name="stayease.ai.provider", havingValue="local", matchIfMissing=true)
public class RuleBasedAiProvider implements AiProvider {
    private static final Pattern BUDGET = Pattern.compile("(?:under|below|less than|budget(?: of)?|\\p{Sc}|rs\\.?)[\\s\\p{Sc}]*([0-9][0-9,]*(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISO_DATE = Pattern.compile("\\b(20\\d{2}-\\d{2}-\\d{2})\\b");
    @Override public AssistantIntent extract(String message, String context) {
        String text = message.toLowerCase(Locale.ROOT);
        String intent = text.matches(".*\\b(nearby|near this|what can i explore|attractions|sightseeing)\\b.*") ? "ATTRACTIONS"
                : text.matches(".*\\b(itinerary|plan.*trip|day-wise|day by day|weekend trip|visit.*for.*days?)\\b.*") ? "ITINERARY"
                : text.matches(".*\\b(cheaper|cheapest|lower price|less expensive)\\b.*") ? "CHEAPER"
                : text.matches(".*\\b(price|cost|how much|estimate)\\b.*") ? "PRICE"
                : text.matches(".*\\b(find|hotel|stay|search|pool|beach|family-friendly)\\b.*") ? "SEARCH" : "CHAT";
        String location = null;
        for (String city : List.of("Mumbai", "Pune", "Goa", "Delhi", "Bengaluru", "Bangalore"))
            if (text.contains(city.toLowerCase(Locale.ROOT))) location = city.equals("Bangalore") ? "Bengaluru" : city;
        if (location == null && context != null) {
            Matcher m = Pattern.compile("(?i)location=([^;]+)").matcher(context);
            if (m.find()) location = m.group(1).trim();
        }
        BigDecimal budget = null;
        Matcher money = BUDGET.matcher(message);
        if (money.find()) try { budget = new BigDecimal(money.group(1).replace(",", "")); } catch (NumberFormatException ignored) { }
        LocalDate in = null, out = null;
        Matcher dates = ISO_DATE.matcher(message);
        try { if (dates.find()) in = LocalDate.parse(dates.group(1)); if (dates.find()) out = LocalDate.parse(dates.group(1)); } catch (RuntimeException ignored) { in = null; out = null; }
        List<String> amenities = new ArrayList<>();
        if (text.contains("pool") || text.contains("swimming")) amenities.add("Pool");
        if (text.contains("beach")) amenities.add("Sea view");
        if (text.contains("breakfast")) amenities.add("Breakfast");
        if (text.contains("spa")) amenities.add("Spa");
        Integer days = null;
        Matcher duration = Pattern.compile("(\\d+)\\s+days?").matcher(text);
        if (duration.find()) try { days = Math.min(14, Integer.parseInt(duration.group(1))); } catch (RuntimeException ignored) { }
        Integer guests = text.contains("family") ? 3 : null;
        return new AssistantIntent(intent, location, budget, in, out, guests, amenities, null, days);
    }
    @Override public boolean isConfigured() { return false; }
}
