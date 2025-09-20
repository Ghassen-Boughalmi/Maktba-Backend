# Maktba-Backend

🛒 **Maktba-Backend**: A Spring Boot backend for a mobile app managing stationery stores (books, paper, pens). Built with robust security (JWT-based authentication), real-time inventory management, and high performance. Integrates **Cloudinary** for image storage, **Twilio** for SMS notifications (planned), and **PostgreSQL** for reliable data persistence. Ideal for stationery retail businesses. 📚✏️ #SpringBoot #Stationery #Cloudinary #Twilio #PostgreSQL

## Table of Contents
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Features](#features)
- [Configuration Notes](#configuration-notes)
- [Contributing](#contributing)

## Getting Started

### Prerequisites
Before you begin, ensure you have the following requirements:

- **Java Development Kit (JDK)**: Version 21 (as specified in `build.gradle`).
- **Gradle**: Version 8.0 or higher for building the project.
- **PostgreSQL**: Version 14 or higher, with a running database instance.
- **Cloudinary Account**: For image uploads (requires `cloud_name`, `api_key`, and `api_secret`).
- **Twilio Account**: For SMS notifications (requires `account_sid`, `auth_token`, and `trial_number`; currently commented out).
- An IDE with Spring Boot support (e.g., IntelliJ IDEA, Eclipse, or VS Code).
- **Postman**: For testing API endpoints.

### Installation
1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/maktba-backend.git
   ```

2. **Navigate to the project directory**:
   ```bash
   cd maktba-backend
   ```

3. **Configure application properties**:
   - Update `src/main/resources/application.properties` with your PostgreSQL credentials and (optionally) Cloudinary and Twilio credentials:
     ```properties
     spring.application.name=maktba
     spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
     spring.datasource.username=postgres
     spring.datasource.password=root
     spring.datasource.driver-class-name=org.postgresql.Driver
     spring.jpa.hibernate.ddl-auto=create-drop
     spring.security.user.name=admin
     spring.security.user.password=admin123
     # Twilio (commented out until SMS functionality is implemented)
     #twillio.account-sid=
     #twillio.auth-token=
     #twillio.trial-number=
     # Cloudinary (commented out until image upload is fully configured)
     #cloudinary.cloud-name=
     #cloudinary.api-key=
     #cloudinary.api-secret=
     logging.level.org.springframework=DEBUG
     logging.level.com.tn.maktba=DEBUG
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
- **User Authentication**: Secure JWT-based authentication for user registration and login.
- **Product Management**: CRUD operations for managing stationery products (books, pens, paper) with image uploads via Cloudinary.
- **Category Management**: Organize products into categories (e.g., Books, Stationery) with CRUD operations.
- **Shopping Cart**: Each user has a personal cart to add, remove, or reset items. Users can create and modify carts, with confirmed carts visible to admins in a dedicated list. Modifications to confirmed carts update the admin list.
- **Admin Dashboard**: Admins can view and manage confirmed carts, with real-time updates for modified carts.
- **Real-Time Inventory**: Track product quantities in real-time using PostgreSQL.
- **Validation and Error Handling**: Robust input validation for product, category, and cart data to ensure data integrity.
- **Scalable Architecture**: Built with Spring Boot 3.3.5 for high performance and scalability, suitable for stationery retail.

## Configuration Notes
- **Twilio**: The Twilio configuration (`twillio.account-sid`, `twillio.auth-token`, `twillio.trial-number`) is commented out in `application.properties` as SMS notification functionality is planned but not yet implemented. Uncomment and configure these properties when integrating Twilio for sending order confirmations or notifications.
- **Cloudinary**: The Cloudinary configuration (`cloudinary.cloud-name`, `cloudinary.api-key`, `cloudinary.api-secret`) is commented out in `application.properties` to allow local development without requiring a Cloudinary account. Uncomment and provide valid credentials to enable image uploads for products.
- **Database**: The `spring.jpa.hibernate.ddl-auto=create-drop` setting is used for development, dropping and recreating the database schema on each application start. Change to `update` or `validate` for production to preserve data.
- **Security**: The default admin credentials (`spring.security.user.name=admin`, `spring.security.user.password=admin123`) are included for testing. Replace with secure credentials in production.

## Contributing
Contributions are welcome! To contribute to Maktba-Backend, please fork the repository, create a new branch for your feature or bug fix, make your changes, and submit a pull request to the main repository. Ensure your contributions align with the project’s [Code of Conduct](CODE_OF_CONDUCT.md) and [Contributing Guidelines](CONTRIBUTING.md).
