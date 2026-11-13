package com.stayease.assistant;

public interface AiProvider {
    AssistantIntent extract(String message, String recentContext);
    default boolean isConfigured() { return true; }
}
