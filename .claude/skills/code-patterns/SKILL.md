---
name: code-patterns
description: Detailed code examples and implementation templates for this project. Use this skill whenever writing new code, creating new files, implementing features, or when you need to see concrete examples of how this project implements patterns. Consult this skill before writing any substantial new code to ensure you match existing patterns exactly.
---

# Code Patterns for Basisprojekt

The authoritative rules are in the root CLAUDE.md. This skill provides detailed, real examples of those rules in action.

## When to use this skill
- Writing a new feature, domain object, entity, repository, service, or controller
- Unsure HOW to implement a pattern mentioned in CLAUDE.md -- check here for examples
- The user asks to see "how we do X" with concrete code

## Reference Files
Load the reference file matching your current task:
- `references/implementation-examples.md` -- Full annotated examples of creating new features from scratch, extracted from the actual codebase (value objects, entities, repositories, services, controllers)
- `references/error-handling-examples.md` -- Complete error handling patterns with real code showing Optional usage, ResponseStatusException, requireNonNull
- `references/test-examples.md` -- Full test file example showing Testcontainers setup, RestClient usage, Given/When/Then structure
