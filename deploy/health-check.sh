#!/usr/bin/env bash
# ==============================================================================
# Health Check Verification Script for Bus Reservation System
# Checks systemd service status, local port 8080, and Nginx public proxy
# ==============================================================================

set -euo pipefail

TARGET_HOST="${1:-localhost}"
HTTP_PORT="${2:-80}"

echo "=========================================================="
echo " Running System & Health Verification"
echo "=========================================================="

# 1. Systemd Service Check
echo "[1/4] Checking systemd service status..."
if command -v systemctl >/dev/null 2>&1; then
    sudo systemctl status bus-reservation --no-pager | head -n 12
fi

# 2. Local Actuator Health Check
echo ""
echo "[2/4] Testing local Spring Boot Actuator endpoint (127.0.0.1:8080)..."
LOCAL_HEALTH=$(curl -s -m 5 http://127.0.0.1:8080/actuator/health || echo "FAILED")
echo "Response: ${LOCAL_HEALTH}"

# 3. Nginx Reverse Proxy Endpoint Check
echo ""
echo "[3/4] Testing reverse proxy endpoint (http://${TARGET_HOST}:${HTTP_PORT}/actuator/health)..."
PROXY_HEALTH=$(curl -s -m 5 "http://${TARGET_HOST}:${HTTP_PORT}/actuator/health" || echo "FAILED")
echo "Response: ${PROXY_HEALTH}"

# 4. Public Web Home Page Check
echo ""
echo "[4/4] Testing home page route (http://${TARGET_HOST}:${HTTP_PORT}/)..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -m 5 "http://${TARGET_HOST}:${HTTP_PORT}/" || echo "000")
echo "HTTP Status Code: ${HTTP_CODE}"

if [ "${HTTP_CODE}" = "200" ]; then
    echo "SUCCESS: All health and gateway checks passed!"
    exit 0
else
    echo "WARNING: Home page returned non-200 code (${HTTP_CODE})."
    exit 1
fi
