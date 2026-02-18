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
-   **Production Ready**: Docker support and ready-to-deploy configuration for Render (PostgreSQL).

## 🛠️ Tech Stack

-   **Backend**: Java 17, Spring Boot 3.5.10
-   **Database**:
    -   **Development**: MySQL
    -   **Production**: PostgreSQL (Render Managed)
-   **Security**: Spring Security, JWT
-   **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
-   **DevOps**: Docker, Maven, Render Blueprints

## 📋 Prerequisites

-   Java 17 or higher
-   Maven 3.8+
-   MySQL 8.0+ (for local development)
-   Docker (optional, for containerized run)

## 🔧 Installation & Running Locally

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/Venkat5674/bus_reservation_system.git
    cd bus-reservation_system
    ```

2.  **Configure Database (MySQL)**
    -   Create a database named `busdb` in your MySQL server.
    -   Update `src/main/resources/application.yaml` if your credentials differ from the defaults:
        ```yaml
        spring:
          datasource:
            url: jdbc:mysql://localhost:3306/busdb
            username: root
            password: root
        ```

3.  **Build and Run**
    ```bash
    mvn clean install
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

## 🐳 Docker Support

To run the application as a Docker container:

1.  **Build the Image**
    ```bash
    docker build -t bus-reservation .
    ```

2.  **Run the Container**
    ```bash
    docker run -p 8080:8080 bus-reservation
    ```

## ☁️ Deployment (Render)

This project is configured for **Render Blueprints**.

1.  Push your code to a GitHub repository.
2.  Log in to [Render](https://render.com/).
3.  Click **New +** -> **Blueprint**.
4.  Connect your repository.
5.  Render will automatically detect `render.yaml` and provision:
    -   A **PostgreSQL Database**.
    -   A **Web Service** (Spring Boot App) connected to the database.
6.  Approve the deployment and wait for the "Live" status!

**Note**: The deployment automatically uses the `prod` profile (`application-prod.yaml`) which is optimized for PostgreSQL on Render.

## 🔌 API Endpoints

| Method | Endpoint | Description | Role |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | User Registration | Public |
| `POST` | `/api/auth/login` | User Login & Token Generation | Public |
| `GET` | `/api/buses` | Get All Buses (Paginated) | Public/User |
| `GET` | `/api/buses/search` | Search Bus by Source/Dest | Public/User |
| `POST` | `/api/buses` | Add New Bus | **Admin** |

## Deployement Link : https://bus-reservation-system-ffgo.onrender.com/

## 🤝 Contributing

Contributions are welcome! Please fork the repository and submit a Pull Request.

## 📄 License

This project is licensed under the MIT License.
