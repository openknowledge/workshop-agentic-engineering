# Migration Patterns

## File Naming Convention

```
V<major>.<minor>.<patch>__<description-in-kebab-case>.sql
```

Examples:
- `V1.0.0__create-counter.sql` -- Initial table creation
- `V1.1.0__add-user-table.sql` -- New table in minor version
- `V1.1.1__add-index-on-username.sql` -- Small change in patch version

Place migrations in: `basisprojekt-server/src/main/resources/db/migration/`

## Table Creation Template

```sql
-- 1. Create sequence for the primary key
CREATE SEQUENCE seq_<column>_id
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 2. Create table with prefixed names
CREATE TABLE tab_<entity> (
    c_id BIGINT NOT NULL,
    c_<field1> VARCHAR(255) NOT NULL,
    c_<field2> INTEGER NOT NULL DEFAULT 0,
    CONSTRAINT pk_c_id PRIMARY KEY (c_id),
    CONSTRAINT uq_c_<field> UNIQUE (c_<field>)
);
```

## Naming Conventions Summary

| Element | Pattern | Example |
|---------|---------|---------|
| Table | `TAB_<ENTITY>` | `TAB_COUNTER` |
| Column | `C_<FIELD>` | `C_NAME`, `C_VALUE` |
| Primary Key ID | `C_ID` | `C_ID` |
| Sequence | `SEQ_<COLUMN>` | `SEQ_C_ID` |
| PK Constraint | `pk_<column>` | `pk_c_id` |
| Unique Constraint | `uq_<column>` | `uq_c_name` |
| Foreign Key | `fk_<table>_<column>` | `fk_order_c_user_id` |
| Index | `idx_<table>_<column>` | `idx_counter_c_name` |

## JPA Entity Mapping for New Table

```java
@Entity(name = "EntityName")
@Table(name = "TAB_ENTITY_NAME")
@NamedQuery(
        name = EntityNameEntity.BY_FIELD,
        query = "SELECT e FROM EntityName e WHERE e.aggregate.field.field = :param")
public class EntityNameEntity {

    public static final String BY_FIELD = "EntityNameEntity.byField";

    @Id
    @Column(name = "C_ID")
    @GeneratedValue(strategy = SEQUENCE, generator = "SEQ_ENTITY")
    @SequenceGenerator(name = "SEQ_ENTITY", sequenceName = "SEQ_C_ID", allocationSize = 1)
    private Long id;

    @Embedded
    @AttributeOverride(name = "field1.field1", column = @Column(name = "C_FIELD1"))
    @AttributeOverride(name = "field2.field2", column = @Column(name = "C_FIELD2"))
    private AggregateRecord aggregate;
}
```

## Adding a Foreign Key Column

```sql
-- Add FK column to existing table
ALTER TABLE tab_order
    ADD COLUMN c_user_id BIGINT NOT NULL;

ALTER TABLE tab_order
    ADD CONSTRAINT fk_order_c_user_id
    FOREIGN KEY (c_user_id) REFERENCES tab_user(c_id);

-- Add index for FK (recommended for join performance)
CREATE INDEX idx_order_c_user_id ON tab_order(c_user_id);
```

## Test Data

Test data goes in `basisprojekt-server/src/test/resources/db/testdata.sql`.
This file is used by docker-compose for integration test environments.

```sql
-- Example: Update seed data for testing
UPDATE tab_counter SET c_value = 42 WHERE c_name = 'example';
```

## Critical Rules

- NEVER modify an existing migration file that has been applied
- ALWAYS create a new migration with the next version number
- ALWAYS use parameterized queries in JPQL (`:paramName`, never string concatenation)
- ALWAYS set `allocationSize = 1` on `@SequenceGenerator` to match PostgreSQL sequence defaults
