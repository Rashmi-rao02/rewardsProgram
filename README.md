# Retail Rewards Program API

A Spring Boot-based RESTful service designed to calculate reward points for retail customers based on their transaction history. This project implements a tiered reward system and provides dynamic reporting based on custom date ranges or recent timeframes up to a maximum of 3 months.
## 🛠 Tech Stack

* **Java 17**: Core programming language.
* **Spring Boot 3.x**: Framework for building the REST API.
* **Spring Data JPA**: For database abstraction and persistence.
* **Maven**: Dependency management and build tool.
* **Lombok**: To reduce boilerplate code (Getters, Setters, Logging).
* **JUnit 5 & MockMvc**: For comprehensive unit, boundary, and web-layer testing.

---

## 🚀 Features

* **Tiered Reward Logic**: Automatically calculates points based on transaction thresholds.
* **Dynamic Timeframe Filtering**: Filter rewards by specific `startDate` and `endDate`.
* **Recent Activity Reporting**: Query rewards from the database using specific date ranges.
* **Robust Error Handling**: Centralized validation for JSON structure, data types, and business logic.
* **H2 Data Persistence**: Transactions are stored in a database, seeded automatically via data.sql.
* **Recent Activity Reporting**: Rolling window summary for the last $N$ months with grand totals.

---

## 🧮 Reward Logic Explained

The system follows a tiered approach per transaction:
* **$0 - $50**: 0 points.
* **$51 - $100**: 1 point for every dollar over $50.
* **Over $100**: 2 points for every dollar over $100, plus 50 points (from the $50-$100 tier).
---

## 📁 Project Structure



```text
src/
├── main/
│   ├── java/com/retailer/reward/
│   │   ├── controller/         # REST Controllers (GET endpoints for DB queries)
│   │   ├── dto/                # Data Transfer Objects (Response wrappers)
│   │   ├── exception/          # @RestControllerAdvice for global error handling
│   │   ├── model/              # JPA Entities (Transaction table mapping)
│   │   ├── repository/         # Spring Data JPA Repository
│   │   └── service/            # Business logic & Database interaction
│   └── resources/
│       ├── schema.sql          # Database table definitions
│       └── data.sql            # Initial data seeding
└── test/
    ├── java/com/retailer/reward/
    │   ├── controller/         # RewardControllerTest.java (Web layer tests)
    │   ├── dto/                # RewardResponseTest.java (Serialization tests)
    │   ├── exception/          # GlobalExceptionHandlerTest.java (Error mapping tests)
    │   ├── repository/         # TransactionRepositoryTest.java (Data access tests) <-- ADDED
    │   └── service/            # RewardServiceTest.java (Business logic & UTC sync tests)
    └── resources/
        └── application-test.properties  # Test-specific config 

```

## 🚥 Getting Started

1.  **Installation:**
    ```bash
    git clone [https://github.com/Rashmi-rao02/rewardsProgram.git](https://github.com/Rashmi-rao02/rewardsProgram.git)
    mvn clean install
    ```
2.  **Running the application:**
    ```bash
    mvn spring-boot:run
    ```
3. **H2 Console:**
   Access the database UI at http://localhost:8080/h2-console (JDBC URL: jdbc:h2:mem:rewarddb).

## 📖 API Documentation:
  1.Calculate Rewards:
    Endpoint: GET /api/reward/calculate

  Query Parameters:

| Parameter | Type      | Required | Description                              |
|-----------|-----------|----------|------------------------------------------|
| startDate | LocalDate | Yes      | Start of calculation window (YYYY-MM-DD) |
| endDate   | LocalDate | Yes      | End of calculation window (YYYY-MM-DD)   |

  2.Recent Rewards Summary:
    Endpoint: GET /api/reward/recent

  Query Parameters:

| Parameter | Type      | Required | Description                              |
|-----------|-----------|----------|------------------------------------------|
| months    | Integer   | No       | Number of months (default:3              |


  Sample Response Body:
```json
{
  "customerRewards": [
    {
      "customerId": 1,
      "monthlyPoints": { "FEBRUARY": 90 },
      "totalPoints": 90
    }
  ],
  "grandTotalPoints": 90,
  "reportStartDate": "2025-11-09",
  "reportEndDate": "2026-02-09"
}
```

## ⚠️ Error Handling & Validation

Standardized JSON error responses:

| Scenario       | HTTP Status     | Message                                     |
|----------------|-----------------|---------------------------------------------|
| Future Dates   | 400 Bad Request | "Future dates not allowed"                  |
| Invalid Dates  | 400 Bad Request | "Start date cannot be after end date"       |
| Missing Params | 400 Bad Request | "The required query parameter...is missing" |
| Type Mismatch  | 400 Bad Request | "Parameter 'months' has an invalid value"   |


## 🧪 Testing

Automated Tests
```bash
   mvn test
```

Manual cURL Test
```bash
curl "http://localhost:8080/api/reward/calculate?startDate=2025-01-01&endDate=2026-12-31"
```
