# Workshop Agentic Engineering

Welcome to the Agentic Engineering workshop.
In this workshop, we will use a customer management application as our sample application.

## Customer Management Application

Customer Management is a sample project for managing customers. It consists of two Maven modules:

- **customer-management-server** – Backend based on Spring Boot (Java 21 or later) that provides the REST API and data persistence (PostgreSQL, Flyway migrations).
- **customer-management-client** – Frontend based on React/TypeScript/Vite that is integrated into the Maven build via the Frontend Maven Plugin.

The server embeds the built frontend and delivers the entire application as a single executable artifact.

### Prerequisites

- Java 21 or later
- Docker and Docker Compose (for building/starting the containers)

### Build with Maven

The project root contains a Maven wrapper, so no local Maven installation is required.

Build the entire project (client and server):

```bash
./mvnw clean install
```

### Full Stack with Docker Compose

Since the server requires a PostgreSQL database and a WireMock server as runtime dependencies, starting it via Docker Compose is recommended. This starts the database, Flyway migrations, WireMock and the server together:

```bash
docker compose up --build
```

The application is then available at [http://localhost:8080](http://localhost:8080).

The setup also works with Podman.

### Running the Application Locally

First, start the database and the WireMock server via Docker Compose:

```bash
docker compose up --build customer-management-database flyway wiremock
```

Then start the server locally:

```bash
./mvnw -pl customer-management-server spring-boot:run
```

For local development, the frontend can be started separately with hot reload:

```bash
cd customer-management-client
npm install
npm run dev
```

### Installing opencode

As part of the Maven build (`./mvnw clean install`), the Frontend Maven Plugin also automatically installs [opencode](https://opencode.ai) locally in `customer-management-client/node`. A global installation is not required.

After a successful `./mvnw clean install`, opencode can be started as follows:

```bash
./customer-management-client/node/bin/opencode
```

On Windows, the executable is located at `node\opencode.cmd` instead:

```bash
\customer-management-client\node\opencode.cmd
```

Within opencode, an AI provider can be selected and configured via `/connect`.

## Exercise: Agentic Workflow with the Orchestrator

In this exercise, you will try out how a multi-agent workflow implements a feature end to end based on a spec.

The `orchestrator` agent is configured as the default agent (see `opencode.json`). It does not write any code itself, but delegates each phase to specialized subagents (see `.opencode/agents/`):

- `ux-designer` – derives the functional requirements and acceptance scenarios from the spec and later reviews the implemented tests.
- `frontend-dev` – implements click-dummy tests and pages first, then the real UI and the end-to-end tests.
- `backend-dev` – designs and implements the REST API test-driven.
- `review` – checks backend, frontend and tests for consistency.

### The Task

**New feature**: The spec [`docs/specs/customer-addresses.md`](docs/specs/customer-addresses.md) describes customer addresses as a sub-resource of the customer.

1. **Read the spec**: Get an overview of the feature and the decisions in the spec.
2. **Start the workflow**: Start opencode and ask the orchestrator to implement the spec, e.g. `Implement docs/specs/customer-addresses.md`.
3. **Follow the phases**: Observe how the orchestrator delegates the phases to the subagents and how they work in their own sessions.
4. **Review the result**: Review what the agents have implemented and how the result differs from the previous exercises.
