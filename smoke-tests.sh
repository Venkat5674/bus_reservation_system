#!/usr/bin/env bash
# ==============================================================================
# Comprehensive API Smoke Test Suite for Bus Reservation System
# Tests End-to-End API Workflows, Security Enforcement, and Data Integrity
# Usage: ./smoke-tests.sh [BASE_URL] [ADMIN_EMAIL] [ADMIN_PASSWORD]
# Example: ./smoke-tests.sh http://localhost:8080
# ==============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
ADMIN_EMAIL="${2:-admin@yourdomain.com}"
ADMIN_PASSWORD="${3:-AdminTestPassword123}"
TIMESTAMP=$(date +%s)
TEST_USER_EMAIL="smoketest_${TIMESTAMP}@example.com"
TEST_USER_PASS="SmokePass123!"

echo "=========================================================="
echo " BUS RESERVATION SYSTEM - API SMOKE TEST SUITE"
echo " Target Base URL: ${BASE_URL}"
echo "=========================================================="

PASSED=0
FAILED=0

run_check() {
    local test_name="$1"
    local expected="$2"
    local actual="$3"

    if echo "${actual}" | grep -q "${expected}"; then
        echo "  [PASS] ${test_name}"
        PASSED=$((PASSED + 1))
    else
        echo "  [FAIL] ${test_name} - Expected '${expected}' but got: ${actual}"
        FAILED=$((FAILED + 1))
    fi
}

# 1. Health Check
echo "[Test 1] Actuator Health Check"
HEALTH_RESP=$(curl -s -m 5 "${BASE_URL}/actuator/health" || echo "")
run_check "Health Status UP" '"status":"UP"' "${HEALTH_RESP}"

# 2. Public Bus Listing
echo "[Test 2] Public Get All Buses"
BUSES_RESP=$(curl -s -m 5 "${BASE_URL}/api/buses" || echo "")
run_check "Get All Buses Status 200" '"success":true' "${BUSES_RESP}"

# 3. User Registration
echo "[Test 3] User Registration (/api/auth/register)"
REGISTER_PAYLOAD="{\"name\":\"Smoke Tester\",\"email\":\"${TEST_USER_EMAIL}\",\"password\":\"${TEST_USER_PASS}\"}"
REG_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "${REGISTER_PAYLOAD}" || echo "")
run_check "User Registration Success" '"success":true' "${REG_RESP}"
USER_TOKEN=$(echo "${REG_RESP}" | jq -r '.data.token // empty' 2>/dev/null || true)

# 4. User Login
echo "[Test 4] User Login (/api/auth/login)"
LOGIN_PAYLOAD="{\"email\":\"${TEST_USER_EMAIL}\",\"password\":\"${TEST_USER_PASS}\"}"
LOGIN_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "${LOGIN_PAYLOAD}" || echo "")
run_check "User Login Success" '"success":true' "${LOGIN_RESP}"

# 5. Invalid Login Validation
echo "[Test 5] Invalid Credentials Rejected"
BAD_LOGIN_PAYLOAD="{\"email\":\"${TEST_USER_EMAIL}\",\"password\":\"WrongPassword\"}"
BAD_LOGIN_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "${BAD_LOGIN_PAYLOAD}" || echo "")
run_check "Invalid Login Returns 401/Failure" '"success":false' "${BAD_LOGIN_RESP}"

# 6. Admin Login
echo "[Test 6] Admin Login (/api/auth/login)"
ADMIN_LOGIN_PAYLOAD="{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASSWORD}\"}"
ADMIN_LOGIN_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "${ADMIN_LOGIN_PAYLOAD}" || echo "")
ADMIN_TOKEN=$(echo "${ADMIN_LOGIN_RESP}" | jq -r '.data.token // empty' 2>/dev/null || true)
if [ -n "${ADMIN_TOKEN}" ]; then
    run_check "Admin Authentication" '"role":"ROLE_ADMIN"' "${ADMIN_LOGIN_RESP}"
else
    echo "  [WARN] Admin login skipped (credentials not matching or custom). Using user token for remaining tests."
    ADMIN_TOKEN="${USER_TOKEN}"
fi

# 7. Bus Creation (Admin Only)
echo "[Test 7] Create Bus Schedule"
DEPARTURE_TIME=$(date -u -d "+1 day" +"%Y-%m-%dT10:00" 2>/dev/null || date -u -v+1d +"%Y-%m-%dT10:00" 2>/dev/null || echo "2026-12-01T10:00")
ARRIVAL_TIME=$(date -u -d "+1 day +4 hours" +"%Y-%m-%dT14:00" 2>/dev/null || date -u -v+1d -v+4H +"%Y-%m-%dT14:00" 2>/dev/null || echo "2026-12-01T14:00")
BUS_NUM="SMOKE-${TIMESTAMP}"
BUS_PAYLOAD="{\"busNumber\":\"${BUS_NUM}\",\"source\":\"SmokeCityA\",\"destination\":\"SmokeCityB\",\"departureTime\":\"${DEPARTURE_TIME}\",\"arrivalTime\":\"${ARRIVAL_TIME}\",\"totalSeats\":40,\"price\":29.99}"

CREATE_BUS_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/buses" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -d "${BUS_PAYLOAD}" || echo "")
run_check "Bus Creation" '"success":true' "${CREATE_BUS_RESP}"
BUS_ID=$(echo "${CREATE_BUS_RESP}" | jq -r '.data.id // empty' 2>/dev/null || true)

# 8. Bus Search
echo "[Test 8] Search Bus by Source & Destination"
SEARCH_RESP=$(curl -s -m 5 "${BASE_URL}/api/buses/search?source=SmokeCityA&destination=SmokeCityB" || echo "")
run_check "Bus Search Finds Created Bus" "${BUS_NUM}" "${SEARCH_RESP}"

# 9. Book Seat (if BUS_ID available)
if [ -n "${BUS_ID}" ]; then
    echo "[Test 9] Seat Reservation (/api/bookings)"
    BOOK_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/bookings?userId=1&busId=${BUS_ID}&seatNumber=1" \
        -H "Authorization: Bearer ${USER_TOKEN}" || echo "")
    run_check "Seat Reservation Confirmed" '"status":"CONFIRMED"' "${BOOK_RESP}"
    BOOKING_ID=$(echo "${BOOK_RESP}" | jq -r '.data.id // empty' 2>/dev/null || true)

    # 10. Duplicate Seat Booking Prevention
    echo "[Test 10] Prevent Double Booking of Same Seat"
    DUP_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/bookings?userId=1&busId=${BUS_ID}&seatNumber=1" \
        -H "Authorization: Bearer ${USER_TOKEN}" || echo "")
    run_check "Double Booking Rejected" '"success":false' "${DUP_RESP}"

    # 11. Cancel Booking
    if [ -n "${BOOKING_ID}" ]; then
        echo "[Test 11] Cancel Reservation (/api/bookings/cancel/{id})"
        CANCEL_RESP=$(curl -s -m 5 -X PUT "${BASE_URL}/api/bookings/cancel/${BOOKING_ID}" \
            -H "Authorization: Bearer ${USER_TOKEN}" || echo "")
        run_check "Booking Cancellation" '"success":true' "${CANCEL_RESP}"

        # 12. Re-book Previously Cancelled Seat
        echo "[Test 12] Re-book Cancelled Seat"
        REBOOK_RESP=$(curl -s -m 5 -X POST "${BASE_URL}/api/bookings?userId=1&busId=${BUS_ID}&seatNumber=1" \
            -H "Authorization: Bearer ${USER_TOKEN}" || echo "")
        run_check "Re-booking Freed Seat" '"status":"CONFIRMED"' "${REBOOK_RESP}"
    fi
fi

echo "=========================================================="
echo " SMOKE TEST SUMMARY: Passed: ${PASSED} | Failed: ${FAILED}"
echo "=========================================================="

if [ "${FAILED}" -eq 0 ]; then
    exit 0
else
    exit 1
fi
