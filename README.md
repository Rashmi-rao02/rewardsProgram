Spring Boot-based RESTful service designed to calculate reward points for retail customers based on their monthly transaction history. This project implements a tiered reward system and provides a dynamic, scalable API

## Features:

* Tiered Reward Logic:
  . 2 points for every dollar spent over $100 in each transaction.
  . 1 point for every dollar spent over $50 in each transaction.
* Dynamic Timeframe Filtering: 
  Users can specify custom startDate and endDate via query parameters to generate reports for any period (e.g., 3-month windows, monthly summaries).
* Robust Error Handling:
  Centralized exception handling using @RestControllerAdvice to manage missing parameters, date mismatches, and invalid JSON inputs.
----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
## API Documentation:
  Calculate Rewards
  POST /api/reward/calculate
  Query Parameters: 
  Parameter | Type | Required | Description | | :--- | :--- | :--- | :--- | | startDate | LocalDate | Yes | Start of the calculation window (YYYY-MM-DD) | | 
  endDate | LocalDate | Yes | End of the calculation window (YYYY-MM-DD) |
  Request Body:
  [
    {
      "customerId": 1,
      "amount": 120.0,
      "date": "2023-01-15"
    }
  ]
  Response:
  [
    {
      "customerId": 1,
      "monthlyPoints": {
        "JANUARY": 90
      },
      "totalPoints": 90
    }
  ]
------------------------------------------------------------------------------------------------------------------------------------------------------------------
## Running Tests:
To run the full test suite, execute the following command in the terminal:
mvn test
