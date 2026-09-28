---
name: rest-api-designer
description: Designs and reviews RESTful HTTP APIs, endpoints, JSON payloads, and error responses following our REST API Guidelines. Use whenever creating, designing, or modifying API endpoints, controllers, OpenAPI specs, or DTOs.
---

# REST API Designer Guidelines

Apply these mandatory rules whenever designing or implementing REST endpoints and JSON payloads:

## 1. URL & Path Conventions
- Use **plural nouns** for resource collections (`/customers`, `/addresses`, never `/customer` or `/getAddresses`).
- Use **kebab-case** for multi-word path segments (`/billing-addresses`, never `/billingAddresses` or `/billing_addresses`).
- Keep URL hierarchies shallow (max 2–3 levels, e.g., `/customers/{customer-id}/addresses`).
- Never put verbs in URLs (`/customers/{id}/activate` -> use `PATCH` on `/customers/{id}` or a sub-resource).

## 2. JSON Property & Collection Conventions
- Always use **`camelCase`** for all JSON property names (`postalCode`, `streetName`, `isPrimary`, never `postal_code` or `street_name`).
- Use standard ISO formats: ISO-8601 for dates/timestamps (`2026-11-14T19:00:00Z`) and ISO-3166-1 alpha-2 for country codes (`DE`, `AT`, `CH`).
- Always return a **top-level JSON array** `[...]` directly for collection/list responses (`GET /customers/{customer-id}/addresses` -> `[{ ... }, { ... }]`), never wrap lists in an object like `{ "items": [...] }`.

## 3. HTTP Methods, Content-Types & Status Codes
- `GET` -> `200 OK` (return `[]` with `200 OK` for empty collections, never `404`).
- `POST` -> `201 Created` with `Location` header pointing to the new resource.
- `PUT` -> Full replacement (`200 OK` or `204 No Content`).
- `PATCH` -> Partial update (`200 OK`). **Mandatory Guardrail:** `PATCH` endpoints MUST strictly restrict `consumes` to `application/merge-patch+json` (RFC 7396) or `application/json-patch+json` (RFC 6902). Never accept generic `application/json` on a `PATCH` endpoint (return `415 Unsupported Media Type` otherwise).
- `DELETE` -> `204 No Content` (idempotent).

## 4. Standardized Error Handling (RFC 9457 / Problem Details)
- All `4xx` and `5xx` error responses MUST return `application/problem+json` following RFC 9457 (`type`, `title`, `status`, `detail`, `instance`).
