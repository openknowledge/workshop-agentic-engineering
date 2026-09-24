# Project guidelines

Customer management application, consisting of:

- `customer-management-server` — Spring Boot backend (Maven, Java), base package `de.openknowledge.customermanagement`.
- `customer-management-client` — React + TypeScript frontend (Vite) with unit tests in `tests` and Playwright E2E tests in `e2e`.

## Multi-agent setup

Features are implemented by the `orchestrator` primary agent (see `.opencode/agents/orchestrator.md`), which delegates each phase to the subagents `ux-designer`, `backend-dev`, `frontend-dev` and `review` (see `.opencode/agents/`). Feature specs live in `docs/specs/`.

Within this multi-agent workflow, the agents work autonomously: they do not ask the user questions, make reasonable assumptions, document them, and proceed. This does not apply to interactive commands such as `/grill-me` or `/to-spec`, which are meant to interview the user.
