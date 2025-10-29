# @ModelName Parameter Not Working - Bug Reproduction

This is a minimal Quarkus project to reproduce the issue reported in [quarkiverse/quarkus-langchain4j#1901](https://github.com/quarkiverse/quarkus-langchain4j/issues/1901).

## Issue Description

The `@ModelName` parameter validates that model configuration exists but **doesn't actually switch models at runtime** when used with `Multi<String>` streaming responses and `@MemoryId`.

### Expected Behavior
When calling a service method with different `@ModelName` values, each call should use the specified model configuration.

### Actual Behavior
- ✅ `@ModelName` validates configuration exists (fails for invalid model names)
- ❌ All calls use the same model (non-named fallback configuration)
- The model specified by `@ModelName` parameter is ignored at runtime
- Issue occurs with `Multi<String>` return type for streaming responses

## Project Structure

```
bug-demo/
├── src/main/java/io/quarkus/langchain4j/bug/
│   └── AssistantServiceWithMemory.java    # Service with @MemoryId + @ModelName
├── src/main/resources/
│   └── application.properties             # Multiple named configurations
└── src/test/java/io/quarkus/langchain4j/bug/
    └── ModelNameMemoryInteractionTest.java # Test demonstrating the bug
```

## Configuration

The project configures three model setups:

1. **Non-named (fallback)**: OpenAI GPT-4o
2. **Named "default"**: OpenAI GPT-4  
3. **Named "default2"**: Mistral AI

```properties
# Non-named fallback
quarkus.langchain4j.chat-model.provider=openai
quarkus.langchain4j.openai.chat-model.model-name=gpt-4o

# Named: "default" 
quarkus.langchain4j.default.chat-model.provider=openai
quarkus.langchain4j.openai.default.chat-model.model-name=gpt-4

# Named: "default2"
quarkus.langchain4j.default2.chat-model.provider=mistralai
quarkus.langchain4j.mistralai.default2.chat-model.model-name=mistral-large-latest
```

## Code Example

```java
@RegisterAiService
public interface AssistantServiceWithMemory {
    
    @SystemMessage("You are a helpful assistant. Respond with your model name.")
    @UserMessage("{userMessage}")
    Multi<String> chat(  // Streaming response with Multi<String>
        @MemoryId String conversationId,
        @ModelName String model,
        String userMessage
    );
}
```

**Key factors**: The bug appears when combining:
1. `@ModelName` for runtime model selection
2. `@MemoryId` for conversation memory
3. `Multi<String>` return type for streaming responses

## How to Reproduce

### Prerequisites
- Java 17+
- Valid API keys for OpenAI and Mistral AI

### Steps

1. **Clone and navigate to the project:**
   ```bash
   cd bug-demo
   ```

2. **Set API keys:**
   ```bash
   export OPENAI_API_KEY=your-openai-key
   export MISTRAL_API_KEY=your-mistral-key
   ```

3. **Run the test:**
   ```bash
   ./mvnw test
   ```

4. **Observe the output:**
   - Both calls return "I am GPT-4"
   - Second call should return "I am Mistral" but uses the same model
   - The non-named fallback configuration (gpt-4o) is used for both
   - Expected: First call uses gpt-4, second uses mistral-large-latest

### Test Output Example

```
Call 1: conversationId='conv1', model='default' (expects OpenAI GPT-4)
Response: I am GPT-4.

Call 2: conversationId='conv2', model='default2' (expects Mistral)
Response: I am GPT-4.

✗ BUG DEMONSTRATION:
Both calls used the same model!
Expected: First uses gpt-4, second uses mistral-large-latest
Actual: Both use gpt-4o (the non-named fallback configuration)
```

## Expected vs Actual

| Call | Expected Model | Actual Model |
|------|----------------|--------------|
| `chat("conv1", "default", ...)` | OpenAI GPT-4 | GPT-4o (fallback) |
| `chat("conv2", "default2", ...)` | Mistral Large | GPT-4o (fallback) |

## Additional Observations

1. **Validation works**: Using an invalid model name (e.g., `"nonexistent"`) correctly fails validation
2. **Fallback always used**: Despite validation passing, runtime always uses the non-named configuration
3. **Combination of factors**: The issue appears when combining:
   - `@ModelName` for dynamic model selection
   - `@MemoryId` for conversation memory management
   - `Multi<String>` return type for streaming responses
4. **Consistent behavior**: Both streaming calls receive responses from the same model, confirming the bug

## Environment

- Quarkus: 3.28.4
- quarkus-langchain4j-bom: 3.28.4
- Java: 17

## Questions

Why does the framework require a non-named configuration when named configurations are available? This forces developers to always have a default connection even when they want to manage everything through named configurations.

---

**For maintainers**: This minimal reproduction case should help identify the root cause of the `@ModelName` parameter not switching models at runtime.
