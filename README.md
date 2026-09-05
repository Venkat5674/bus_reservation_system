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
-   **DevOps**: Maven, Render

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

## Deploy on Render with Supabase

1. Create a free project at [Supabase](https://supabase.com/) and open **Project Settings -> Database**.
2. Copy the direct connection host, or use the Supabase session pooler host if your network does not support IPv6. Use port `5432`, database `postgres`, and user `postgres`.
3. Push the project to GitHub.
4. In [Render](https://render.com/), choose **New -> Blueprint** and select the repository. Render will detect `render.yaml` and build the Docker image.
5. In the Render service environment variables, set `DB_HOST` to the Supabase host and `DB_PASSWORD` to the Supabase database password. The other PostgreSQL values are already configured.
6. Deploy and open the generated Render URL.

The production profile uses PostgreSQL with SSL and `ddl-auto: update`, so the tables are created automatically in Supabase. Render's free web service may sleep after inactivity, but Supabase stores the application data persistently.

## 🔌 API Endpoints

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | User Registration | Public |
| `POST` | `/api/auth/login` | User Login & Token Generation | Public |
| `GET` | `/api/buses` | Get All Buses (Paginated) | Public/User |
| `GET` | `/api/buses/search` | Search Bus by Source/Dest | Public/User |
| `POST` | `/api/buses` | Add New Bus | **Admin** |

## Deployed Link : 

    https://bus-reservation-system-mxoi.onrender.com/

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.
