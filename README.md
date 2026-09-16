# circuit-breaker-inventory-service

Stock provider for the circuit breaker demo, and the unstable half of it. It answers stock checks from
[`../circuit-breaker-order-service`](../circuit-breaker-order-service) and exposes a switch that makes
every check fail with a 500, so the caller's resilience can be exercised on demand.

**There is no Resilience4j here on purpose.** This service only produces failures; reacting to them is
the order service's job.

## How it works

```
order-service ──GET /v1/inventory/{productId}?quantity=N──► InventoryController
                                                                    │
                                                            InventoryServiceImpl
                                                                    │
                                              faultEnabled? ──yes──► InventoryFaultException ──► 500
                                                    │
                                                    no
                                                    │
                                              InventoryItemRepository (H2, in-memory)
                                                    │
                                                    ▼
                                    { productId, requested, available, inStock }

POST /v1/inventory/toggle-fault ──► FaultServiceImpl ──► flips the AtomicBoolean
```

- **The check never writes.** It does not reserve or decrement stock, so the same request can be
  replayed as many times as an experiment needs.
- **The fault is checked before the database.** It stands for the whole service being down, not for a
  data problem.
- **The flag is process-wide and resets on restart.** It lives in a singleton `AtomicBoolean`, atomic
  because virtual threads read it concurrently.
- **Insufficient stock is not an error.** It answers 200 with `inStock: false`; only a missing product
  (404) or the injected fault (500) produce error responses.

## Stack

Java 21 · Spring Boot 4 · Spring MVC · Spring Data JPA · H2 (in-memory) · Bean Validation · Lombok

## Run

```bash
./mvnw spring-boot:run
```

No Docker needed: the database is in-memory and reseeded on every start. Three items are seeded, the
keyboard with zero stock so the "out of stock" path works without editing any data:

| id | name | quantity |
|---|---|---|
| 1 | Laptop | 10 |
| 2 | Mouse | 50 |
| 3 | Keyboard | 0 |

## Endpoints

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/v1/inventory/{productId}?quantity=N` | Check whether `productId` has at least `N` units | 200 |
| POST | `/v1/inventory/toggle-fault` | Flip the injected fault and return the new state | 200 |

```bash
curl -s 'localhost:8082/v1/inventory/1?quantity=2'
# {"productId":1,"requested":2,"available":10,"inStock":true}

curl -s -X POST localhost:8082/v1/inventory/toggle-fault
# {"faultEnabled":true}

curl -i 'localhost:8082/v1/inventory/1?quantity=2'
# HTTP/1.1 500
```

Error responses:

| Status | When |
|---|---|
| 400 | `quantity` missing, not a number, or not positive |
| 404 | No inventory item with that id |
| 500 | The injected fault is enabled |

## Configuration

| Property | Default | Meaning |
|---|---|---|
| `server.port` | `8082` | HTTP port |
| `spring.datasource.url` | `jdbc:h2:mem:inventory;DB_CLOSE_DELAY=-1` | In-memory database; `DB_CLOSE_DELAY=-1` keeps the schema alive between pooled connections |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema comes from the JPA annotations; there is no Flyway and no `.sql` file |
| `spring.threads.virtual.enabled` | `true` | Virtual threads for the request path |

The H2 web console is not enabled: in Boot 4 it lives in the separate `spring-boot-h2console` module.
Add that dependency and `spring.h2.console.enabled: true` if you want it.

## Tests

```bash
./mvnw clean verify
```

Mockito + AssertJ unit tests for both services, and a `@WebMvcTest` slice covering the 200, 404, 500
and toggle responses.
