package org.dailyquestionsms.service;

import java.time.Duration;
import java.util.Arrays;

import org.dailyquestionsms.model.GeminiRequest;
import org.dailyquestionsms.model.GeminiResponse;
import org.dailyquestionsms.model.QuestionDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final WebClient webClient;
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    // @Value("${gemini.api.key:}")
    private String geminiApiKey = "AIzaSyAnIkGKpuFFBAmRMYDctkHuJa03HodUSD4";

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl(GEMINI_API_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    public Mono<String> generateSolution(QuestionDetailResponse.QuestionDetail question, String language,
            boolean includeCode) {
        String prompt = buildPrompt(question, language, includeCode);

        // Log the generated prompt for debugging
        logger.info("Generated prompt for question: {} (language: {}, includeCode: {})",
                question.getTitle(), language, includeCode);
        logger.debug("Full prompt: {}", prompt);

        GeminiRequest request = new GeminiRequest(Arrays.asList(
                new GeminiRequest.Content(Arrays.asList(
                        new GeminiRequest.Part(prompt)))));

        logger.info("Generating {} solution for question: {} in language: {}",
                includeCode ? "code" : "approach", question.getTitle(), language);

        return webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("key", geminiApiKey)
                        .build())
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> {
                            logger.error("Gemini API HTTP Error: {} - {}", response.statusCode(),
                                    response.statusCode().value());
                            return response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        logger.error("Gemini API error response body: {}", body);
                                        return Mono.error(new RuntimeException(
                                                "Gemini API HTTP " + response.statusCode() + ": " + body));
                                    })
                                    .defaultIfEmpty("")
                                    .flatMap(body -> {
                                        if (body == null || body.toString().isEmpty()) {
                                            return Mono.error(new RuntimeException(
                                                    "Gemini API HTTP " + response.statusCode() + ": No response body"));
                                        }
                                        return Mono.error(new RuntimeException(
                                                "Gemini API HTTP " + response.statusCode() + ": " + body));
                                    });
                        })
                .bodyToMono(GeminiResponse.class)
                .timeout(Duration.ofSeconds(60))
                .map(this::extractResponseText)
                .map(this::validateAndEnhanceSolution)
                .doOnSuccess(response -> logger.info("Successfully generated {} solution",
                        includeCode ? "code" : "approach"))
                .doOnError(error -> logger.error("Error generating solution: {}", error.getMessage()));
    }

    private String validateAndEnhanceSolution(String solution) {
        if (solution == null || solution.trim().isEmpty()) {
            return "Error: No solution generated from AI service.";
        }

        // Log the solution length for monitoring
        logger.info("Generated solution length: {} characters", solution.length());

        // Check if the solution contains key components
        String lowerSolution = solution.toLowerCase();
        boolean hasComplexity = lowerSolution.contains("time complexity") || lowerSolution.contains("space complexity");
        boolean hasExplanation = lowerSolution.contains("approach") || lowerSolution.contains("explanation")
                || lowerSolution.contains("algorithm");

        // If it's a code solution, check for code blocks
        boolean hasCode = solution.contains("```") || solution.contains("def ") || solution.contains("public ") ||
                solution.contains("class ") || solution.contains("function ") || solution.contains("int ");

        // Log what components were found
        logger.info("Solution validation - Complexity: {}, Explanation: {}, Code: {}", hasComplexity, hasExplanation,
                hasCode);

        // If the solution seems incomplete, add a note
        if (!hasComplexity || !hasExplanation) {
            logger.warn("Generated solution may be incomplete - missing complexity analysis or explanation");
        }

        return solution;
    }

    private String buildPrompt(QuestionDetailResponse.QuestionDetail question, String language, boolean includeCode) {
        StringBuilder prompt = new StringBuilder();

        // Problem description
        prompt.append("You are an expert programming tutor and competitive programmer. Here's a LeetCode problem:\n\n");
        prompt.append("TITLE: ").append(question.getTitle()).append("\n");
        prompt.append("DIFFICULTY: ").append(question.getDifficulty()).append("\n");
        prompt.append("ACCEPTANCE RATE: ").append(question.getStats()).append("\n\n");

        // Clean HTML content (remove HTML tags)
        String cleanContent = cleanHtmlContent(question.getContent());
        prompt.append("PROBLEM DESCRIPTION:\n").append(cleanContent).append("\n\n");

        // Extract and highlight constraints
        String constraints = extractConstraints(cleanContent);
        if (!constraints.isEmpty()) {
            prompt.append("EXTRACTED CONSTRAINTS:\n").append(constraints).append("\n\n");
        }

        // Sample test case
        prompt.append("SAMPLE TEST CASE:\n").append(question.getSampleTestCase()).append("\n\n");

        // Function signature
        prompt.append("FUNCTION SIGNATURE:\n").append(question.getMetaData()).append("\n\n");

        // Include the driver code/template for the specific language
        if (question.getCodeSnippets() != null && !question.getCodeSnippets().isEmpty()) {
            var codeSnippet = question.getCodeSnippets().stream()
                    .filter(cs -> cs.getLangSlug().equalsIgnoreCase(language))
                    .findFirst()
                    .orElse(null);

            if (codeSnippet != null) {
                prompt.append("DRIVER CODE/TEMPLATE FOR ").append(language.toUpperCase()).append(":\n");
                prompt.append(codeSnippet.getCode()).append("\n\n");
            }
        }

        // Hints
        if (question.getHints() != null && !question.getHints().isEmpty()) {
            prompt.append("HINTS:\n");
            for (int i = 0; i < question.getHints().size(); i++) {
                prompt.append((i + 1) + ". ").append(question.getHints().get(i)).append("\n");
            }
            prompt.append("\n");
        }

        // Critical performance and constraint requirements
        prompt.append("CRITICAL REQUIREMENTS:\n");
        prompt.append("1. Your solution MUST handle ALL edge cases and constraints mentioned in the problem\n");
        prompt.append("2. The solution MUST be optimized to avoid Time Limit Exceeded (TLE) errors\n");
        prompt.append("3. Consider the worst-case time complexity and ensure it fits within LeetCode's time limits\n");
        prompt.append("4. Handle boundary conditions, empty inputs, and extreme values properly\n");
        prompt.append("5. Use efficient data structures and algorithms appropriate for the problem constraints\n");
        prompt.append("6. Ensure the solution works for the full range of input sizes mentioned in constraints\n\n");

        // Language-specific instructions
        if (includeCode) {
            prompt.append("Please provide a complete, production-ready solution in ").append(language).append(".\n");
            prompt.append("Include:\n");
            prompt.append("1. A clear explanation of your approach and why it's optimal\n");
            prompt.append("2. The complete code solution that can be directly submitted to LeetCode\n");
            prompt.append("3. Detailed time and space complexity analysis\n");
            prompt.append("4. Explanation of how your solution handles the constraints and edge cases\n");
            prompt.append("5. Brief explanation of key parts of the code\n");
            prompt.append("6. Any important optimizations you implemented\n\n");
            prompt.append(
                    "IMPORTANT: The code must be complete and ready to run. Include all necessary imports and helper functions.\n");
            prompt.append("Make sure the solution follows ").append(language)
                    .append(" best practices and naming conventions.");
        } else {
            prompt.append("Please provide a detailed problem-solving approach for this problem.\n");
            prompt.append("Include:\n");
            prompt.append("1. Step-by-step algorithm explanation with complexity analysis\n");
            prompt.append("2. Key insights and observations that lead to the optimal solution\n");
            prompt.append("3. Detailed time and space complexity analysis\n");
            prompt.append("4. Alternative approaches and their trade-offs\n");
            prompt.append("5. Common pitfalls and how to avoid them\n");
            prompt.append("6. Specific strategies for handling the constraints mentioned in the problem\n");
            prompt.append("7. Edge cases to consider and how to handle them\n\n");
            prompt.append("Do NOT provide any code - only the approach, strategy, and analysis.");
        }

        return prompt.toString();
    }

    private String extractConstraints(String content) {
        if (content == null || content.isEmpty())
            return "";

        StringBuilder constraints = new StringBuilder();

        // Look for common constraint patterns
        String[] lines = content.split("\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            String lowerLine = trimmedLine.toLowerCase();

            // Look for lines that contain constraint information
            if (lowerLine.contains("constraints") ||
                    lowerLine.contains("1 <= ") ||
                    lowerLine.contains("0 <= ") ||
                    lowerLine.contains(" <= ") ||
                    lowerLine.contains("array") && lowerLine.contains("length") ||
                    lowerLine.contains("string") && lowerLine.contains("length") ||
                    lowerLine.contains("time complexity") ||
                    lowerLine.contains("space complexity") ||
                    lowerLine.contains("follow up") ||
                    lowerLine.contains("note:") ||
                    lowerLine.contains("important:") ||
                    lowerLine.contains("hint:") ||
                    lowerLine.matches(".*\\d+\\s*<=.*") ||
                    lowerLine.matches(".*\\d+\\s*<.*") ||
                    lowerLine.matches(".*\\d+\\s*>.*") ||
                    lowerLine.matches(".*\\d+\\s*>=.*") ||
                    lowerLine.matches(".*\\d+\\s*\\+.*") ||
                    lowerLine.matches(".*\\d+\\s*\\-.*") ||
                    lowerLine.matches(".*\\d+\\s*\\*.*") ||
                    lowerLine.matches(".*\\d+\\s*\\/.*")) {

                constraints.append("• ").append(trimmedLine).append("\n");
            }
        }

        // If no specific constraints found, look for any lines with numbers and
        // comparisons
        if (constraints.length() == 0) {
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.matches(".*\\d+.*") &&
                        (trimmedLine.contains("<") || trimmedLine.contains(">") ||
                                trimmedLine.contains("=") || trimmedLine.contains("array") ||
                                trimmedLine.contains("string") || trimmedLine.contains("element") ||
                                trimmedLine.contains("character") || trimmedLine.contains("digit"))) {
                    constraints.append("• ").append(trimmedLine).append("\n");
                }
            }
        }

        // Also look for any lines that might contain important information about input
        // ranges
        if (constraints.length() == 0) {
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (trimmedLine.contains("input") &&
                        (trimmedLine.contains("array") || trimmedLine.contains("string") ||
                                trimmedLine.contains("integer") || trimmedLine.contains("list"))) {
                    constraints.append("• ").append(trimmedLine).append("\n");
                }
            }
        }

        // Log what constraints were found
        if (constraints.length() > 0) {
            logger.info("Extracted constraints for question: {}", constraints.toString().trim());
        } else {
            logger.info("No specific constraints extracted for question");
        }

        return constraints.toString();
    }

    private String cleanHtmlContent(String htmlContent) {
        if (htmlContent == null)
            return "";

        // First, let's preserve some important structural information before removing
        // HTML
        String processedContent = htmlContent;

        // Replace common HTML elements that contain important information
        processedContent = processedContent
                .replaceAll("<strong>", "**") // Bold text often contains constraints
                .replaceAll("</strong>", "**")
                .replaceAll("<b>", "**")
                .replaceAll("</b>", "**")
                .replaceAll("<em>", "*") // Italic text often contains important notes
                .replaceAll("</em>", "*")
                .replaceAll("<i>", "*")
                .replaceAll("</i>", "*")
                .replaceAll("<code>", "`") // Code blocks often contain examples
                .replaceAll("</code>", "`")
                .replaceAll("<pre>", "\n```\n") // Pre-formatted blocks
                .replaceAll("</pre>", "\n```\n")
                .replaceAll("<ul>", "\n") // Lists
                .replaceAll("</ul>", "\n")
                .replaceAll("<ol>", "\n")
                .replaceAll("</ol>", "\n")
                .replaceAll("<li>", "• ")
                .replaceAll("</li>", "\n")
                .replaceAll("<br\\s*/?>", "\n") // Line breaks
                .replaceAll("<p>", "\n") // Paragraphs
                .replaceAll("</p>", "\n")
                .replaceAll("<div>", "\n") // Divs
                .replaceAll("</div>", "\n");

        // Remove remaining HTML tags
        processedContent = processedContent.replaceAll("<[^>]*>", "");

        // Decode HTML entities
        processedContent = processedContent
                .replaceAll("&nbsp;", " ")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&")
                .replaceAll("&#39;", "'")
                .replaceAll("&quot;", "\"")
                .replaceAll("&le;", "<=")
                .replaceAll("&ge;", ">=")
                .replaceAll("&times;", "×")
                .replaceAll("&minus;", "-");

        // Clean up excessive whitespace while preserving structure
        processedContent = processedContent
                .replaceAll("\\n\\s*\\n\\s*\\n", "\n\n") // Remove excessive blank lines
                .replaceAll("\\s+", " ") // Normalize whitespace
                .trim();

        return processedContent;
    }

    private String extractResponseText(GeminiResponse response) {
        if (response.getCandidates() != null && !response.getCandidates().isEmpty()) {
            var candidate = response.getCandidates().get(0);
            if (candidate.getContent() != null && candidate.getContent().getParts() != null
                    && !candidate.getContent().getParts().isEmpty()) {
                return candidate.getContent().getParts().get(0).getText();
            }
        }
        return "No response generated from Gemini API";
    }
}
