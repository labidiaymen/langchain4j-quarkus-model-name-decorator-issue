package io.quarkus.langchain4j.bug;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.ModelName;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.smallrye.mutiny.Multi;

/**
 * This service demonstrates the BUG: @ModelName validates but doesn't switch models.
 * 
 * The issue occurs when combining:
 * - @ModelName for runtime model selection
 * - @MemoryId for conversation memory
 * - Multi<String> return type for streaming responses
 * 
 * Expected: Calling with model="default" uses OpenAI GPT-4, model="default2" uses Mistral
 * Actual: @ModelName validates the config exists, but always uses the same model (non-named fallback)
 */
@RegisterAiService
public interface AssistantServiceWithMemory {
    
    @SystemMessage("You are a helpful assistant. Respond with your model name.")
    @UserMessage("{userMessage}")
    Multi<String> chat(
        @MemoryId String conversationId,
        @ModelName String model,
        String userMessage
    );
}
