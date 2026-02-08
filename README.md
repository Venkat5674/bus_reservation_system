# Bus Reservation System

A production-grade Bus Reservation System built with Spring Boot, JSP, and MySQL.

## Prerequisites
- Java 17+
- Maven 3+
- MySQL Server

## Setup Instructions

1.  **Database Configuration**
    - Create a MySQL database named `bus_reservation`.
    - Update `src/main/resources/application.properties` with your MySQL username and password (default: `root`/`root`).
    - The application is configured to create/update tables automatically (`hibernate.ddl-auto=update`).

2.  **Build and Run (Local)**
    - Open a terminal in the project root.
    - Run `mvn spring-boot:run`.
    - The application will start on `http://localhost:8080`.

3.  **Build and Run (Docker)**
    - Ensure Docker Desktop is running.
    - Run `docker compose up --build`.
    - This will start both the application and a MySQL database container.
    - Access the app at `http://localhost:8080`.

## Features

### User
- **Register/Login**: Secure session-based authentication.
- **Search Buses**: Find buses by source, destination, and date.
- **Seat Selection**: Interactive grid to select available seats.
- **Booking**: Seat locking mechanism prevents double booking.
- **View Ticket**: view booking history.

### Admin
- **Login**: Admin access via same login page (Role: ADMIN).
- **Dashboard**: Manage Buses, Routes, and Schedules.
- **Add Bus**: Define bus details and capacity.
- **Add Route**: Define source, destination, and distance.
- **Add Schedule**: Assign buses to routes with timing and price.

## Project Structure
- `src/main/java/com/example/busreservation`: Backend source code.
    - `entity`: JPA Entities.
    - `repository`: Data Access Layer.
    - `service`: Business Logic & Transaction Management.
    - `controller`: Web Controllers.
- `src/main/webapp/WEB-INF/jsp`: Frontend JSP views.
- `db/schema.sql`: Database schema reference.

## Default Admin Credentials
When the application starts for the first time, a default administrator account is created automatically:
- **Email**: `admin@bus.com`
- **Password**: `admin`

## Testing Data
To test the flow:
1.  Login as Admin (`admin@bus.com` / `admin`).
2.  Ad Admin: Add a Bus, Add a Route, then Add a Schedule.
3.  Logout and Registers as a new User.
4.  Login as User: Search for that route/date, select a seat, and book.
