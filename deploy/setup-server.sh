#!/usr/bin/env bash
# ==============================================================================
# Oracle Cloud Ubuntu VM Initialization Script
# Architecture: AMD64 or ARM64 (Ampere A1 Always Free)
# OS: Ubuntu 22.04 / 24.04 LTS
# ==============================================================================

set -euo pipefail

echo "=========================================================="
echo " Starting Server Setup for Bus Reservation System"
echo "=========================================================="

# 1. Update and Upgrade Packages
echo "[1/7] Updating system packages..."
sudo apt-get update -y && sudo apt-get upgrade -y
sudo apt-get install -y --no-install-recommends \
    curl \
    wget \
    jq \
    ufw \
    nginx \
    certbot \
    python3-certbot-nginx \
    openjdk-21-jre-headless \
    postgresql \
    postgresql-contrib

# 2. Configure Firewall (OCI and UFW)
echo "[2/7] Configuring UFW Firewall..."
# Enable UFW and allow SSH, HTTP, HTTPS
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow 22/tcp comment 'SSH'
sudo ufw allow 80/tcp comment 'HTTP'
sudo ufw allow 443/tcp comment 'HTTPS'
# Port 8080 and 5432 remain internal only!
echo "y" | sudo ufw enable
sudo ufw status verbose

# Also ensure Oracle Cloud iptables rules allow 80 & 443 (Ubuntu images on OCI include custom iptables)
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 80 -j ACCEPT || true
sudo iptables -I INPUT 6 -m state --state NEW -p tcp --dport 443 -j ACCEPT || true
sudo netfilter-persistent save || true

# 3. Create Application Directory
echo "[3/7] Setting up application workspace..."
APP_DIR="/opt/bus-reservation"
sudo mkdir -p "${APP_DIR}"
sudo chown -R ubuntu:ubuntu "${APP_DIR}"
chmod 750 "${APP_DIR}"

# 4. Initialize PostgreSQL Database
echo "[4/7] Configuring PostgreSQL..."
DB_NAME="bus_reservation_db"
DB_USER="bus_app_user"
# Generate secure random DB password if not provided
DB_PASSWORD="${1:-$(openssl rand -base64 18 | tr -dc 'a-zA-Z0-9')}"

sudo -u postgres psql -tc "SELECT 1 FROM pg_database WHERE datname = '${DB_NAME}'" | grep -q 1 || \
sudo -u postgres psql <<EOF
CREATE DATABASE ${DB_NAME};
CREATE USER ${DB_USER} WITH ENCRYPTED PASSWORD '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON DATABASE ${DB_NAME} TO ${DB_USER};
\c ${DB_NAME}
GRANT ALL ON SCHEMA public TO ${DB_USER};
EOF

echo "PostgreSQL database '${DB_NAME}' configured for user '${DB_USER}'."

# 5. Create .env.production file if not already present
ENV_FILE="${APP_DIR}/.env.production"
if [ ! -f "${ENV_FILE}" ]; then
    echo "[5/7] Creating ${ENV_FILE}..."
    JWT_SECRET=$(openssl rand -hex 32)
    cat <<EOF > "${ENV_FILE}"
SPRING_PROFILES_ACTIVE=prod
PORT=8080
DB_URL=jdbc:postgresql://localhost:5432/${DB_NAME}?sslmode=prefer
DB_USER=${DB_USER}
DB_PASSWORD=${DB_PASSWORD}
JWT_SECRET=${JWT_SECRET}
JWT_EXPIRATION_MS=604800000
ADMIN_NAME=Admin Venkatesh
ADMIN_EMAIL=admin@yourdomain.com
ADMIN_PASSWORD=$(openssl rand -base64 12)
EOF
    chmod 600 "${ENV_FILE}"
    chown ubuntu:ubuntu "${ENV_FILE}"
    echo "Generated production secrets in ${ENV_FILE}"
else
    echo "[5/7] ${ENV_FILE} already exists, preserving existing secrets."
fi

# 6. Configure Nginx
echo "[6/7] Configuring Nginx reverse proxy..."
if [ -f "deploy/nginx-bus-reservation.conf" ]; then
    sudo cp deploy/nginx-bus-reservation.conf /etc/nginx/sites-available/bus-reservation
elif [ -f "/opt/bus-reservation/nginx-bus-reservation.conf" ]; then
    sudo cp /opt/bus-reservation/nginx-bus-reservation.conf /etc/nginx/sites-available/bus-reservation
fi

sudo rm -f /etc/nginx/sites-enabled/default
sudo ln -sf /etc/nginx/sites-available/bus-reservation /etc/nginx/sites-enabled/bus-reservation
sudo nginx -t && sudo systemctl reload nginx

# 7. Install systemd service
echo "[7/7] Installing systemd service..."
if [ -f "deploy/bus-reservation.service" ]; then
    sudo cp deploy/bus-reservation.service /etc/systemd/system/bus-reservation.service
elif [ -f "/opt/bus-reservation/bus-reservation.service" ]; then
    sudo cp /opt/bus-reservation/bus-reservation.service /etc/systemd/system/bus-reservation.service
fi

sudo systemctl daemon-reload
sudo systemctl enable bus-reservation.service

echo "=========================================================="
echo " Server Setup Completed Successfully!"
echo " Next steps:"
echo " 1. Copy 'bus-reservation-0.0.1-SNAPSHOT.jar' to ${APP_DIR}/bus-reservation.jar"
echo " 2. Run: sudo systemctl start bus-reservation"
echo " 3. Verify: curl http://localhost:8080/actuator/health"
echo "=========================================================="
