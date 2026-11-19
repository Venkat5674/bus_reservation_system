# Bus Reservation System

A comprehensive, production-ready, cloud-native Bus Reservation System built with Spring Boot, designed for seamless ticket booking and fleet management. This application supports Role-Based Access Control (RBAC) securely managed via JWT authentication, is containerized, and is configured for deployment on Oracle Cloud Infrastructure (OCI) Always Free Compute VMs.

## 🚀 Features

-   **User Authentication**: Secure Registration and Login with JWT (JSON Web Tokens).
-   **Role-Based Access Control (RBAC)**:
    -   **Admin**: Add buses, manage fleet, view all bookings and users.
    -   **User**: Search buses, view availability, book seats, cancel bookings.
-   **Bus Management**: Create and manage bus schedules, routes, and seat availability.
-   **Search Functionality**: Filter buses by source and destination.
-   **Health & Monitoring**: Spring Boot Actuator endpoints (`/actuator/health`, `/actuator/info`) with private metrics.
-   **Responsive UI**: Built with Thymeleaf, modern CSS, and vanilla JavaScript.
-   **Global Exception Handling**: Structured, standardized JSON API error responses.
-   **Production Ready**: Externalized secrets, graceful shutdown, connection pooling (HikariCP), and multi-arch containerization (AMD64 / ARM64 Ampere A1).

## 🛠️ Tech Stack

-   **Backend**: Java 17 / 21 LTS, Spring Boot 3.5.10, Spring Security 6, Spring Data JPA
-   **Database**:
    -   **Development / Testing**: File-based or in-memory H2
    -   **Production**: PostgreSQL (Local, Supabase, or OCI Managed PostgreSQL)
-   **Security**: Spring Security, JJWT (HMAC SHA-256), BCrypt
-   **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
-   **DevOps & Deployment**: Maven Wrapper, Docker, Linux systemd, Nginx reverse proxy, Certbot Let's Encrypt

---

## 📋 Prerequisites

-   Java 17 or Java 21 LTS installed
-   Maven 3.8+ (or use the included `./mvnw` / `mvnw.cmd` wrapper)
-   Docker (optional)

---

## 🔧 Local Development & Testing

1.  **Run All Automated Tests**:
    ```bash
    # Linux / macOS
    ./mvnw clean test

    # Windows
    .\mvnw.cmd clean test
    ```

2.  **Package Production Executable JAR**:
    ```bash
    # Linux / macOS
    ./mvnw clean package

    # Windows
    .\mvnw.cmd clean package
    ```
    The runnable fat JAR is created at `target/bus-reservation-0.0.1-SNAPSHOT.jar`.

3.  **Run Locally**:
    ```bash
    # Linux / macOS
    ./mvnw spring-boot:run

    # Windows
    .\mvnw.cmd spring-boot:run
    ```
    Access the UI at `http://localhost:8080/` and health check at `http://localhost:8080/actuator/health`.

---

## 🔌 REST API Summary

| Method | Endpoint | Description | Role / Auth |
| :--- | :--- | :--- | :--- |
| `GET` | `/actuator/health` | Application & DB Health Check | Public |
| `POST` | `/api/auth/register` | User Registration | Public |
| `POST` | `/api/auth/login` | User / Admin Login (Returns JWT) | Public |
| `GET` | `/api/buses` | Get All Buses (Paginated) | Public |
| `GET` | `/api/buses/search` | Search Bus by Source & Destination | Public |
| `POST` | `/api/buses` | Add New Bus Schedule | **ADMIN** |
| `POST` | `/api/bookings` | Book Seat (`userId`, `busId`, `seatNumber`) | **USER / ADMIN** |
| `PUT` | `/api/bookings/cancel/{id}` | Cancel Seat Reservation | **USER / ADMIN** |
| `GET` | `/api/bookings` | Get All Bookings (Paginated) | **USER / ADMIN** |
| `GET` | `/api/users` | List Users (Paginated) | **ADMIN** |
| `GET` | `/api/users/{id}` | Get User by ID | **ADMIN** |

---

## ☁️ Oracle Cloud Always Free Deployment Guide

### Architecture Overview

```
[ Internet ]
     │ (HTTPS :443 / HTTP :80)
     ▼
[ OCI Always Free Compute VM (Ubuntu 22.04/24.04 LTS) ]
     │
┌────┴───────────────────────────────────────────────────────┐
│ [ Nginx Reverse Proxy & SSL (Certbot) ]                     │
│    │                                                        │
│    ▼ (Private Localhost:8080)                               │
│ [ Spring Boot Application (systemd: bus-reservation.service)│
│    │                                                        │
│    ▼ (Private Localhost:5432)                               │
│ [ PostgreSQL Database: bus_reservation_db ]                │
└────────────────────────────────────────────────────────────┘
```

### Step 1: Create OCI Compute Instance (Always Free Eligible)
1. In Oracle Cloud Console, navigate to **Compute -> Instances -> Create Instance**.
2. **Image**: Canonical Ubuntu 22.04 or 24.04 LTS.
3. **Shape**:
   - Option A: **VM.Standard.A1.Flex** (Ampere ARM - Always Free up to 4 OCPUs and 24 GB RAM) - Recommended.
   - Option B: **VM.Standard.E2.1.Micro** (AMD x86_64 - Always Free 1 OCPU, 1 GB RAM).
4. **Networking**: Assign a public IPv4 address and select an existing VCN / Subnet.
5. **SSH Keys**: Download the generated private key or paste your public SSH key.

### Step 2: Configure OCI Security List (Ingress Rules)
Under **Virtual Cloud Networks -> VCN -> Security Lists -> Default Security List**, add Ingress Rules:
- **SSH**: Source `0.0.0.0/0` (or your IP), Protocol `TCP`, Destination Port `22`
- **HTTP**: Source `0.0.0.0/0`, Protocol `TCP`, Destination Port `80`
- **HTTPS**: Source `0.0.0.0/0`, Protocol `TCP`, Destination Port `443`
*(Do NOT open port 8080 or 5432 to the public internet; keep them bound locally).*

### Step 3: Run Server Setup Script on VM
Connect via SSH:
```bash
ssh -i /path/to/private_key ubuntu@<YOUR_VM_PUBLIC_IP>
```
Clone or transfer the repository, then execute:
```bash
cd bus_reservation_system-main
chmod +x deploy/*.sh
sudo ./deploy/setup-server.sh
```
This automatically installs Java 21, PostgreSQL, Nginx, UFW firewall rules, and creates `/opt/bus-reservation/.env.production`.

### Step 4: Deploy Executable JAR
Copy your packaged JAR from your build machine to the server:
```bash
scp -i /path/to/private_key target/bus-reservation-0.0.1-SNAPSHOT.jar ubuntu@<YOUR_VM_PUBLIC_IP>:/tmp/
```
On the server, run:
```bash
sudo ./deploy/deploy.sh /tmp/bus-reservation-0.0.1-SNAPSHOT.jar
```

### Step 5: Verify Deployment & Health Check
Run the automated verification script:
```bash
./deploy/health-check.sh <YOUR_VM_PUBLIC_IP> 80
```
Run the full API smoke test suite:
```bash
./smoke-tests.sh http://<YOUR_VM_PUBLIC_IP>
```

### Step 6: Enable HTTPS (Let's Encrypt SSL)
If you have a domain pointed to your VM's public IP:
```bash
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

---

## 🔒 Production Environment Variables Checklist

Set these in `/opt/bus-reservation/.env.production` (chmod `600`):

| Variable | Description | Example / Default |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `PORT` | Internal HTTP listening port | `8080` |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/bus_reservation_db?sslmode=prefer` |
| `DB_USER` | PostgreSQL username | `bus_app_user` |
| `DB_PASSWORD` | Strong PostgreSQL password | `[Managed Secure Password]` |
| `JWT_SECRET` | 256-bit Hex JWT Secret Key | `[Generated 64+ char Hex String]` |
| `JWT_EXPIRATION_MS` | Token expiry in ms | `604800000` (7 days) |
| `ADMIN_NAME` | Initial seeded Admin display name | `Admin Venkatesh` |
| `ADMIN_EMAIL` | Admin login email | `admin@yourdomain.com` |
| `ADMIN_PASSWORD` | Admin initial login password | `[Managed Strong Password]` |

---

## ☁️ Deploying to Render (Free Cloud Deployment)

You can deploy this application to **[Render.com](https://render.com/)** in two simple ways:

### Option A: 1-Click Render Blueprint (Recommended)
Because the repository includes `render.yaml`, Render can provision both the Spring Boot Web Service and a Free Managed PostgreSQL Database simultaneously.

1. Push your repository to **GitHub** or **GitLab**.
2. Log in to [Render Dashboard](https://dashboard.render.com/).
3. Click **"New +"** &rarr; **"Blueprint"**.
4. Connect your GitHub repository.
5. Render reads `render.yaml` and will automatically configure:
   - **Database**: PostgreSQL instance (`busdb`)
   - **Web Service**: Dockerized Spring Boot application with `-XX:+UseSerialGC` memory tuning for Render's 512MB RAM tier
   - **Environment Variables**: Injects DB host, port, credentials, and auto-generates `JWT_SECRET`.
6. Click **"Apply"**. Render will build and deploy your app with a free `https://<your-app>.onrender.com` public URL with automatic HTTPS!

### Option B: Manual Web Service Deployment on Render
1. In Render Dashboard, click **"New +"** &rarr; **"Web Service"**.
2. Connect your Git repository.
3. Configure the following settings:
   - **Runtime**: `Docker`
   - **Instance Type**: `Free`
   - **Health Check Path**: `/actuator/health`
4. Under **Environment Variables**, add:
   - `SPRING_PROFILES_ACTIVE`: `prod` (or leave default to run with file-based H2)
   - `PORT`: `10000` (Render default)
   - `JWT_SECRET`: *(A random 64-character hex or string)*
   - `ADMIN_EMAIL`: `admin@yourdomain.com`
   - `ADMIN_PASSWORD`: `YourAdminPassword123`
   - If using PostgreSQL:
     - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` (copied from your Render PostgreSQL instance)
5. Click **"Create Web Service"**.

---

## 📄 License

This project is licensed under the MIT License.
