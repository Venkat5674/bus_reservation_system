# Bus Reservation System

A comprehensive, cloud-native Bus Reservation System built with Spring Boot, designed for seamless ticket booking and fleet management. This application supports Role-Based Access Control (RBAC) securely managed via JWT authentication and is containerized for easy deployment.

## 🚀 Features

-   **User Authentication**: Secure Registration and Login with JWT (JSON Web Tokens).
-   **Role-Based Access Control**:
    -   **Admin**: Add buses, manage fleet, view bookings.
    -   **User**: Search for buses, view availability, book tickets.
-   **Bus Management**: Create and manage bus schedules, routes, and seat availability.
-   **Search Functionality**: Filter buses by source and destination.
-   **Responsive UI**: Built with Thymeleaf and modern CSS for a premium user experience.
-   **Global Exception Handling**: Graceful error handling with custom responses.
-   **Terminal Ready**: Runs directly with the Maven wrapper and uses a local file-backed H2 database by default.

## 🛠️ Tech Stack

-   **Backend**: Java 17, Spring Boot 3.5.10
-   **Database**:
    -   **Development**: H2 file database (no separate database server required)
    -   **Optional**: MySQL or PostgreSQL through environment variables
-   **Security**: Spring Security, JWT
-   **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
-   **DevOps**: Maven

## 📋 Prerequisites

-   Java 17 or higher
-   Maven 3.8+
-   Maven is not required because the project includes the Maven wrapper.

## 🔧 Installation & Running Locally

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/Venkat5674/bus_reservation_system.git
    cd bus-reservation_system
    ```

2.  **Build and Run**
    ```bash
    .\mvnw.cmd clean install
    .\mvnw.cmd spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

The default database is stored in the project's `data` directory and is created automatically. To use an external database, set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER`, and `DB_DIALECT` before starting the application.

## Deploy on Railway

1. Push the project to GitHub.
2. Create a new project at [Railway](https://railway.app/) and choose **Deploy from GitHub Repo**.
3. Select this repository. Railway will use `railway.json` to build and start the application.
4. Open the generated Railway domain. No Docker or database service is required for a demo deployment.

The deployed app uses the same H2 file database as local development. Railway's default filesystem is temporary, so database data can be lost when the service is redeployed. Use an external database by setting the `DB_*` variables when persistent production data is required.

## 🔌 API Endpoints

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | User Registration | Public |
| `POST` | `/api/auth/login` | User Login & Token Generation | Public |
| `GET` | `/api/buses` | Get All Buses (Paginated) | Public/User |
| `GET` | `/api/buses/search` | Search Bus by Source/Dest | Public/User |
| `POST` | `/api/buses` | Add New Bus | **Admin** |

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.
