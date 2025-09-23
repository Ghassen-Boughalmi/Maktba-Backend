# Maktba-Backend

🛒 **Maktba-Backend**: A Spring Boot backend for a mobile app managing stationery stores (books, paper, pens). Built with robust security (JWT-based authentication), real-time inventory management, and high performance. Integrates **Cloudinary** for image storage, **Twilio** for SMS-based account verification and password reset (implemented and working), and **PostgreSQL** for reliable data persistence. Ideal for stationery retail businesses. 📚✏️ #SpringBoot #Stationery #Cloudinary #Twilio #PostgreSQL

## Table of Contents

* [Getting Started](#getting-started)

  * [Prerequisites](#prerequisites)
  * [Installation](#installation)
* [Features](#features)
* [Configuration Notes](#configuration-notes)
* [Contributing](#contributing)

## Getting Started

### Prerequisites

Before you begin, ensure you have the following requirements:

* **Java Development Kit (JDK)**: Version 21 (as specified in `build.gradle`).
* **Gradle**: Version 8.0 or higher for building the project.
* **PostgreSQL**: Version 14 or higher, with a running database instance.
* **Cloudinary Account**: For image uploads (requires `cloud_name`, `api_key`, and `api_secret`) — integrated and working for product image uploads.
* **Twilio Account**: For SMS-based account verification and password reset (requires `account_sid`, `auth_token`, and `trial_number`) — integrated and working.
* An IDE with Spring Boot support (e.g., IntelliJ IDEA, Eclipse, or VS Code).
* **Postman**: For testing API endpoints.

### Installation

1. **Clone the repository**:

   ```bash
   git clone https://github.com/Ghassen-Boughalmi/Maktba-Backend.git
   ```

2. **Navigate to the project directory**:

   ```bash
   cd maktba-backend
   ```

3. **Configure application properties**:

   * Update `src/main/resources/application.properties` with your PostgreSQL credentials and Cloudinary and Twilio credentials:

     ```properties
     spring.application.name=maktba
     spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
     spring.datasource.username=postgres
     spring.datasource.password=root
     spring.datasource.driver-class-name=org.postgresql.Driver
     spring.jpa.hibernate.ddl-auto=create-drop
     spring.security.user.name=admin
     spring.security.user.password=admin123

     # Twilio (configured and active)
     twilio.account-sid=YOUR_TWILIO_ACCOUNT_SID
     twilio.auth-token=YOUR_TWILIO_AUTH_TOKEN
     twilio.trial-number=+1234567890

     # Cloudinary (configured and active)
     cloudinary.cloud-name=YOUR_CLOUDINARY_CLOUD_NAME
     cloudinary.api-key=YOUR_CLOUDINARY_API_KEY
     cloudinary.api-secret=YOUR_CLOUDINARY_API_SECRET
     ```

4. **Build the project using Gradle**:

   ```bash
   ./gradlew clean build
   ```

5. **Run the application**:

   ```bash
   ./gradlew bootRun
   ```

   The application will start at `http://localhost:8080`.

## Features

* **User Authentication**: Secure JWT-based authentication for user registration and login.
* **SMS Verification**: Twilio integration for account verification and password reset via SMS (active and tested).
* **Product Management**: CRUD operations for managing stationery products (books, pens, paper) with image uploads via Cloudinary (active integration).
* **Category Management**: Organize products into categories (e.g., Books, Stationery) with CRUD operations.
* **Shopping Cart**: Each user has a personal cart to add, remove, or reset items. Users can create and modify carts, with confirmed carts visible to admins in a dedicated list. Modifications to confirmed carts update the admin list.
* **Admin Dashboard**: Admins can view and manage confirmed carts, with real-time updates for modified carts.
* **Real-Time Inventory**: Track product quantities in real-time using PostgreSQL.
* **Validation and Error Handling**: Robust input validation for product, category, and cart data to ensure data integrity.
* **Scalable Architecture**: Built with Spring Boot 3.3.5 for high performance and scalability, suitable for stationery retail.

## Configuration Notes

* **Twilio**: Twilio is integrated and working. Set `twilio.account-sid`, `twilio.auth-token`, and `twilio.trial-number` in `application.properties`. The service is used for account verification and password reset via SMS; ensure your Twilio account configuration (phone numbers, messaging service, and allowed destinations) is correctly set for production usage.
* **Cloudinary**: Cloudinary is integrated and working for product image uploads. Set `cloudinary.cloud-name`, `cloudinary.api-key`, and `cloudinary.api-secret` in `application.properties`. The application uses Cloudinary SDK to upload and manage product images.
* **Database**: The `spring.jpa.hibernate.ddl-auto=create-drop` setting is used for development, dropping and recreating the database schema on each application start. Change to `update` or `validate` for production to preserve data.
* **Security**: The default admin credentials (`spring.security.user.name=admin`, `spring.security.user.password=admin123`) are included for testing. Replace with secure credentials in production.

## Contributing

Contributions are welcome! To contribute to Maktba-Backend, please fork the repository, create a new branch for your feature or bug fix, make your changes, and submit a pull request to the main repository. Ensure your contributions align with the project’s [Code of Conduct](CODE_OF_CONDUCT.md) and [Contributing Guidelines](CONTRIBUTING.md).
