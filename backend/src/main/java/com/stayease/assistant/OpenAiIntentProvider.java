package com.stayease.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
@ConditionalOnProperty(name="stayease.ai.provider", havingValue="openai")
public class OpenAiIntentProvider implements AiProvider {
    private final RestClient client;
    private final ObjectMapper mapper;
    private final String model;
    public OpenAiIntentProvider(@Value("${stayease.ai.openai.api-key:}") String key,
                                @Value("${stayease.ai.openai.model:gpt-4.1-mini}") String model,
                                ObjectMapper mapper) {
        this.client = RestClient.builder().baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + key).build();
        this.mapper = mapper; this.model = model;
    }
    @Override public AssistantIntent extract(String message, String recentContext) {
        try {
            Map<String,Object> schema = Map.of("type","object","properties",Map.of(
                    "intent",Map.of("type","string","enum",new String[]{"SEARCH","CHEAPER","PRICE","ATTRACTIONS","ITINERARY","CHAT"}),
                    "location",Map.of("type",new String[]{"string","null"}),
                    "budget",Map.of("type",new String[]{"number","null"}),
                    "checkIn",Map.of("type",new String[]{"string","null"}),
                    "checkOut",Map.of("type",new String[]{"string","null"}),
                    "guests",Map.of("type",new String[]{"integer","null"}),
                    "amenities",Map.of("type","array","items",Map.of("type","string")),
                    "interests",Map.of("type",new String[]{"string","null"}),
                    "days",Map.of("type",new String[]{"integer","null"})),
                    "required",new String[]{"intent","location","budget","checkIn","checkOut","guests","amenities","interests","days"},"additionalProperties",false);
            Map<String,Object> body = Map.of("model",model,"store",false,"instructions",
                    "Extract travel intent as JSON. Use dates in ISO YYYY-MM-DD using today's date "+java.time.LocalDate.now()+". Never answer the user. Use only supported database destinations Mumbai, Pune, Goa, Delhi, Bengaluru. Preserve context where relevant. Set unknown values null. Amenities must be hotel amenities.",
                    "input", "Context: "+(recentContext == null ? "none" : recentContext)+"\nUser: "+message,
                    "text",Map.of("format",Map.of("type","json_schema","name","travel_intent","strict",true,"schema",schema)));
            JsonNode response = client.post().uri("/responses").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
            String output = response.path("output_text").asText();
            JsonNode result = mapper.readTree(output);
            return new AssistantIntent(result.path("intent").asText("CHAT"), nullableText(result,"location"),
                    result.path("budget").isNumber()?result.path("budget").decimalValue():null,
                    nullableDate(result,"checkIn"), nullableDate(result,"checkOut"),
                    result.path("guests").isInt()?result.path("guests").asInt():null,
                    mapper.convertValue(result.path("amenities"),mapper.getTypeFactory().constructCollectionType(java.util.List.class,String.class)),
                    nullableText(result,"interests"),result.path("days").isInt()?result.path("days").asInt():null);
        } catch (Exception ex) { throw new AiProviderException("The travel assistant provider is unavailable",ex); }
    }
    private String nullableText(JsonNode n,String f){return n.path(f).isTextual()?n.path(f).asText():null;}
    private java.time.LocalDate nullableDate(JsonNode n,String f){try{return n.path(f).isTextual()?java.time.LocalDate.parse(n.path(f).asText()):null;}catch(Exception e){return null;}}
    @Override public boolean isConfigured(){return true;}
}
