#!/bin/bash

# Local Test Script for LeetCode Reminder Workflow
# This simulates what the GitHub Actions workflow will do

set -e  # Exit on any error

# Configuration - can be overridden via environment variables (useful for CI)
LEETCODE_MS_BASE_URL="${LEETCODE_MS_BASE_URL:-https://leetcode-reminder-ms-19a5a910d3d2.herokuapp.com}"
USERNAME="${USERNAME:-Abdul1028}"
DEFAULT_LANGUAGE="${DEFAULT_LANGUAGE:-python}"
INCLUDE_CODE="${INCLUDE_CODE:-true}"
# MODE controls what to run: all|morning|afternoon|evening|status|ai|health
MODE="${MODE:-all}"
# Slack Incoming Webhook URL (set as secret in CI). If empty, Slack is skipped.
SLACK_WEBHOOK_URL="${SLACK_WEBHOOK_URL:-}"

# Quick local test (replace webhook URL):
# cd "/Users/abdul/Desktop/Own Work/Leetcode-Reminder/Daily-Questions-MS"
# chmod +x test-workflow-locally.sh
# SLACK_WEBHOOK_URL='https://hooks.slack.com/services/XXX' \
# LEETCODE_MS_BASE_URL='https://leetcode-reminder-ms-19a5a910d3d2.herokuapp.com' \
# USERNAME='Abdul1028' DEFAULT_LANGUAGE='python' INCLUDE_CODE='true' \
# MODE=morning NON_INTERACTIVE=true ./test-workflow-locally.sh
# Non-interactive mode (auto-enabled in CI)
if [ -n "$CI" ] || [ -n "$GITHUB_ACTIONS" ] || [ "${NON_INTERACTIVE:-false}" = "true" ]; then
    NON_INTERACTIVE=true
else
    NON_INTERACTIVE=false
fi

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🧪 Testing LeetCode Reminder Workflow Locally${NC}"
echo "=================================================="
echo ""

# Check if jq is available for better JSON parsing
if command -v jq &> /dev/null; then
    echo -e "${BLUE}ℹ️  jq found - will use proper JSON parsing${NC}"
else
    echo -e "${YELLOW}⚠️  jq not found - using basic parsing. Install jq for better results:${NC}"
    echo -e "${BLUE}ℹ️    macOS: brew install jq${NC}"
    echo -e "${BLUE}ℹ️    Ubuntu: sudo apt-get install jq${NC}"
    echo -e "${BLUE}ℹ️    CentOS: sudo yum install jq${NC}"
fi
echo ""

# Function to print colored output
print_status() {
    local status=$1
    local message=$2
    case $status in
        "INFO") echo -e "${BLUE}ℹ️  $message${NC}" ;;
        "SUCCESS") echo -e "${GREEN}✅ $message${NC}" ;;
        "WARNING") echo -e "${YELLOW}⚠️  $message${NC}" ;;
        "ERROR") echo -e "${RED}❌ $message${NC}" ;;
    esac
}

# JSON escape helper using python3 (fallback to basic sed if python unavailable)
json_escape() {
    if command -v python3 >/dev/null 2>&1; then
        python3 - <<'PY'
import json,sys
text=sys.stdin.read()
print(json.dumps(text)[1:-1])
PY
    else
        # rudimentary escape; recommended to have python3 on CI
        sed 's/\\/\\\\/g; s/"/\\"/g; s/\t/\\t/g; s/\r/\\r/g; s/\n/\\n/g'
    fi
}

# Send Slack message using blocks with optional solution code block
send_slack_message() {
    local title=$1
    local message=$2
    local question_title=$3
    local solution_content=$4
    local language=$5

    if [ -z "$SLACK_WEBHOOK_URL" ]; then
        print_status "WARNING" "SLACK_WEBHOOK_URL not set — skipping Slack notification"
        return 0
    fi

    local esc_title=$(printf "%s" "$title" | json_escape)
    local esc_message=$(printf "%s" "$message" | json_escape)
    local esc_qtitle=$(printf "%s" "$question_title" | json_escape)

    # Build base blocks (use double quotes to allow apostrophes safely)
    local blocks_start="{\"type\":\"header\",\"text\":{\"type\":\"plain_text\",\"text\":\"${esc_title}\"}}"
    blocks_start+=",{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"${esc_message}\"}}"
    blocks_start+=",{\"type\":\"section\",\"text\":{\"type\":\"mrkdwn\",\"text\":\"*Today's Question:* ${esc_qtitle}\"}}"
    blocks_start+=",{\"type\":\"divider\"}"

    local blocks=$blocks_start

    if [ -n "$solution_content" ]; then
        # Compose code block with language hint
        local code_block="\`\`\`$language\n$solution_content\n\`\`\`"
        local esc_code=$(printf "%s" "$code_block" | json_escape)
        blocks+=',{"type":"section","text":{"type":"mrkdwn","text":"'"${esc_code}"'"}}'
    fi

    local payload='{'"\"text\":\"LeetCode Reminder: ${esc_title}\",\"blocks\":[${blocks}]"'}'

    if [ "${DEBUG_SLACK:-false}" = "true" ]; then
        echo "SLACK PAYLOAD: $payload"
    fi

    local slack_response
    slack_response=$(curl -s -X POST -H 'Content-type: application/json' --data "$payload" "$SLACK_WEBHOOK_URL") || {
        print_status "ERROR" "Failed to reach Slack webhook"
        return 1
    }

    if [ "$slack_response" != "ok" ]; then
        print_status "ERROR" "Slack returned non-ok response: $slack_response"
        return 1
    fi

    print_status "SUCCESS" "Sent Slack notification"
}

# Function to check if service is running
check_service() {
    print_status "INFO" "Checking if service is running..."

    if curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/health" > /dev/null; then
        print_status "SUCCESS" "Service is running at $LEETCODE_MS_BASE_URL"
        return 0
    else
        print_status "ERROR" "Service is not running at $LEETCODE_MS_BASE_URL"
        print_status "INFO" "Please start your service with: ./mvnw spring-boot:run"
        return 1
    fi
}

# Function to test user status endpoint
test_user_status() {
    print_status "INFO" "Testing user status endpoint..."

    local status_response=$(curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/check-user-status/$USERNAME")

    if [ $? -eq 0 ]; then
        print_status "SUCCESS" "User status endpoint working"
        echo "Response: $status_response"

        # Extract hasSolved value
        local has_solved=$(echo "$status_response" | grep -o '"hasSolved":[^,]*' | cut -d':' -f2 | tr -d ' ')
        local question_title=$(echo "$status_response" | grep -o '"todayQuestionTitle":"[^"]*"' | cut -d'"' -f4)

        echo ""
        print_status "INFO" "Parsed values:"
        echo "  - Has solved: $has_solved"
        echo "  - Question: $question_title"

        return 0
    else
        print_status "ERROR" "Failed to get user status"
        return 1
    fi
}

# Function to test AI solution endpoint
test_ai_solution() {
    print_status "INFO" "Testing AI solution endpoint..."

    local solution_response=$(curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/ai-solution?language=$DEFAULT_LANGUAGE&includeCode=$INCLUDE_CODE")

    if [ $? -eq 0 ]; then
        print_status "SUCCESS" "AI solution endpoint working"

        # Extract full solution content - handle JSON properly
        local solution=""
        if command -v jq &> /dev/null; then
            # Use jq if available for proper JSON parsing
            solution=$(echo "$solution_response" | jq -r '.solution // empty')
        else
            # Fallback to basic parsing - extract everything between "solution":" and the next quote
            solution=$(echo "$solution_response" | sed -n 's/.*"solution":"\([^"]*\)".*/\1/p' | sed 's/\\n/\n/g' | sed 's/\\"/"/g' | sed 's/\\t/\t/g')
        fi
        echo "Solution received (${#solution} characters)"

        # Show solution summary
        local line_count=$(echo "$solution" | wc -l)
        local word_count=$(echo "$solution" | wc -w)
        echo "  - Lines: $line_count"
        echo "  - Words: $word_count"
        echo "  - Language: $DEFAULT_LANGUAGE"

        # Only prompt in interactive mode
        if [ "$NON_INTERACTIVE" != "true" ]; then
            echo ""
            read -p "Would you like to see the full solution? (y/n): " -n 1 -r
            echo ""
            if [[ $REPLY =~ ^[Yy]$ ]]; then
               echo ""
               echo "📝 FULL SOLUTION:"
               echo "================="
               printf '```%s\n' "$DEFAULT_LANGUAGE"
               printf '%s\n' "$solution"
               echo '```'
               echo ""
            fi
        fi

        return 0
    else
        print_status "ERROR" "Failed to get AI solution"
        return 1
    fi
}

# Function to simulate morning reminder
simulate_morning_reminder() {
    print_status "INFO" "Simulating morning reminder (6 AM scenario)..."

    local status_response=$(curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/check-user-status/$USERNAME")
    local has_solved=$(echo "$status_response" | grep -o '"hasSolved":[^,]*' | cut -d':' -f2 | tr -d ' ')
    local question_title=$(echo "$status_response" | grep -o '"todayQuestionTitle":"[^"]*"' | cut -d'"' -f4)

    if [ "$has_solved" = "false" ]; then
        print_status "INFO" "User has NOT solved today's question. Sending reminder with solution..."

        # Get AI solution
        local solution_response=$(curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/ai-solution?language=$DEFAULT_LANGUAGE&includeCode=$INCLUDE_CODE")
        local solution=""
        if command -v jq &> /dev/null; then
            # Use jq if available for proper JSON parsing
            solution=$(echo "$solution_response" | jq -r '.solution // empty')
        else
            # Fallback to basic parsing - extract everything between "solution":" and the next quote
            solution=$(echo "$solution_response" | sed -n 's/.*"solution":"\([^"]*\)".*/\1/p' | sed 's/\\n/\n/g' | sed 's/\\"/"/g' | sed 's/\\t/\t/g')
        fi

        echo ""
        echo "📧 MORNING REMINDER MESSAGE:"
        echo "============================="
        echo "Good morning! 🌅"
        echo ""
        echo "It's time for your daily LeetCode challenge!"
        echo ""
        echo "Today's Question: $question_title"
        echo ""
        echo "Here's your complete solution to get started:"
        echo ""
        echo '```'"$DEFAULT_LANGUAGE"
        # Format the solution for better readability
        printf "%s\n" "$solution" | sed 's/^/  /'  # Add indentation for better formatting
        echo '```'
        echo ""
        echo "Happy coding! 🚀 Remember, consistency is key to mastering algorithms!"

        # Send Slack message
        local msg_title="Morning Reminder"
        local msg_intro="Good morning! It\'s time for your daily LeetCode challenge. Here\'s a complete solution to help you get started."
        send_slack_message "$msg_title" "$msg_intro" "$question_title" "$solution" "$DEFAULT_LANGUAGE"

    else
        print_status "INFO" "User has already solved today's question. Sending congratulatory message..."

        echo ""
        echo "📧 MORNING COMPLETION MESSAGE:"
        echo "==============================="
        echo "Good morning! 🌅"
        echo ""
        echo "🎉 Congratulations! You've already solved today's LeetCode question: $question_title"
        echo ""
        echo "That's the spirit! Starting your day with problem-solving shows great dedication. Keep up the excellent work! 💪✨"

        # Send Slack message
        local msg_title="Morning Completed"
        local msg_intro="Great job! You\'ve already solved today\'s question. Keep up the momentum!"
        send_slack_message "$msg_title" "$msg_intro" "$question_title" "" "$DEFAULT_LANGUAGE"
    fi
}

# Function to simulate afternoon check
simulate_afternoon_check() {
    print_status "INFO" "Simulating afternoon check (4 PM scenario)..."

    local status_response=$(curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/check-user-status/$USERNAME")
    local has_solved=$(echo "$status_response" | grep -o '"hasSolved":[^,]*' | cut -d':' -f2 | tr -d ' ')
    local question_title=$(echo "$status_response" | grep -o '"todayQuestionTitle":"[^"]*"' | cut -d'"' -f4)

    if [ "$has_solved" = "false" ]; then
        echo ""
        echo "📧 AFTERNOON REMINDER MESSAGE:"
        echo "==============================="
        echo "Good afternoon! ☀️"
        echo ""
        echo "Just a friendly reminder that you haven't solved today's LeetCode question yet: $question_title"
        echo ""
        echo "Take a break from your work and give your brain a workout! 💡"
        echo ""
        echo "You can do this! 🚀"

        # Send Slack message
        local msg_title="Afternoon Reminder"
        local msg_intro="Gentle nudge — you haven\'t solved today\'s question yet. Take a short break and give it a try!"
        send_slack_message "$msg_title" "$msg_intro" "$question_title" "" "$DEFAULT_LANGUAGE"
    else
        echo ""
        echo "📧 AFTERNOON COMPLETION MESSAGE:"
        echo "================================="
        echo "Good afternoon! ☀️"
        echo ""
        echo "🎯 Great job! You've completed today's LeetCode challenge: $question_title"
        echo ""
        echo "Your problem-solving skills are getting stronger every day! Keep up the momentum! 💪✨"

        # Send Slack message
        local msg_title="Afternoon Completed"
        local msg_intro="Nicely done! You\'ve completed today\'s challenge."
        send_slack_message "$msg_title" "$msg_intro" "$question_title" "" "$DEFAULT_LANGUAGE"
    fi
}

# Function to simulate evening reminder
simulate_evening_reminder() {
    print_status "INFO" "Simulating evening reminder (11 PM scenario)..."

    local status_response=$(curl -s "$LEETCODE_MS_BASE_URL/api/daily-question/check-user-status/$USERNAME")
    local has_solved=$(echo "$status_response" | grep -o '"hasSolved":[^,]*' | cut -d':' -f2 | tr -d ' ')
    local question_title=$(echo "$status_response" | grep -o '"todayQuestionTitle":"[^"]*"' | cut -d'"' -f4)

    if [ "$has_solved" = "false" ]; then
        echo ""
        echo "📧 EVENING REMINDER MESSAGE:"
        echo "============================="
        echo "Good evening! 🌙"
        echo ""
        echo "⏰ Last call reminder! Today's LeetCode question is still waiting: $question_title"
        echo ""
        echo "Don't let today end without solving it! Even a quick 15-minute session counts towards your learning journey. 🚀"
        echo ""
        echo "You've got this! 💪✨"

        # Send Slack message
        local msg_title="Evening Reminder"
        local msg_intro="Last call for today\'s LeetCode! A short focused session still counts."
        send_slack_message "$msg_title" "$msg_intro" "$question_title" "" "$DEFAULT_LANGUAGE"
    else
        echo ""
        echo "📧 EVENING COMPLETION MESSAGE:"
        echo "================================="
        echo "Good evening! 🌙"
        echo ""
        echo "🎉 Fantastic! You've conquered today's LeetCode challenge: $question_title"
        echo ""
        echo "What a great way to end your day! Your dedication to continuous learning is inspiring. 🌟"
        echo ""
        echo "Rest well and get ready for tomorrow's challenge! 💪✨"

        # Send Slack message
        local msg_title="Evening Completed"
        local msg_intro="Fantastic finish! Today\'s challenge is complete."
        send_slack_message "$msg_title" "$msg_intro" "$question_title" "" "$DEFAULT_LANGUAGE"
    fi
}

# Main test execution
main() {
    echo -e "${BLUE}🚀 Starting workflow run (mode: ${MODE})...${NC}"
    echo ""

    # Check if service is running
    if ! check_service; then
        exit 1
    fi

    echo ""

    case "$MODE" in
        health)
            print_status "SUCCESS" "Health check passed"
            ;;
        status)
            if ! test_user_status; then exit 1; fi
            ;;
        ai)
            if ! test_ai_solution; then exit 1; fi
            ;;
        morning)
            if ! test_user_status; then exit 1; fi
            if ! test_ai_solution; then exit 1; fi
            echo ""
            simulate_morning_reminder
            ;;
        afternoon)
            if ! test_user_status; then exit 1; fi
            echo ""
            simulate_afternoon_check
            ;;
        evening)
            if ! test_user_status; then exit 1; fi
            echo ""
            simulate_evening_reminder
            ;;
        all|*)
            # Test endpoints
            if ! test_user_status; then exit 1; fi
            echo ""
            if ! test_ai_solution; then exit 1; fi
            echo ""
            echo -e "${GREEN}✅ All endpoints working! Now simulating workflow scenarios...${NC}"
            echo ""
            simulate_morning_reminder
            echo ""
            echo "----------------------------------------"
            echo ""
            simulate_afternoon_check
            echo ""
            echo "----------------------------------------"
            echo ""
            simulate_evening_reminder
            ;;
    esac

    echo ""
    echo -e "${GREEN}🎉 Workflow run completed successfully!${NC}"
}

# Run the main function
main

