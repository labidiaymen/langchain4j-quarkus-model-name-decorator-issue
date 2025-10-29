<!-- Workspace-specific custom instructions for GitHub Copilot -->

## Project Type
Minimal Quarkus project to reproduce LangChain4j @ModelName runtime switching bug

## Checklist

- [x] Verify that the copilot-instructions.md file in the .github directory is created.
- [x] Clarify Project Requirements
- [x] Scaffold the Project
- [x] Customize the Project
- [x] Install Required Extensions
- [x] Compile the Project
- [x] Ensure Documentation is Complete

## Project Complete

This is a minimal reproduction case for GitHub issue quarkiverse/quarkus-langchain4j#1901.
The project demonstrates that @ModelName validates configuration but doesn't switch models at runtime when used with Multi<String> streaming responses and @MemoryId.
