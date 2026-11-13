package com.stayease.assistant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssistantRequest(@NotBlank @Size(max = 4000) String message,
                               @Size(max = 80) String sessionId) {}
