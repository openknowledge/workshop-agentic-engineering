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

## Playwright MCP

`opencode.json` starts the Playwright MCP server via a globally installed `npx`.

### Troubleshooting - Node/npx Not Installed

If Node/npx is not installed globally, Node/npx can be installed locally.

#### Windows

Node/npx can be installed locally with the following command:

```bash
./mvnw.cmd -pl customer-management-client initialize
```

Afterwards, `opencode.json` can be adjusted as follows:

```json
"command": [
  "cmd",
  "/c",
  "set \"PATH=%CD%\\customer-management-client\\node;%PATH%\" && node customer-management-client\\node\\node_modules\\npm\\bin\\npx-cli.js -y @playwright/mcp@latest"
]
```

#### Linux / macOS

Node/npx can be installed locally with the following command:

```bash
./mvnw -pl customer-management-client initialize
```

Afterwards, `opencode.json` can be adjusted as follows:

```json
"command": [
  "sh",
  "-c",
  "N=\"$PWD/customer-management-client/node\"; PATH=\"$N:$PATH\" exec node \"$N/node_modules/npm/bin/npx-cli.js\" -y @playwright/mcp@latest"
]
```
