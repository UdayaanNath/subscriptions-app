# Subscriptions app

Subscription membership service with **Monthly / Quarterly / Yearly plans**, **Free / Silver / Gold / Premium tiers**,
configurable **benefits**, and **criteria-driven tier changes** driven by delivered orders and user cohort.

See [DESIGN.md](DESIGN.md) for the full design (architecture, entities, tier rules, API).

## Run

Requires Docker and Docker Compose.

```bash
docker compose up --build
```

The app waits until Postgres is healthy, then Flyway creates the schema and loads sample data.
Database files are not kept: `docker compose down` drops them, and the next `up` migrates again.

| URL | What |
|---|---|
| http://localhost:8080/api/v1 | HTTP API |
| `localhost:5432` | Postgres `subscription_db`, user `admin`, password `admin123` |

Stop:

```bash
docker compose down
```

The API does not require authentication.

## Tests

```bash
./mvnw test
```

## 5-minute demo

Seeded users: **1 Alice** (`STANDARD`), **2 Bob** (`EARLY_ADOPTER`), **3 Carol** (`EMPLOYEE`), **4 Dave** (`DEACTIVATED`), **5 Eve** (`EARLY_ADOPTER`).
`./demo.sh` runs the scripted checks below and prints `PASS` / `FAIL`. Run it against a freshly started stack.

```bash
chmod +x demo.sh
./demo.sh
```

The same flows by hand:

```bash
BASE=http://localhost:8080/api/v1

# 1. Plans on offer (one row per tier × plan)
curl -s "$BASE/subscriptions?size=20" | jq

# 2. Alice is on Free Yearly and is not eligible for Silver
curl -s -X PATCH $BASE/memberships/1/upgrade | jq

# 3. Bob no longer meets Silver rules, so he moves down one tier
curl -s -X PATCH $BASE/memberships/2/downGrade | jq

# 4. Create an order for Bob, then refuse an order update for deactivated Dave
curl -s -X POST $BASE/orders -H 'Content-Type: application/json' \
     -d '{"userId":2,"amount":640}' | jq
curl -s -X PATCH $BASE/orders/9 -H 'Content-Type: application/json' \
     -d '{"orderStatus":"DELIVERED"}' | jq

# 5. Attach free delivery to Alice's plan and read the benefits she can use
curl -s -X POST $BASE/benefits -H 'Content-Type: application/json' \
     -d '{"subscriptionId":3,"benefitType":"FREE_DELIVERY","name":"Free delivery"}' | jq
curl -s "$BASE/benefits?userId=1" | jq
```

## Configuration (`application.yaml`)

```yaml
server:
  tomcat:
    threads:
      min-spare: 5
      max: 20

spring:
  datasource:
    hikari:
      maximum-pool-size: 5

app:
  subscription:
    default-free-tier-days: 36500
    tier-order:
      - FREE
      - SILVER
      - GOLD
      - PREMIUM
    tier-rules:
      SILVER:
        min-order-count: 5          # delivered orders must be greater than this
        min-monthly-order-value: 1000
        cohorts: [STANDARD, EARLY_ADOPTER, EMPLOYEE]
```

Containers are limited to 0.5 CPU and 512 MB. The JVM heap is `-Xms256m -Xmx256m` with `-Xss512k`.

## Project layout

```
org.demo.com.subscriptionsapp
├── api
│   ├── controller     User, Subscription, Order, Membership, Benefit
│   ├── dto            request, response, and search criteria per resource
│   └── error          NotFoundException (404), BadRequestException (400)
├── config             SubscriptionConfig, SecurityConfig
├── domain
│   ├── entity         JPA entities, BaseEntity (@Version, timestamps)
│   ├── enums          tiers, plans, statuses, cohorts, benefit types
│   ├── converter      entity ↔ API DTO
│   └── specification  UpgradeRule strategies (order count, monthly value, cohort)
├── repository         Spring Data JPA
├── service            use-cases
└── util               DateUtils
```
