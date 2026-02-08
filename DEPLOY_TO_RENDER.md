# How to Deploy to Render

This guide explains how to deploy the Bus Reservation System to Render.com.

## Prerequisites
1.  A GitHub account with this repository pushed.
2.  A [Render.com](https://render.com) account.

## Deployment Steps

### Method 1: Blueprint (Recommended)
1.  Login to Render.com.
2.  Go to **Blueprints** -> **New Blueprint Instance**.
3.  Connect your GitHub repository.
4.  Render will automatically detect `render.yaml` and configure:
    - A **Web Service** (Docker) for the app.
    - A **PostgreSQL Database** (Native to Render, Free Tier available).
    - The application is updated to automatically detect and use PostgreSQL when deployed.

### Method 2: Manual Web Service (Best for External MySQL)
1.  **Database**:
    - Use a managed MySQL provider like **Clever Cloud (Free Tier)**, **Aiven**, or **Railway**.
    - Get your `JDBC URL`, `Username`, and `Password`.
    - Example URL: `jdbc:mysql://your-db-host:3306/bus_reservation?useSSL=false`

2.  **Web Service**:
    - Go to Render Dashboard -> **New +** -> **Web Service**.
    - Connect your GitHub repository.
    - **Runtime**: Select **Docker**.
    - **Environment Variables**: Add the following:
        - `SPRING_DATASOURCE_URL`: Your MySQL JDBC URL.
        - `SPRING_DATASOURCE_USERNAME`: Your DB Username.
        - `SPRING_DATASOURCE_PASSWORD`: Your DB Password.

3.  **Deploy**:
    - Click **Create Web Service**.
    - Render will build the Docker image and deploy it.

## Troubleshooting
- **Build Failed**: Check the logs. Ensure `Dockerfile` is present in the root.
- **App Crashing**: Check the logs. Usually due to incorrect Database URL or credentials.
- **Port**: Render automatically detects port `8080` exposed in Dockerfile.
