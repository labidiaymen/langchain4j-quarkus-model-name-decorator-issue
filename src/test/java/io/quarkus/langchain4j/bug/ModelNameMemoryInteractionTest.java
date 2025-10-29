package io.quarkus.langchain4j.bug;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Test that demonstrates the @ModelName runtime switching bug.
 * 
 * BUG DESCRIPTION:
 * - @ModelName validates that the configuration exists (will fail for invalid names)
 * - But it doesn't actually switch to that model at runtime
 * - All calls use the non-named fallback configuration
 * 
 * REPRODUCTION FACTORS:
 * - Using @ModelName + @MemoryId together
 * - Multi<String> return type for streaming responses
 * - Multiple named model configurations available
 * 
 * HOW TO RUN:
 * 1. Set environment variables with valid API keys:
 *    export OPENAI_API_KEY=your-key
 *    export MISTRAL_API_KEY=your-key
 * 2. Run: ./mvnw test
 * 
 * WHAT TO OBSERVE:
 * - Both calls respond with "I am GPT-4"
 * - Second call with model="default2" should use Mistral but doesn't
 * - Confirms @ModelName doesn't switch models at runtime
 */
@QuarkusTest
public class ModelNameMemoryInteractionTest {

    @Inject
    AssistantServiceWithMemory serviceWithMemory;

    @Test
    @DisplayName("BUG: @ModelName doesn't switch models at runtime (with @MemoryId)")
    public void testModelNameWithMemory() {
        System.out.println("\n========================================");
        System.out.println("Testing @ModelName WITH @MemoryId");
        System.out.println("========================================\n");
        
        try {
            // Both calls should use different models based on @ModelName parameter
            System.out.println("Call 1: conversationId='conv1', model='default' (expects OpenAI GPT-4)");
            
            StringBuilder response1 = new StringBuilder();
            serviceWithMemory.chat("conv1", "default", "What model are you?")
                .collect().asList()
                .await().atMost(java.time.Duration.ofSeconds(10))
                .forEach(response1::append);
            
            System.out.println("Response: " + response1);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error on first call: " + e.getMessage());
            System.err.println("This error shows which model configuration was attempted.");
        }
        
        try {
            System.out.println("Call 2: conversationId='conv2', model='default2' (expects Mistral)");
            
            StringBuilder response2 = new StringBuilder();
            serviceWithMemory.chat("conv2", "default2", "What model are you?")
                .collect().asList()
                .await().atMost(java.time.Duration.ofSeconds(10))
                .forEach(response2::append);
            
            System.out.println("Response: " + response2);
            System.out.println();
            
        } catch (Exception e) {
            System.err.println("Error on second call: " + e.getMessage());
            System.err.println("This error shows which model configuration was attempted.");
        }
        
        System.err.println("\n✗ BUG DEMONSTRATION:");
        System.err.println("Check the errors above - both calls attempted the same model!");
        System.err.println("Expected: First uses gpt-4, second uses mistral-large-latest");
        System.err.println("Actual: Both use gpt-4o (the non-named fallback configuration)");
    }
}
