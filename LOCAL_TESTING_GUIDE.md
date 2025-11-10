# Local Testing Guide

This guide will help you test your LeetCode reminder workflow locally before adding it to GitHub Actions.

## 🚀 Quick Start Testing

### 1. Start Your Service
```bash
# In one terminal, start your Spring Boot service
./mvnw spring-boot:run
```

### 2. Run Basic Tests
```bash
# In another terminal, run basic endpoint tests
./simple-test.sh
```

### 3. Run Full Workflow Simulation
```bash
# Test the complete workflow logic
./test-workflow-locally.sh
```

## 🧪 Testing Levels

### Level 1: Basic Endpoint Testing
Test if your endpoints are working correctly:

```bash
# Health check
curl "http://localhost:8080/api/daily-question/health"

# User status check
curl "http://localhost:8080/api/daily-question/check-user-status/Abdul1028"

# AI solution generation
curl "http://localhost:8080/api/daily-question/ai-solution?language=python&includeCode=true"
```

### Level 2: Workflow Logic Testing
Test the complete workflow logic using our simulation script:

```bash
./test-workflow-locally.sh
```

This script will:
- ✅ Check if service is running
- ✅ Test user status endpoint
- ✅ Test AI solution endpoint
- ✅ Simulate morning reminder (6 AM)
- ✅ Simulate afternoon check (4 PM)
- ✅ Simulate evening reminder (11 PM)

### Level 3: Manual Workflow Testing
Test each scenario manually:

#### Scenario A: User Hasn't Solved Today's Question
1. **Check status**: `curl "http://localhost:8080/api/daily-question/check-user-status/Abdul1028"`
2. **Get AI solution**: `curl "http://localhost:8080/api/daily-question/ai-solution?language=python&includeCode=true"`
3. **Verify responses** contain expected data

#### Scenario B: User Has Solved Today's Question
1. **Check status**: `curl "http://localhost:8080/api/daily-question/check-user-status/Abdul1028"`
2. **Verify** `hasSolved` is `true`
3. **Check** submission details are present

## 🔧 Configuration for Testing

### Update Test Scripts
Edit the test scripts to match your setup:

```bash
# In test-workflow-locally.sh
LEETCODE_MS_BASE_URL="http://localhost:8080"
USERNAME="Abdul1028"  # Change to your username
DEFAULT_LANGUAGE="python"  # Change if you prefer another language
INCLUDE_CODE="true"  # Change to false if you want approach only
```

### Environment Variables
Make sure your local service has the required environment variables:

```bash
# Set your Gemini API key
export GEMINI_API_KEY="your_actual_api_key_here"

# Or create a .env file
echo "GEMINI_API_KEY=your_actual_api_key_here" > .env
```

## 📊 Expected Test Results

### Successful Test Output
```
🧪 Testing LeetCode Reminder Workflow Locally
==================================================

🚀 Starting local workflow test...

ℹ️  Checking if service is running...
✅ Service is running at http://localhost:8080

ℹ️  Testing user status endpoint...
✅ User status endpoint working
Response: {"username":"Abdul1028","todayQuestionTitle":"...",...}

ℹ️  Parsed values:
  - Has solved: false
  - Question: Two Sum

ℹ️  Testing AI solution endpoint...
✅ AI solution endpoint working
Solution preview: Here's a solution to the Two Sum problem...

✅ All endpoints working! Now simulating workflow scenarios...

ℹ️  Simulating morning reminder (6 AM scenario)...
ℹ️  User has NOT solved today's question. Sending reminder with solution...

📧 MORNING REMINDER MESSAGE:
=============================
Good morning! 🌅

It's time for your daily LeetCode challenge!

Today's Question: Two Sum

Here's your solution to get started:

```python
def twoSum(nums, target):
    # Solution code here...
```

Happy coding! 🚀 Remember, consistency is key to mastering algorithms!

----------------------------------------

ℹ️  Simulating afternoon check (4 PM scenario)...
📧 AFTERNOON REMINDER MESSAGE:
===============================
Good afternoon! ☀️

Just a friendly reminder that you haven't solved today's LeetCode question yet: Two Sum

Take a break from your work and give your brain a workout! 💡

You can do this! 🚀

----------------------------------------

ℹ️  Simulating evening reminder (11 PM scenario)...
📧 EVENING REMINDER MESSAGE:
=============================
Good evening! 🌙

⏰ Last call reminder! Today's LeetCode question is still waiting: Two Sum

Don't let today end without solving it! Even a quick 15-minute session counts towards your learning journey. 🚀

You've got this! 💪✨

🎉 Local workflow test completed successfully!

Next steps:
1. Deploy your service to a cloud platform
2. Set up Slack webhook
3. Configure GitHub secrets
4. Add the workflow to your repository
```

## 🚨 Troubleshooting Common Issues

### Issue 1: Service Not Running
```
❌ Service is not running at http://localhost:8080
ℹ️  Please start your service with: ./mvnw spring-boot:run
```

**Solution**: Start your Spring Boot service in another terminal

### Issue 2: API Key Missing
```
❌ Failed to get AI solution
```

**Solution**: Check if `GEMINI_API_KEY` is set in your environment

### Issue 3: Network Issues
```
❌ Failed to get user status
```

**Solution**: 
- Check if LeetCode is accessible from your network
- Verify firewall settings
- Check if you're behind a corporate proxy

### Issue 4: JSON Parsing Errors
```
ℹ️  Parsed values:
  - Has solved: 
  - Question: 
```

**Solution**: The response format might be different. Check the actual response and update the parsing logic.

## 🔍 Debug Mode

Enable debug logging in your Spring Boot application:

```properties
# In application.properties
logging.level.org.dailyquestionsms.service=DEBUG
logging.level.org.springframework.web.reactive.function.client=DEBUG
```

## 📝 Testing Checklist

Before moving to GitHub Actions, ensure:

- [ ] ✅ Service starts without errors
- [ ] ✅ Health endpoint responds
- [ ] ✅ User status endpoint works
- [ ] ✅ AI solution endpoint works
- [ ] ✅ All workflow scenarios simulate correctly
- [ ] ✅ Error handling works as expected
- [ ] ✅ Response parsing is accurate
- [ ] ✅ No sensitive data is logged

## 🚀 Next Steps After Successful Testing

1. **Deploy your service** to a cloud platform (Railway, Render, Heroku, etc.)
2. **Set up Slack webhook** in your workspace
3. **Configure GitHub secrets** with your deployed service URL
4. **Add the workflow** to your repository
5. **Test the actual GitHub Actions** workflow

## 💡 Pro Tips

1. **Test with different usernames** to ensure the endpoint works for various users
2. **Test with different languages** to verify AI solution generation
3. **Test error scenarios** by temporarily breaking your service
4. **Monitor response times** to ensure performance is acceptable
5. **Test with actual LeetCode data** to ensure real-world compatibility

---

**Happy testing! 🧪✨ Once everything works locally, you'll be ready for GitHub Actions!**
