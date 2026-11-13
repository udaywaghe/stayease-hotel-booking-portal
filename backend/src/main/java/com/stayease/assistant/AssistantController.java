package com.stayease.assistant;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantService assistant;
    public AssistantController(AssistantService assistant) { this.assistant=assistant; }
    @PostMapping("/chat") public AssistantResponse chat(@Valid @RequestBody AssistantRequest request) { return assistant.chat(request); }
}
