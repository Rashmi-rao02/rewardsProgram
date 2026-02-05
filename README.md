# Retail Rewards Program API

A Spring Boot-based RESTful service designed to calculate reward points for retail customers based on their transaction history. This project implements a tiered reward system and provides dynamic reporting based on custom or recent timeframes.

## 🛠 Tech Stack

* **Java 17**: Core programming language.
* **Spring Boot 3.x**: Framework for building the REST API.
* **Maven**: Dependency management and build tool.
* **Lombok**: To reduce boilerplate code (Getters, Setters, Logging).
* **JUnit 5 & AssertJ**: For robust unit and boundary analysis testing.

---

## 🚀 Features

* **Tiered Reward Logic**: Automatically calculates points based on transaction thresholds.
* **Dynamic Timeframe Filtering**: Filter rewards by specific `startDate` and `endDate`.
* **Recent Activity Reporting**: Quick-access API to view rewards for the last $N$ months.
* **Robust Error Handling**: Centralized validation for JSON structure, data types, and business logic.

---

## 🧮 Reward Logic Explained

The system follows a tiered approach per transaction:
* **$0 - $50**: 0 points.
* **$51 - $100**: 1 point for every dollar over $50.
* **Over $100**: 2 points for every dollar over $100, plus 50 points (from the $50-$100 tier).
---

## 📁 Project Structure



```text
src/main/java/com/retailer/reward/
├── controller/        # REST Controllers (API Endpoints)
├── dto/               # Data Transfer Objects (Request/Response)
├── exception/         # Global Exception Handler & Error Details
├── model/             # Domain Entities (Transaction)
└── service/           # Business Logic (Reward Calculation)

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

## 📖 API Documentation:
  1.Calculate Rewards:
    Endpoint: POST /api/reward/calculate

  Query Parameters:

| Parameter | Type      | Required | Description                              |
|-----------|-----------|----------|------------------------------------------|
| startDate | LocalDate | Yes      | Start of calculation window (YYYY-MM-DD) |
| endDate   | LocalDate | Yes      | End of calculation window (YYYY-MM-DD)   |

  2.Recent Rewards:
    Endpoint: POST /api/reward/recent

  Query Parameters:

| Parameter | Type      | Required | Description                              |
|-----------|-----------|----------|------------------------------------------|
| months    | Integer   | No       | Number of months (default:3              |

 Sample Request Body:
```json
 [
 {
 "customerId": 1,
 "amount": 120.0,
 "date": "2025-01-15"
 }
 ]
```
  Sample Response Body:
```json
[
  {
    "customerId": 1,
    "monthlyPoints": { "JANUARY": 90 },
    "totalPoints": 90
  }
]
```

## ⚠️ Error Handling & Validation

Standardized JSON error responses:

| Scenario       | HTTP Status     | Message                                      |
|----------------|-----------------|----------------------------------------------|
| Malformed JSON | 400 Bad Request | "Malformed JSON input: Check data types..."  |
| Invalid Dates  | 400 Bad Request | "Start date cannot be after end date"        |
| Missing Params | 400 Bad Request | "The required query parameter...is missing"  |


## 🧪 Testing

Automated Tests
```bash
   mvn test
```

Manual cURL Test
```bash
curl --location --request POST 'http://localhost:8080/api/reward/calculate?startDate=2025-01-01&endDate=2025-04-30' \
--header 'Content-Type: application/json' \
--data-raw '[{"customerId": 1, "amount": 120.0, "date": "2025-01-15"}]'
```
