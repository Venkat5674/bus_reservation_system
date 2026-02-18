# Deploying Bus Reservation System to AWS

This guide outlines the steps to deploy your Spring Boot application to AWS using **Docker**, **Elastic Container Registry (ECR)**, **Elastic Beanstalk (EB)**, and **Relational Database Service (RDS)**.

## Prerequisites

1.  **AWS Account**: [Sign up here](https://aws.amazon.com/).
2.  **AWS CLI**: Installed and configured with `aws configure`. [Installation Guide](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html).
3.  **Docker**: Installed and running.

---

## Step 1: Prepare the Database (AWS RDS)

1.  Log in to the **AWS Management Console** and search for **RDS**.
2.  Click **Create database**.
3.  **Engine**: Choose **MySQL**.
4.  **Template**: Select **Free Tier** (if eligible) or **Dev/Test**.
5.  **Settings**:
    *   **DB Instance Identifier**: `bus-reservation-db`
    *   **Master Username**: `admin`
    *   **Master Password**: Create a strong password (e.g., `SecurePass123!`). **Remember this.**
6.  **Connectivity**:
    *   **Public Access**: **No** (Recommended for security).
    *   **VPC Security Group**: Create a new one (e.g., `rds-launch-wizard`).
7.  **Additional Configuration**:
    *   **Initial Database Name**: `busdb` (Crucial! Do not leave this empty).
8.  Click **Create database**.
9.  Wait for the status to become **Available**. Note the **Endpoint** (e.g., `bus-reservation-db.xxxxx.us-east-1.rds.amazonaws.com`).

---

## Step 2: Build and Push Docker Image (AWS ECR)

1.  **Create a Repository**:
    *   Go to **Elastic Container Registry (ECR)** in AWS Console.
    *   Click **Get Started** / **Create repository**.
    *   **Repository name**: `bus-reservation`.
    *   Click **Create repository**.

2.  **Push Image to ECR**:
    *   Select the repository `bus-reservation` and click **View push commands**.
    *   Run the commands in your terminal (PowerShell):
        1.  **Login**:
            ```powershell
            (Get-ECRLoginCommand).Password | docker login --username AWS --password-stdin 783896581564.dkr.ecr.ap-south-1.amazonaws.com
            ```
            *OR if you use AWS CLI Standard:*
            ```powershell
            aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 783896581564.dkr.ecr.ap-south-1.amazonaws.com
            ```
        2.  **Build**:
            ```powershell
            docker build -t bus-reservation .
            ```
        3.  **Tag**:
            ```powershell
            docker tag bus-reservation:latest 783896581564.dkr.ecr.ap-south-1.amazonaws.com/bus-reservation:latest
            ```
        4.  **Push**:
            ```powershell
            docker push 783896581564.dkr.ecr.ap-south-1.amazonaws.com/bus-reservation:latest
            ```

---

## Step 3: Deploy to Elastic Beanstalk

1.  **Prepare Configuration**:
    *   Open `Dockerrun.aws.json` in your project.
    *   Replace `%AWS_ACCOUNT_ID%` and `%AWS_REGION%` with your actual values (e.g., `123456789012` and `us-east-1`).

2.  **Create Environment**:
    *   Go to **Elastic Beanstalk** -> **Create application**.
    *   **Application Name**: `BusReservationSystem`.
    *   **Platform**: **Docker** (Amazon Linux 2023).
    *   **Application Code**:
        *   Select **Upload your code**.
        *   Upload the modified `Dockerrun.aws.json` file.
    *   **Presets**: **Single instance (free tier eligible)**.
    *   Click **Next** until **Configure updates, monitoring, and logging**.

3.  **Configure Environment Properties**:
    *   Go to **Modify Software** (or look for "Environment properties" in the config steps).
    *   Add the following properties:
        *   `DB_URL`: `jdbc:mysql://<YOUR_RDS_ENDPOINT>:3306/busdb` (Replace `<YOUR_RDS_ENDPOINT>`)
        *   `DB_USERNAME`: `admin`
        *   `DB_PASSWORD`: `<YOUR_RDS_PASSWORD>`
        *   `SERVER_PORT`: `8080`

4.  **Network Access (Important)**:
    *   Once the environment is created, go to **EC2** -> **Security Groups**.
    *   Find the Security Group for your RDS instance (`rds-launch-wizard`...).
    *   Edit **Inbound rules**.
    *   Add a rule: **Type**: `MySQL/Aurora` (3306), **Source**: Custom -> Select the Security Group of your **Elastic Beanstalk** environment.
    *   This allows your app to talk to the database.

5.  **Launch**:
    *   Click **Create environment** / **Submit**.
    *   Wait for the health to turn Green.

---

## Step 4: Access Application

*   Click the URL provided in the Elastic Beanstalk dashboard.
*   Your application is now live!

---

## Troubleshooting

### Error: `Get-ECRLoginCommand is not recognized`
This error means **AWS Tools for PowerShell** is not installed.
**Solution**:
1.  Install **AWS CLI** (Recommended):
    *   Download and install from: [https://aws.amazon.com/cli/](https://aws.amazon.com/cli/)
    *   **Restart your terminal** (PowerShell/CMD) after installation.
    *   Verify installation: `aws --version`
2.  Use the standard AWS CLI login command instead of the PowerShell cmdlet:
    ```powershell
    aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin 783896581564.dkr.ecr.ap-south-1.amazonaws.com
    ```
