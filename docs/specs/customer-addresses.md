# Customer Addresses

## Problem Statement

Today, clerks can only record a customer's name, email, phone number and date of birth. There is no way to store postal addresses. This removes the basis for sending invoices or delivering goods: it is unknown where an invoice should go and to which locations deliveries are allowed. In addition, the customer list does not show where a customer is located.

## Solution

Every customer gets a list of addresses. An address consists of street, house number, optional address addition, zip code, city and country, and can optionally be marked as a billing address (`BILLING`) or delivery address (`DELIVERY`). A customer should have exactly one billing address and at least one delivery address; in addition, any number of addresses without a type are allowed.

Addresses are exposed as a sub-resource of the customer (`/api/customers/{customer-id}/addresses`). Each address has its own ID and can be created, read, replaced and deleted individually. The customer resource itself remains unchanged and does not contain addresses.

The server guarantees that a customer never has more than one billing address: marking an address as billing address automatically removes the type from the previous billing address. In addition, the server rejects removing or retyping the last billing address or the last delivery address. The minimum rules ("exactly one billing address", "at least one delivery address") for new customers and existing customers without addresses are enforced by the client.

Addresses are maintained (add, change, remove) directly in the existing customer form in a dedicated "Addresses" section. Input is validated immediately in the form, including country-dependent zip code validation for Germany, Austria and Switzerland, and validated again by the server. When saving, the client first saves the customer and then sends the address changes to the sub-resource. The customer list shows each customer's billing address in a separate column.

Existing customers without addresses remain readable and deletable; the required addresses only have to be added the next time they are saved.

## User Stories

1. As a clerk, I want to assign multiple addresses to a customer, so that I have all relevant addresses of a customer in one place.
2. As a clerk, I want to mark an address as billing address, so that it is clear where invoices are sent.
3. As a clerk, I want to mark one or more addresses as delivery address, so that deliveries to different locations are possible.
4. As a clerk, I want to be able to record addresses without a type, so that I can keep track of other known addresses.
5. As a clerk, I want a customer not to be saved without a billing address, so that invoicing is always possible.
6. As a clerk, I want a customer not to be saved with two billing addresses, so that it is unambiguous which billing address applies.
7. As a clerk, I want a customer not to be saved without a delivery address, so that deliveries can always be made.
8. As a clerk, I want street, house number, zip code, city and country to be required, so that every address is postally complete.
9. As a clerk, I want to be able to enter an optional address addition (e.g. "c/o", "rear building", "3rd floor"), so that shipments arrive even in complicated delivery situations.
10. As a clerk, I want to choose the country from a selection list with English country names, so that I don't need to know country codes and no invalid countries are recorded.
11. As a clerk, I want Germany, Austria and Switzerland at the top of the country selection, so that I can quickly find the most common countries.
12. As a clerk, I want Germany to be preselected for a new address, so that normally I don't have to change anything.
13. As a clerk, I want a German zip code to require exactly five digits, so that typos stand out immediately.
14. As a clerk, I want an Austrian or Swiss zip code to require exactly four digits, so that typos stand out immediately.
15. As a clerk, I want to be able to enter foreign zip codes in their respective format (e.g. "SW1A 1AA"), so that international customers can be maintained as well.
16. As a clerk, I want a house number to have to start with a digit (e.g. "12", "12a", "12-14"), so that swapped input is detected.
17. As a clerk, I want the city not to contain digits, so that zip code and city are not accidentally mixed up.
18. As a clerk, I want to see error messages directly at the affected address field, so that I know what to correct.
19. As a clerk, I want to see error messages about the billing/delivery address rule above the address section, so that I can tell which address is missing or duplicated.
20. As a clerk, I want to see all error messages in English, so that they match the rest of the UI.
21. As a clerk, I want to find an empty block for the billing address and one for the delivery address when creating a customer, so that I can fill in the normal case right away.
22. As a clerk, I want to add further addresses via "Add address", so that I can record additional delivery addresses.
23. As a clerk, I want a newly added address to be preset as a delivery address, so that I don't have to change the most common case.
24. As a clerk, I want to delete any address via "Remove", so that outdated addresses disappear.
25. As a clerk, I want to see an error message after removing a required address instead of a disabled button, so that I can freely rearrange and correct.
26. As a clerk, I want to change a customer's existing addresses, so that moves can be maintained.
27. As a clerk, I want to change the type of an existing address, so that I can e.g. turn a delivery address into the billing address.
28. As a clerk, I want the order of my addresses to be preserved, so that the display looks the same after saving.
29. As a clerk, I want to see a "Billing address" column in the customer list (e.g. "Sample Street 12a, 26122 Oldenburg"), so that I can quickly identify customers.
30. As a clerk, I want to sort the customer list by billing address, so that I can group customers regionally.
31. As a clerk, I want to search the customer list by parts of the billing address, so that I can find customers by their city.
32. As a clerk, I want to see a "–" in the billing address column for existing customers without an address, so that I can recognize incomplete customers.
33. As a clerk, I want to still be able to open and view existing customers without addresses, so that the introduction of addresses does not disrupt operations.
34. As a clerk, I want to be able to delete existing customers without addresses, so that legacy data can be cleaned up.
35. As a clerk, I want to be notified when editing an existing customer that the billing and delivery address are missing, so that I complete the data the next time I save.
36. As a clerk, I want all of a customer's addresses to be removed when the customer is deleted, so that no orphaned data remains.
37. As a clerk, I want to see an error message if saving the addresses fails after the customer has been saved, so that I know the addresses have to be checked.
38. As an API user, I want to manage addresses via the sub-resource `/api/customers/{customer-id}/addresses`, so that I can create, change and delete individual addresses without sending the whole customer.
39. As an API user, I want to retrieve all addresses of a customer with a single request, so that I don't need one request per address.
40. As an API user, I want each address in the address list to contain a `self` link, so that I can address it directly.
41. As an API user, I want to receive a `Location` header with the URI of the new address when creating an address, so that I can reference it afterwards.
42. As an API user, I want the customer endpoints to remain unchanged, so that existing clients continue to work.
43. As an API user, I want marking an address as billing address to automatically remove the type from the previous billing address, so that I can switch the billing address with a single request.
44. As an API user, I want removing or retyping the last billing address or the last delivery address to be rejected with `409 Conflict`, so that a customer cannot lose its required addresses by accident.
45. As an API user, I want to receive `404 Not Found` for addresses of unknown customers or for addresses that belong to another customer, so that addresses cannot be accessed via the wrong customer.
46. As an API user, I want to receive precise paths such as `zipCode` in `invalid-params` on validation errors, so that I can map errors programmatically.
47. As an API user, I want to receive a separate error entry for each rule violated at the same time, so that I can fix all problems in one pass.
48. As an API user, I want to receive error messages in English (default) or German depending on the language, so that they fit my client.
49. As an API user, I want invalid country codes to be rejected with a clear message, so that no faulty countries are stored.
50. As an API user, I want an unknown address type to be rejected, so that only `BILLING`, `DELIVERY` or no type is stored.
51. As an API user, I want values that are too long to be rejected with a validation message instead of a database error, so that errors remain understandable.
52. As an API user, I want the address endpoints and field lengths to be documented in the OpenAPI schema, so that I can build my clients correctly.
53. As a developer, I want entity, validation and database lengths to match, so that the architecture test stays green and no inconsistencies arise.

## Implementation Decisions

### Domain model (server)

- The customer record and the customer entity remain unchanged; they do not get an `addresses` field.
- New entity for addresses with its own technical ID (sequence, as for customers) and a reference to the customer. Addresses are always loaded and changed via their customer; there is no global address access.
- `Address` is an embeddable record (address data without ID) with the following fields, each as its own value object record in the project style (`@JsonValue`, Bean Validation, `public static final int MAX_LENGTH`):

| Field (JSON) | Value object | Required | Max. length | Rule | Message (en) |
|---|---|---|---|---|---|
| `type` | `AddressType` | no | 32 | `@OneOf({"BILLING","DELIVERY"})` | existing `OneOf.message` |
| `street` | `Street` | yes | 255 | `@NotBlank` | default |
| `houseNumber` | `HouseNumber` | yes | 16 | `@NotBlank`, pattern `^\d+.*$` | "must start with a digit" |
| `addressAddition` | `AddressAddition` | no | 255 | – | – |
| `zipCode` | `ZipCode` | yes | 10 | `@NotBlank`; country-dependent format (see below) | "is not a valid zip code for the given country" |
| `city` | `City` | yes | 255 | `@NotBlank`, pattern `\D*` | "must not contain digits" |
| `country` | `CountryCode` | yes | 2 | new constraint `@CountryCode` | "is not a valid ISO 3166 country code" |

- The address type follows the pattern of `ContactType` (string value object with `@OneOf`) and is optional.
- The order of a customer's addresses is the creation order (ascending address ID).

### Validation

- **`@CountryCode`**: new constraint in the validation infrastructure package; valid values are exactly the ISO 3166 alpha-2 codes of the JDK (`Locale.getISOCountries()`). No additional library.
- **Country-dependent zip code validation**: class-level constraint on `Address`.
  - `DE`: exactly 5 digits
  - `AT`, `CH`: exactly 4 digits
  - all other countries: `^[A-Za-z0-9 -]{1,10}$`
  - The violation is reported on `zipCode` via `addPropertyNode` (path `zipCode`).
- Validation of a single address applies via `@Valid` on `POST` and `PUT` of the address endpoints. Errors are delivered via the existing ValidationExceptionHandler as `400 Bad Request` with `invalid-params` (`name`/`reason`) – no new error format.
- **List rules on the server** (enforced in the domain service when an address is created, replaced or deleted):
  - **At most one billing address**: If an address is created or replaced with type `BILLING`, the type of the previous billing address of the same customer is removed (it becomes an address without type). This is not an error.
  - **Last billing address**: Replacing the current billing address with a different type or deleting it is rejected with `409 Conflict`, "the last billing address cannot be removed; mark another address as billing address instead".
  - **Last delivery address**: Replacing the only remaining delivery address with a different type (including `BILLING`) or deleting it is rejected with `409 Conflict`, "the last delivery address cannot be removed; add another delivery address first".
  - These rules only protect existing billing/delivery addresses. They do not require a customer to have addresses; customers without addresses (new or existing) remain valid on the server.
  - `409` responses are `application/problem+json` with the localized message in `detail`.
- The minimum rules ("exactly one billing address", "at least one delivery address") are enforced by the client only.
- No validation takes place on read and delete of customers; existing customers without addresses therefore remain functional.
- All new messages are maintained in the existing ValidationMessages bundles (default, `_de`, `_en`): English texts in the default and `_en` bundles, German translations in `_de`. English is the server's default locale; German is returned for `Accept-Language: de`.

### Database

- New Flyway migration V1.3.0 creates `tab_customer_address` and the sequence `seq_customer_address_c_id`:
  - `c_id` (PK, from the sequence), `c_customer_id` (FK to `tab_customer`, `ON DELETE CASCADE`)
  - `c_type VARCHAR(32)` nullable, `c_street VARCHAR(255)`, `c_house_number VARCHAR(16)`, `c_address_addition VARCHAR(255)` nullable, `c_zip_code VARCHAR(10)`, `c_city VARCHAR(255)`, `c_country VARCHAR(2)`
  - naming conventions `pk_`/`fk_` as in V1.2.0
- No minimum-count or uniqueness constraints in the database; no data migration for existing customers.
- Column lengths match `MAX_LENGTH`/`@Size` exactly, so that `OpenApiConstraintArchitectureTest` stays green.
- The test data gets one billing and one delivery address for each of the two test customers (Max Mustermann, Erika Musterfrau).

### API contract

- The customer endpoints (`/api/customers`, `/api/customers/{id}`) and the customer JSON remain unchanged.
- New endpoints (sub-resource of the customer):

| Method | Path | Request | Response |
|---|---|---|---|
| `GET` | `/api/customers/{customer-id}/addresses` | – | `200 OK`, top-level JSON array of addresses with `self` link; `[]` if the customer has no addresses |
| `POST` | `/api/customers/{customer-id}/addresses` | address | `201 Created`, `Location: /api/customers/{customer-id}/addresses/{address-id}`, no body |
| `GET` | `/api/customers/{customer-id}/addresses/{address-id}` | – | `200 OK`, address |
| `PUT` | `/api/customers/{customer-id}/addresses/{address-id}` | address | `204 No Content` (full replacement) |
| `DELETE` | `/api/customers/{customer-id}/addresses/{address-id}` | – | `204 No Content` |

- Status codes for errors (all `application/problem+json`):
  - `400 Bad Request`: validation errors (`invalid-params`), invalid IDs
  - `404 Not Found`: unknown customer, unknown address, or address belonging to another customer
  - `409 Conflict`: removing or retyping the last billing or delivery address
- Address IDs are server-generated; an `id` in the request body is not part of the schema.
- Request body for `POST` and `PUT`, and response body for `GET` of a single address:

```json
{
  "type": "BILLING",
  "street": "Sample Street",
  "houseNumber": "12a",
  "addressAddition": "c/o Company XY",
  "zipCode": "26122",
  "city": "Oldenburg",
  "country": "DE"
}
```

- Response body for `GET` of the address list (analogous to the customer overview with `self`):

```json
[
  {
    "self": { "href": "/api/customers/1/addresses/7" },
    "type": "BILLING",
    "street": "Sample Street",
    "houseNumber": "12a",
    "addressAddition": "c/o Company XY",
    "zipCode": "26122",
    "city": "Oldenburg",
    "country": "DE"
  }
]
```

### Client

- **Domain schema**: New Zod address schema that mirrors all server rules for a single address:
  - required fields, maximum lengths, house number pattern, city without digits
  - country-dependent zip code via `superRefine` at address level (error on field `zipCode`)
  - type: `BILLING`, `DELIVERY` or empty
  - The country code is not validated separately against a list; the selection list only allows valid codes.
  - English error messages analogous to the existing ones.
- **Form schema**: The form combines customer and address list; "exactly one billing address" / "at least one delivery address" are checked via `superRefine` on the list (error on `addresses`). The customer schema itself does not get addresses.
- **Types**: The `Address` type (without ID) and the `AddressOverview` type (with `self`) are derived from the schemas.
- **API functions**: new functions for the address endpoints (list, create, replace, delete). The existing customer API functions remain unchanged. Creating a customer additionally reads the `Location` header to get the URI of the new customer.
- **Loading**: The edit page loads the customer and its addresses in parallel. The customer list loads the addresses of each customer separately (via the `self` link of the customer overview plus `/addresses`) to display the billing address column.
- **Saving** (not atomic):
  - New customer: `POST` the customer, then `POST` each address in form order.
  - Existing customer: `PUT` the customer, then compare the form addresses with the loaded addresses and send the changes in this order:
    1. `POST` new addresses (in form order)
    2. `PUT` changed addresses whose new type is `BILLING`
    3. `PUT` all other changed addresses
    4. `DELETE` removed addresses
  - This order avoids `409` responses in the common cases (replacing the billing address, replacing a delivery address, switching the billing address to another address).
  - If an address request fails, the remaining requests are not sent; the form stays open, reloads the addresses and shows a generic error ("Addresses could not be saved").
- **Customer form**: new "Addresses" section below the existing fields.
  - Array field (TanStack Form `mode="array"`), one block per address with the fields address type, street, house number, address addition, zip code, city, country. Each block remembers the address ID (if already saved) to determine the changes when saving.
  - Address type selection: "Billing address", "Delivery address", "No type".
  - Country selection: all ISO countries with English names via `Intl.DisplayNames(['en'], { type: 'region' })`; `DE`, `AT`, `CH` at the top, the rest alphabetically by English name. Preselected `DE`.
  - The "Add address" button appends a block with type `DELIVERY`, country `DE` and otherwise empty fields.
  - "Remove" button per block, always enabled; missing required addresses are displayed as errors.
  - List errors are displayed above the address section, field errors at the respective field.
  - When creating a new customer, two blocks are prefilled: one with `BILLING`, one with `DELIVERY`.
  - When editing, the loaded addresses are adopted; existing customers without addresses start with an empty list and immediately see the list errors.
- **Customer table**: new "Billing address" column in the format "Street HouseNumber, ZipCode City", sortable and searchable; "–" if no billing address exists or the addresses have not been loaded (yet).
- Server errors continue to be displayed generically.

## Testing Decisions

### What makes a good test

- Tests exclusively check externally observable behavior via the highest available interface: HTTP responses of the REST API, the public Zod schemas, rendered components from the user's perspective, or the running application in the browser.
- No tests for implementation details (individual validator classes, internal helper functions, entity mapping). Validators and list rules are tested via the API.
- Error cases check exact status codes, `name` paths and English `reason`/`detail` texts (default locale).

### Test seams (all already exist, no new ones)

1. **REST API `/api/customers` and `/api/customers/{customer-id}/addresses`** via the existing integration test setup (Spring Boot with random port, Postgres via Testcontainers, rollback after each test). Covers mapping, migration and validation together.
2. **Architecture test** for consistency of entity column lengths, `@Size` and Flyway migrations.
3. **Client domain and components** via Vitest and Testing Library: public schemas, rendered customer form, rendered customer table.
4. **Playwright E2E** against the running application.

### Server (prior art: existing `CustomerControllerTest` with JSON text blocks, Given/When/Then, AssertJ on map bodies; e.g. `multipleAdditionalEmails`, `createCustomerWithInvalidAdditionalParametersFails`)

- New test class for the address endpoints.
- Create an address: `201` with `Location`; retrieve it individually and in the list; optional fields (`addressAddition`, `type`) are preserved.
- List returns `[]` for a customer without addresses and preserves the creation order.
- Replace (`PUT`) and delete (`DELETE`) an address.
- Creating or replacing an address with type `BILLING` removes the type from the previous billing address.
- `409 Conflict` for: deleting the billing address; changing the type of the billing address; deleting the last delivery address; changing the type of the last delivery address (including to `BILLING`).
- `404 Not Found` for: unknown customer; unknown address; address of another customer.
- Validation error cases with exact `name`/`reason`:
  - wrong zip code for `DE`, `AT`, `CH`
  - valid foreign zip code is accepted (e.g. `GB` "SW1A 1AA")
  - invalid country code
  - house number without leading digit
  - city with digit
  - fields that are too long
  - unknown address type
  - missing required fields
- Customer JSON and customer overview contain no addresses; existing customer tests remain unchanged.
- Existing customer without addresses is readable and deletable.
- Deleting a customer removes its addresses (no FK error, address list afterwards returns `404`).
- `OpenApiConstraintArchitectureTest` stays green unchanged.

### Client (prior art: existing form and FormField tests with Vitest globals, `userEvent`, label-based queries)

- Schema tests (new test file for the address and form schemas): all field rules, country-dependent zip code, list rules, optional fields.
- Form tests: initial values on creation (two blocks), "Add address" (type delivery address, country DE), "Remove", display of field and list errors, country selection with DACH on top, passing the addresses (including IDs of existing addresses) to `onSubmit`.
- Save logic tests: the resulting requests and their order for new customers and for added, changed, retyped and removed addresses; error handling when an address request fails.
- Table tests: "Billing address" column including formatting, "–" for a missing address, sorting and search.
- The coverage threshold of 95% is maintained.

### E2E (prior art: existing Playwright suite with its own fixture, role-/label-based locators)

- Create a customer with billing and delivery address and find it again in the list.
- Change the address of an existing customer.
- Switch the billing address to another existing address.
- Delete the address of an existing customer.
- Existing E2E tests for creating and editing are extended with required addresses.

## Out of Scope

- Address roles that allow an address to be billing and delivery address at the same time (the same address has to be entered twice).
- Further address types (e.g. private/business).
- Embedding addresses in the customer JSON or the customer overview.
- Atomic saving of customer and addresses (transactions across multiple requests, batch endpoints).
- `PATCH` for addresses.
- Swapping the types of the only delivery address and the billing address in one save (the server rejects it with `409`; workaround: add another delivery address first).
- Placeholder or data migration for existing customers.
- Server-side enforcement of the minimum rules for new customers and existing customers without addresses.
- Database constraints for minimum count or uniqueness of address types.
- Zip code format validation for countries outside DE/AT/CH.
- Address validation against external services (existence check, geocoding, autocomplete).
- Evaluating the server's `invalid-params` in the client and displaying them at the form field.
- Upper limit for the number of addresses.
- Reordering addresses.
- Dedicated subpages for address management.
- Adding `additionalEmails`/`additionalPhoneNumbers` to the client.

## Further Notes

- Since `PUT` from the client sends the complete customer object and the client does not know `additionalEmails`/`additionalPhoneNumbers`, these continue not to be sent when saving. This existing behavior is not changed by the feature, but should be reviewed separately.
- Saving is not atomic: if an address request fails, the customer and possibly some addresses are already saved. The client reloads the addresses and shows an error, so the clerk can correct the state.
- The customer list loads the addresses with one request per customer (N+1). This is acceptable for the current data volume; if it becomes a problem, a read-only `billingAddress` in the customer overview or a filter on the address endpoint can be added later.
- The customer endpoints remain backward compatible: API users can still create and change customers without addresses. The minimum rules are only enforced by the client and should be communicated to external API users.
- User-visible texts, identifiers, test names and commit messages are English – as in the rest of the project. The server's validation messages are additionally available in German via `Accept-Language: de`.
