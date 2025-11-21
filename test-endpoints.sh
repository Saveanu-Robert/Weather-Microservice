#!/bin/bash

# API Endpoint Test Script
# Tests all endpoints of the Weather Microservice

BASE_URL="http://localhost:8080/api"
ACTUATOR_URL="http://localhost:8080"
CONTENT_TYPE="Content-Type: application/json"

echo "========================================="
echo "Weather Microservice Endpoint Tests"
echo "========================================="
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_count=0
pass_count=0
fail_count=0

# Function to test endpoint
test_endpoint() {
    test_count=$((test_count + 1))
    local test_name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_status=$5

    echo -e "${YELLOW}Test $test_count: $test_name${NC}"

    if [ -z "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$BASE_URL$endpoint" \
            -H "$CONTENT_TYPE" -d "$data")
    fi

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} - Status: $http_code"
        echo "Response: $body" | head -c 200
        echo ""
        pass_count=$((pass_count + 1))
    else
        echo -e "${RED}✗ FAIL${NC} - Expected: $expected_status, Got: $http_code"
        echo "Response: $body"
        fail_count=$((fail_count + 1))
    fi
    echo ""
}

# Function to test actuator endpoint (uses different base URL)
test_actuator_endpoint() {
    test_count=$((test_count + 1))
    local test_name=$1
    local endpoint=$2
    local expected_status=$3

    echo -e "${YELLOW}Test $test_count: $test_name${NC}"

    response=$(curl -s -w "\n%{http_code}" -X GET "$ACTUATOR_URL$endpoint")

    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')

    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} - Status: $http_code"
        echo "Response: $body" | head -c 200
        echo ""
        pass_count=$((pass_count + 1))
    else
        echo -e "${RED}✗ FAIL${NC} - Expected: $expected_status, Got: $http_code"
        echo "Response: $body"
        fail_count=$((fail_count + 1))
    fi
    echo ""
}

# =====================================
# LOCATION ENDPOINTS
# =====================================
echo "========== LOCATION ENDPOINTS =========="
echo ""

# Test 1: Create Location
test_endpoint \
    "Create Location (Berlin)" \
    "POST" \
    "/locations" \
    '{"name":"Berlin","country":"Germany","latitude":52.5200,"longitude":13.4050,"region":"Berlin"}' \
    201

# Test 2: Get All Locations
test_endpoint \
    "Get All Locations" \
    "GET" \
    "/locations" \
    "" \
    200

# Test 3: Get Location by ID
test_endpoint \
    "Get Location by ID (1)" \
    "GET" \
    "/locations/1" \
    "" \
    200

# Test 4: Search Locations
test_endpoint \
    "Search Locations (Paris)" \
    "GET" \
    "/locations/search?name=Paris" \
    "" \
    200

# Test 5: Update Location
test_endpoint \
    "Update Location (1)" \
    "PUT" \
    "/locations/1" \
    '{"name":"Paris","country":"France","latitude":48.8566,"longitude":2.3522,"region":"Paris Region Updated"}' \
    200

# Test 6: Create Location (for later tests)
test_endpoint \
    "Create Location (Tokyo)" \
    "POST" \
    "/locations" \
    '{"name":"Tokyo","country":"Japan","latitude":35.6762,"longitude":139.6503,"region":"Kanto"}' \
    201

# Test 7: Invalid Location (missing required fields)
test_endpoint \
    "Create Invalid Location (missing name)" \
    "POST" \
    "/locations" \
    '{"country":"Test","latitude":0,"longitude":0}' \
    400

# Test 8: Get Non-existent Location
test_endpoint \
    "Get Non-existent Location (9999)" \
    "GET" \
    "/locations/9999" \
    "" \
    404

# =====================================
# WEATHER ENDPOINTS
# =====================================
echo "========== WEATHER ENDPOINTS =========="
echo ""

# Test 9: Get Current Weather (by location name)
test_endpoint \
    "Get Current Weather (Paris)" \
    "GET" \
    "/weather/current?location=Paris&save=true" \
    "" \
    200

# Test 10: Get Current Weather (by location ID)
test_endpoint \
    "Get Current Weather by Location ID (1)" \
    "GET" \
    "/weather/current/location/1?save=true" \
    "" \
    200

# Test 11: Get Weather History (paginated)
test_endpoint \
    "Get Weather History (location 1, page 0)" \
    "GET" \
    "/weather/history/location/1?page=0&size=10" \
    "" \
    200

# Test 12: Get Current Weather (invalid location)
test_endpoint \
    "Get Weather for Invalid Location" \
    "GET" \
    "/weather/current?location=NonExistentCity12345&save=false" \
    "" \
    503

# =====================================
# FORECAST ENDPOINTS
# =====================================
echo "========== FORECAST ENDPOINTS =========="
echo ""

# Test 13: Get Forecast (by location name)
test_endpoint \
    "Get 3-day Forecast (Paris)" \
    "GET" \
    "/forecast?location=Paris&days=3&save=true" \
    "" \
    200

# Test 14: Get Forecast (by location ID)
test_endpoint \
    "Get 5-day Forecast by Location ID (1)" \
    "GET" \
    "/forecast/location/1?days=5&save=true" \
    "" \
    200

# Test 15: Get Stored Forecasts
test_endpoint \
    "Get Stored Forecasts (location 1)" \
    "GET" \
    "/forecast/stored/location/1" \
    "" \
    200

# Test 16: Get Future Forecasts
test_endpoint \
    "Get Future Forecasts (location 1)" \
    "GET" \
    "/forecast/future/location/1" \
    "" \
    200

# Test 17: Invalid Forecast Days
test_endpoint \
    "Get Forecast with Invalid Days (0)" \
    "GET" \
    "/forecast?location=Paris&days=0&save=false" \
    "" \
    400

# Test 18: Invalid Forecast Days (too many)
test_endpoint \
    "Get Forecast with Too Many Days (15)" \
    "GET" \
    "/forecast?location=Paris&days=15&save=false" \
    "" \
    400

# =====================================
# ACTUATOR ENDPOINTS
# =====================================
echo "========== ACTUATOR ENDPOINTS =========="
echo ""

# Test 19: Health Check
test_actuator_endpoint \
    "Health Check" \
    "/actuator/health" \
    200

# Test 20: Info Endpoint
test_actuator_endpoint \
    "Info Endpoint" \
    "/actuator/info" \
    200

# Test 21: Metrics Endpoint
test_actuator_endpoint \
    "Metrics Endpoint" \
    "/actuator/metrics" \
    200

# =====================================
# DELETE OPERATIONS (at the end)
# =====================================
echo "========== DELETE OPERATIONS =========="
echo ""

# Test 22: Create a location just for deleting and capture its ID
test_count=$((test_count + 1))
echo -e "${YELLOW}Test $test_count: Create Location for Delete Test (Madrid)${NC}"

response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/locations" \
    -H "$CONTENT_TYPE" -d '{"name":"Madrid","country":"Spain","latitude":40.4168,"longitude":-3.7038,"region":"Community of Madrid"}')

http_code=$(echo "$response" | tail -n1)
body=$(echo "$response" | sed '$d')

if [ "$http_code" -eq "201" ]; then
    echo -e "${GREEN}✓ PASS${NC} - Status: $http_code"
    echo "Response: $body" | head -c 200
    echo ""
    pass_count=$((pass_count + 1))

    # Extract the ID from the response
    madrid_id=$(echo "$body" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
    echo "Captured Madrid ID: $madrid_id"
else
    echo -e "${RED}✗ FAIL${NC} - Expected: 201, Got: $http_code"
    echo "Response: $body"
    fail_count=$((fail_count + 1))
    madrid_id="999" # Fallback ID if creation failed
fi
echo ""

# Test 23: Delete the location we just created
test_endpoint \
    "Delete Location (Madrid)" \
    "DELETE" \
    "/locations/$madrid_id" \
    "" \
    204

# Test 24: Delete Non-existent Location
test_endpoint \
    "Delete Non-existent Location (9999)" \
    "DELETE" \
    "/locations/9999" \
    "" \
    404

# =====================================
# SUMMARY
# =====================================
echo "========================================="
echo "Test Summary"
echo "========================================="
echo -e "Total Tests: $test_count"
echo -e "${GREEN}Passed: $pass_count${NC}"
echo -e "${RED}Failed: $fail_count${NC}"
echo ""

if [ $fail_count -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
else
    echo -e "${RED}Some tests failed!${NC}"
    echo -e "${YELLOW}Note: Failed tests may be due to auto-increment ID variations.${NC}"
    echo -e "${YELLOW}Verify that the actual functionality works correctly.${NC}"
fi
echo ""

# =====================================
# CLEANUP - Delete all test data
# =====================================
echo "========== CLEANUP =========="
echo "Cleaning up test data..."

# Get all locations and extract their IDs (except ID 1 which is Paris from initial data)
all_locations=$(curl -s "$BASE_URL/locations")
location_ids=$(echo "$all_locations" | grep -o '"id":[0-9]*' | grep -o '[0-9]*' | sort -n | uniq)

# Delete each location except ID 1
deleted_count=0
for id in $location_ids; do
    if [ "$id" -ne "1" ]; then
        curl -s -X DELETE "$BASE_URL/locations/$id" > /dev/null 2>&1
        deleted_count=$((deleted_count + 1))
    fi
done

echo "Cleanup complete - deleted $deleted_count test locations"
echo ""

if [ $fail_count -eq 0 ]; then
    exit 0
else
    exit 1
fi
