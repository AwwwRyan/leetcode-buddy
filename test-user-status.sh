#!/bin/bash

# Test script for the new user status endpoint
# This script demonstrates how to check if a user has solved today's daily question

echo "Testing User Daily Question Status Endpoint"
echo "=========================================="

# Base URL for the API
BASE_URL="http://localhost:8080/api/daily-question"

# Test username (you can change this to test with different users)
USERNAME="Abdul1028"

echo "Checking if user '$USERNAME' has solved today's daily question..."
echo "Endpoint: $BASE_URL/check-user-status/$USERNAME"
echo ""

# Make the API call
curl -s "$BASE_URL/check-user-status/$USERNAME" | jq '.'

echo ""
echo "Response Explanation:"
echo "- hasSolved: true means user has solved today's question"
echo "- hasSolved: false means user has not solved today's question yet"
echo "- submissionLanguage: shows the programming language used (if solved)"
echo "- submissionTimestamp: shows when the user solved it (if solved)"
echo "- message: provides a human-readable status message"
