# Daily Questions MS - LeetCode AI Solution Generator

A Spring Boot microservice that fetches daily LeetCode problems and generates AI-powered solutions using Google's Gemini API.

## Features

- 🎯 **Daily LeetCode Problems**: Automatically fetch today's LeetCode problem
- 🤖 **AI-Powered Solutions**: Generate solutions using Google Gemini AI
- 🌍 **Multi-Language Support**: Solutions in Python, Java, C++, JavaScript, and more
- 📚 **Problem Details**: Complete problem descriptions, hints, and examples
- 🔍 **Flexible Queries**: Get solutions for specific problems or daily challenges

## Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- Google Gemini API key

### Configuration

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Daily-Questions-MS
   ```

2. **Configure Gemini API Key**
   Edit `src/main/resources/application.properties`:
   ```properties
   gemini.api.key=YOUR_ACTUAL_GEMINI_API_KEY_HERE
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The service will start on `http://localhost:8080`

## API Endpoints

### Basic LeetCode Operations

#### Get Daily Question
```http
GET /api/daily-question
```
Returns today's LeetCode problem with basic information.

#### Get Question Details
```http
GET /api/daily-question/detail/{titleSlug}
```
Returns complete problem details including content, hints, and code snippets.

#### Get Daily Question with Full Details
```http
GET /api/daily-question/daily-with-detail
```
Automatically fetches today's problem and its complete details.

#### Get Daily Question with Language-Specific Code
```http
GET /api/daily-question/daily-with-detail/{language}
```
Returns daily problem with code snippets for a specific language (e.g., `java`, `python`, `cpp`).

### AI-Powered Solution Generation

#### Generate AI Solution for Daily Problem
```http
GET /api/daily-question/ai-solution?language=python&includeCode=true
```

**Parameters:**
- `language` (optional): Programming language for the solution (default: `python`)
- `includeCode` (optional): Whether to include code (default: `true`)

**Supported Languages:**
- `python`, `java`, `cpp`, `javascript`, `typescript`, `csharp`, `go`, `rust`, `swift`, `kotlin`, `php`, `ruby`, `scala`, `dart`, `elixir`, `erlang`, `racket`

#### Generate AI Solution for Specific Problem
```http
GET /api/daily-question/ai-solution/{titleSlug}?language=java&includeCode=false
```

**Parameters:**
- `titleSlug`: The problem's title slug (e.g., `two-sum`)
- `language` (optional): Programming language (default: `python`)
- `includeCode` (optional): Whether to include code (default: `true`)

### Utility Endpoints

#### Health Check
```http
GET /api/daily-question/health
```

#### Test LeetCode Connection
```http
GET /api/daily-question/test-leetcode
```

#### Check User Daily Question Status
```http
GET /api/daily-question/check-user-status/{username}
```
Returns whether a specific user has solved today's daily question.

**Parameters:**
- `username`: LeetCode username to check

**Response:**
```json
{
  "username": "Abdul1028",
  "todayQuestionTitle": "Today's Question Title",
  "todayQuestionTitleSlug": "todays-question-title",
  "hasSolved": true,
  "message": "User has successfully solved today's daily question!",
  "submissionLanguage": "python",
  "submissionTimestamp": "1234567890"
}
```

**Use Cases:**
- Check if a user has completed today's challenge
- Track user progress on daily questions
- Build reminder systems for incomplete daily challenges

## Usage Examples

### 1. Get Today's Problem with AI Solution in Python
```bash
curl "http://localhost:8080/api/daily-question/ai-solution?language=python&includeCode=true"
```

### 2. Get Problem-Solving Approach (No Code)
```bash
curl "http://localhost:8080/api/daily-question/ai-solution?language=java&includeCode=false"
```

### 3. Get AI Solution for Specific Problem
```bash
curl "http://localhost:8080/api/daily-question/ai-solution/two-sum?language=cpp&includeCode=true"
```

### 4. Get Daily Problem with Java Code Snippets
```bash
curl "http://localhost:8080/api/daily-question/daily-with-detail/java"
```

### 5. Check if User Has Solved Today's Question
```bash
curl "http://localhost:8080/api/daily-question/check-user-status/Abdul1028"
```

## AI Solution Types

### Enhanced Code Solutions (`includeCode=true`)
When requesting code solutions, the AI will provide:
- **Complete, production-ready code** in the specified language
- **Driver code/template inclusion** for proper context
- **Clear explanation of the approach** and why it's optimal
- **Detailed time and space complexity analysis**
- **Constraint handling strategies** to avoid TLE errors
- **Edge case handling** for boundary conditions
- **Performance optimizations** and best practices
- **Code comments and explanations** for key parts

### Enhanced Problem-Solving Approaches (`includeCode=false`)
When requesting approaches only, the AI will provide:
- **Step-by-step algorithm explanation** with complexity analysis
- **Key insights and observations** that lead to optimal solutions
- **Detailed time and space complexity analysis**
- **Alternative approaches and their trade-offs**
- **Common pitfalls and how to avoid them**
- **Specific strategies for handling constraints** mentioned in the problem
- **Edge cases to consider** and how to handle them
- **Performance optimization strategies** to prevent TLE errors

### 🚀 Enhanced Features for Better Solutions

The AI solution generation now includes several improvements to ensure high-quality, production-ready solutions:

#### 1. **Driver Code Integration**
- Automatically includes the language-specific template code
- Provides proper function signatures and class structures
- Ensures solutions can be directly submitted to LeetCode

#### 2. **Constraint Extraction & Highlighting**
- Automatically identifies and highlights problem constraints
- Extracts input size limits, time complexity requirements
- Highlights edge cases and boundary conditions

#### 3. **Performance Optimization Guidelines**
- Explicit instructions to avoid Time Limit Exceeded (TLE) errors
- Emphasis on efficient data structures and algorithms
- Worst-case complexity analysis and optimization strategies

#### 4. **Enhanced Content Parsing**
- Better preservation of HTML formatting and structure
- Improved constraint and requirement identification
- Better handling of mathematical expressions and comparisons

#### 5. **Quality Validation**
- Solution completeness checking
- Component validation (complexity analysis, explanations, code)
- Logging and monitoring for solution quality

## Response Format

### AI Solution Response
```json
{
  "question": "Problem Title",
  "language": "python",
  "solutionType": "code",
  "solution": "AI-generated solution text..."
}
```

### Problem Details Response
```json
{
  "questionId": "123",
  "title": "Problem Title",
  "difficulty": "Medium",
  "content": "Problem description...",
  "hints": ["Hint 1", "Hint 2"],
  "codeSnippets": [...],
  "sampleTestCase": "input example",
  "metaData": "function signature"
}
```

## Error Handling

The service includes comprehensive error handling:
- HTTP status codes for different error types
- Detailed error messages in responses
- Logging for debugging and monitoring
- Graceful fallbacks for failed requests

## Configuration Options

### Application Properties
```properties
# Server
server.port=8080

# Logging
logging.level.org.dailyquestionsms.service=DEBUG

# Gemini API
gemini.api.key=YOUR_API_KEY

# WebClient
spring.webflux.webclient.max-in-memory-size=10MB
```

## Development

### Project Structure
```
src/main/java/org/dailyquestionsms/
├── controller/
│   └── DailyQuestionController.java
├── service/
│   ├── LeetCodeService.java
│   └── GeminiService.java
└── model/
    ├── LeetCodeResponse.java
    ├── QuestionDetailResponse.java
    ├── GeminiRequest.java
    ├── GeminiResponse.java
    ├── UserSubmissionResponse.java
    └── UserQuestionStatusResponse.java
```

### Adding New Languages
To support additional programming languages:
1. Update the `buildPrompt` method in `GeminiService.java`
2. Add language-specific instructions
3. Test with the new language parameter

## Troubleshooting

### Common Issues

1. **499 Status Code**: Usually indicates network or proxy issues
   - Check your internet connection
   - Verify firewall settings
   - Try increasing timeout values

2. **Gemini API Errors**: 
   - Verify your API key is correct
   - Check API quota and limits
   - Ensure the API key has proper permissions

3. **LeetCode Connection Issues**:
   - LeetCode might be blocking automated requests
   - Try the health check endpoint first
   - Check if LeetCode is accessible from your network

### Debug Mode
Enable debug logging by setting:
```properties
logging.level.org.dailyquestionsms.service=DEBUG
logging.level.org.springframework.web.reactive.function.client=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Support

For issues and questions:
- Check the troubleshooting section
- Review the logs for error details
- Open an issue on the repository
