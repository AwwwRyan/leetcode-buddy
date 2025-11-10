#!/bin/bash

# Test script for Daily Questions MS AI endpoints
# Make sure the service is running on localhost:8080

BASE_URL="http://localhost:8080/api/daily-question"

echo "🧪 Testing Daily Questions MS AI Endpoints"
echo "=========================================="
echo ""
echo "🚀 Enhanced AI Solution Generation Features:"
echo "   ✓ Driver code/template inclusion"
echo "   ✓ Constraint extraction and highlighting"
echo "   ✓ Performance optimization requirements"
echo "   ✓ TLE prevention guidelines"
echo "   ✓ Enhanced content parsing"
echo ""

# Test 1: Health check
echo "1️⃣ Testing health endpoint..."
curl -s "$BASE_URL/health" | jq .
echo ""

# Test 2: Get daily question
echo "2️⃣ Testing daily question endpoint..."
curl -s "$BASE_URL" | jq .
echo ""

# Test 3: Get AI solution for daily problem in Python (with code)
echo "3️⃣ Testing AI solution generation (Python with code)..."
echo "   This will now include driver code, constraints, and performance requirements!"
curl -s "$BASE_URL/ai-solution?language=python&includeCode=true" | jq .
echo ""

# Test 4: Get AI solution for daily problem in Java (approach only)
echo "4️⃣ Testing AI solution generation (Java approach only)..."
echo "   This will include detailed approach with constraint handling strategies!"
curl -s "$BASE_URL/ai-solution?language=java&includeCode=false" | jq .
echo ""

# Test 5: Get daily question with Java code snippets
echo "5️⃣ Testing daily question with Java code snippets..."
curl -s "$BASE_URL/daily-with-detail/java" | jq .
echo ""

# Test 6: Get AI solution for specific problem (if you know a titleSlug)
echo "6️⃣ Testing AI solution for specific problem..."
echo "Note: Replace 'two-sum' with an actual problem titleSlug if available"
curl -s "$BASE_URL/ai-solution/two-sum?language=cpp&includeCode=true" | jq .
echo ""

echo "✅ Testing completed!"
echo ""
echo "📚 Available endpoints:"
echo "   - GET $BASE_URL/health"
echo "   - GET $BASE_URL"
echo "   - GET $BASE_URL/daily-with-detail"
echo "   - GET $BASE_URL/daily-with-detail/{language}"
echo "   - GET $BASE_URL/ai-solution?language={lang}&includeCode={bool}"
echo "   - GET $BASE_URL/ai-solution/{titleSlug}?language={lang}&includeCode={bool}"
echo ""
echo "🔧 Parameters:"
echo "   - language: python, java, cpp, javascript, typescript, csharp, go, rust, swift, kotlin, php, ruby, scala, dart, elixir, erlang, racket"
echo "   - includeCode: true (with code) or false (approach only)"
echo ""
echo "💡 Example: Get Python solution with code:"
echo "   curl \"$BASE_URL/ai-solution?language=python&includeCode=true\""
echo ""
echo "🎯 New Features:"
echo "   - Driver code inclusion for better context"
echo "   - Constraint extraction and highlighting"
echo "   - Performance optimization guidelines"
echo "   - TLE prevention strategies"
echo "   - Enhanced content parsing with HTML preservation"
