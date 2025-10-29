# Quick Start Guide

This project reproduces the @ModelName bug with Multi<String> streaming responses, reported in issue #1901.

## One-Command Test

```bash
# With valid API keys
export OPENAI_API_KEY=your-key
export MISTRAL_API_KEY=your-key
./mvnw test
```

## What You'll See

The test calls the service twice with different @ModelName parameters:
- `model="default"` → should use OpenAI GPT-4
- `model="default2"` → should use Mistral AI

**Bug**: Both calls respond with "I am GPT-4" (using the same non-named fallback model)

## The Issue

When combining `@ModelName` + `@MemoryId` + `Multi<String>` streaming:
- @ModelName validates configuration exists ✅
- @ModelName doesn't switch models at runtime ❌
- All calls use the non-named fallback configuration

## Key Files

- `AssistantServiceWithMemory.java` - Service with Multi<String>, @ModelName + @MemoryId
- `application.properties` - Multiple named model configurations
- `ModelNameMemoryInteractionTest.java` - Test showing both calls use same model

## Expected vs Actual Output

```
Expected:
  Call 1: "I am GPT-4"
  Call 2: "I am Mistral" or similar

Actual:
  Call 1: "I am GPT-4"
  Call 2: "I am GPT-4"  ← Bug: should be Mistral
```
