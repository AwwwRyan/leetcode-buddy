#!/bin/bash

# Simple Test Script for Quick Testing
# Use this for basic endpoint testing

echo "🧪 Simple Local Testing"
echo "======================="
echo ""

# Check if service is running
echo "1. Checking if service is running..."
if curl -s "http://localhost:8080/api/daily-question/health" > /dev/null; then
    echo "✅ Service is running"
else
    echo "❌ Service is not running. Please start with: ./mvnw spring-boot:run"
    exit 1
fi

echo ""

# Test user status endpoint
echo "2. Testing user status endpoint..."
STATUS_RESPONSE=$(curl -s "http://localhost:8080/api/daily-question/check-user-status/Abdul1028")
echo "Response: $STATUS_RESPONSE"

echo ""

# Test AI solution endpoint
echo "3. Testing AI solution endpoint..."
SOLUTION_RESPONSE=$(curl -s "http://localhost:8080/api/daily-question/ai-solution?language=python&includeCode=true")
echo "Response length: ${#SOLUTION_RESPONSE} characters"

echo ""
echo "🎉 Basic testing completed!"
echo ""
echo "To run full workflow simulation:"
echo "./test-workflow-locally.sh"
