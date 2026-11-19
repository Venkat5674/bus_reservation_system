#!/usr/bin/env bash
# ==============================================================================
# Application Deployment Script for Bus Reservation System
# Handles safe deployment, health polling, and rollback on failure
# ==============================================================================

set -euo pipefail

APP_DIR="/opt/bus-reservation"
JAR_SOURCE="${1:-target/bus-reservation-0.0.1-SNAPSHOT.jar}"
APP_JAR="${APP_DIR}/bus-reservation.jar"
BACKUP_JAR="${APP_DIR}/bus-reservation.jar.bak"
HEALTH_URL="http://127.0.0.1:8080/actuator/health"
MAX_ATTEMPTS=20
WAIT_SECONDS=3

echo "Starting deployment of Bus Reservation System..."

if [ ! -f "${JAR_SOURCE}" ]; then
    echo "ERROR: JAR file not found at: ${JAR_SOURCE}"
    echo "Build the JAR first: ./mvnw clean package"
    exit 1
fi

# Backup existing JAR if present
if [ -f "${APP_JAR}" ]; then
    echo "Creating backup of current running JAR..."
    cp "${APP_JAR}" "${BACKUP_JAR}"
fi

# Copy new JAR
echo "Installing new JAR into ${APP_JAR}..."
cp "${JAR_SOURCE}" "${APP_JAR}"
chmod 640 "${APP_JAR}"

# Restart Service
echo "Restarting bus-reservation service..."
sudo systemctl restart bus-reservation

# Verify Health
echo "Waiting for application to pass health checks at ${HEALTH_URL}..."
ATTEMPTS=0
HEALTH_STATUS=""

while [ $ATTEMPTS -lt $MAX_ATTEMPTS ]; do
    ATTEMPTS=$((ATTEMPTS + 1))
    HEALTH_STATUS=$(curl -s -m 5 "${HEALTH_URL}" | jq -r '.status // empty' 2>/dev/null || true)

    if [ "${HEALTH_STATUS}" = "UP" ]; then
        echo "SUCCESS: Application is UP and healthy! (Attempt ${ATTEMPTS}/${MAX_ATTEMPTS})"
        break
    fi

    echo "Attempt ${ATTEMPTS}/${MAX_ATTEMPTS}: Status is '${HEALTH_STATUS:-DOWN}'. Waiting ${WAIT_SECONDS}s..."
    sleep $WAIT_SECONDS
done

if [ "${HEALTH_STATUS}" != "UP" ]; then
    echo "CRITICAL: Health check failed after $((MAX_ATTEMPTS * WAIT_SECONDS)) seconds!"
    echo "Recent application logs:"
    sudo journalctl -u bus-reservation -n 40 --no-pager

    if [ -f "${BACKUP_JAR}" ]; then
        echo "Rolling back to previous backup JAR..."
        cp "${BACKUP_JAR}" "${APP_JAR}"
        sudo systemctl restart bus-reservation
        echo "Rollback completed. Please inspect logs above."
    fi
    exit 1
fi

echo "=========================================================="
echo " Deployment Completed Successfully!"
echo " Service Status: Active (Running)"
echo " Health Status: UP"
echo "=========================================================="
