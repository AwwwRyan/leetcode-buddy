package org.dailyquestionsms.service;

import java.time.Duration;

import org.dailyquestionsms.model.LeetCodeResponse;
import org.dailyquestionsms.model.QuestionDetailResponse;
import org.dailyquestionsms.model.UserSubmissionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class LeetCodeService {
    
    private static final Logger logger = LoggerFactory.getLogger(LeetCodeService.class);
    private final WebClient webClient;
    private static final String LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql";
    
    public LeetCodeService() {
        this.webClient = WebClient.builder()
                .baseUrl(LEETCODE_GRAPHQL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT_LANGUAGE, "en-US,en;q=0.9")
                .defaultHeader(HttpHeaders.REFERER, "https://leetcode.com/")
                .defaultHeader("Origin", "https://leetcode.com")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2MB
                .build();
    }
    
    public Mono<LeetCodeResponse> getDailyQuestion() {
        // Try the simpler daily question endpoint first
        return tryDailyQuestionEndpoint()
                .onErrorResume(error -> {
                    logger.warn("Failed to fetch from daily endpoint, trying GraphQL: {}", error.getMessage());
                    return tryGraphQLEndpoint();
                });
    }
    
    public Mono<QuestionDetailResponse> getQuestionDetail(String titleSlug) {
        String graphqlQuery = String.format("""
            {
              "query": "query getQuestionDetail($titleSlug: String!) { question(titleSlug: $titleSlug) { questionId questionFrontendId title titleSlug difficulty isPaidOnly content translatedTitle translatedContent sampleTestCase enableRunCode metaData stats hints codeSnippets { lang langSlug code } topicTags { name slug translatedName } companyTagStats likes dislikes similarQuestions } }",
              "variables": {
                "titleSlug": "%s"
              }
            }
            """, titleSlug);
        
        logger.info("Fetching detailed question information for titleSlug: {}", titleSlug);
        
        return webClient.post()
                .bodyValue(graphqlQuery)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        logger.error("Question detail HTTP Error: {} - {}", response.statusCode(), response.statusCode().value());
                        // Log all response headers for debugging
                        response.headers().asHttpHeaders().forEach((key, values) -> 
                            logger.error("Response header {}: {}", key, values));
                        
                        return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                logger.error("Question detail error response body: {}", body);
                                return Mono.error(new RuntimeException("Question detail HTTP " + response.statusCode() + ": " + body));
                            })
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (body == null || body.toString().isEmpty()) {
                                    return Mono.error(new RuntimeException("Question detail HTTP " + response.statusCode() + ": No response body"));
                                }
                                return Mono.error(new RuntimeException("Question detail HTTP " + response.statusCode() + ": " + body));
                            });
                    })
                .bodyToMono(QuestionDetailResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> {
                    if (response.getData() != null && response.getData().getQuestion() != null) {
                        logger.info("Successfully fetched question detail: {}", 
                            response.getData().getQuestion().getTitle());
                    } else {
                        logger.warn("Question detail response received but no question data found");
                    }
                })
                .doOnError(error -> {
                    logger.error("Question detail endpoint error: {}", error.getMessage());
                    if (error.getMessage() != null && error.getMessage().contains("499")) {
                        logger.error("Status 499 detected - this might indicate a request cancellation or proxy issue");
                    }
                });
    }
    
    private Mono<LeetCodeResponse> tryDailyQuestionEndpoint() {
        String dailyUrl = "https://leetcode.com/api/problems/daily/";
        logger.info("Trying daily question endpoint: {}", dailyUrl);
        
        return WebClient.builder()
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.REFERER, "https://leetcode.com/")
                .build()
                .get()
                .uri(dailyUrl)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        logger.error("Daily endpoint HTTP Error: {} - {}", response.statusCode(), response.statusCode().value());
                        return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new RuntimeException("Daily endpoint HTTP " + response.statusCode() + ": " + body)));
                    })
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(body -> logger.info("Daily endpoint response: {}", body))
                .doOnError(error -> logger.error("Daily endpoint error: {}", error.getMessage()))
                .then(Mono.error(new RuntimeException("Daily endpoint not implemented yet")));
    }
    
    private Mono<LeetCodeResponse> tryGraphQLEndpoint() {
        // Proper GraphQL query format - simplified and corrected
        String graphqlQuery = """
            {
              "query": "query questionOfToday { activeDailyCodingChallengeQuestion { date link question { acRate difficulty frontendQuestionId: questionFrontendId title titleSlug topicTags { name slug } } } }"
            }
            """;
        
        logger.info("Trying GraphQL endpoint with query: {}", graphqlQuery);
        
        return webClient.post()
                .bodyValue(graphqlQuery)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        logger.error("GraphQL HTTP Error: {} - {}", response.statusCode(), response.statusCode().value());
                        // Log all response headers for debugging
                        response.headers().asHttpHeaders().forEach((key, values) -> 
                            logger.error("Response header {}: {}", key, values));
                        
                        return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                logger.error("GraphQL error response body: {}", body);
                                return Mono.error(new RuntimeException("GraphQL HTTP " + response.statusCode() + ": " + body));
                            })
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (body == null || body.toString().isEmpty()) {
                                    return Mono.error(new RuntimeException("GraphQL HTTP " + response.statusCode() + ": No response body"));
                                }
                                return Mono.error(new RuntimeException("GraphQL HTTP " + response.statusCode() + ": " + body));
                            });
                    })
                .bodyToMono(LeetCodeResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> {
                    if (response.getData() != null && response.getData().getActiveDailyCodingChallengeQuestion() != null) {
                        logger.info("Successfully fetched daily question: {}", 
                            response.getData().getActiveDailyCodingChallengeQuestion().getQuestion().getTitle());
                    } else {
                        logger.warn("GraphQL response received but no question data found");
                    }
                })
                .doOnError(error -> {
                    logger.error("GraphQL endpoint error: {}", error.getMessage());
                    if (error.getMessage() != null && error.getMessage().contains("499")) {
                        logger.error("Status 499 detected - this might indicate a request cancellation or proxy issue");
                    }
                });
    }
    
    public Mono<UserSubmissionResponse> getUserRecentSubmissions(String username, int limit) {
        String graphqlQuery = String.format("""
            {
              "query": "query recentAcSubmissions($username: String!, $limit: Int!) { recentAcSubmissionList(username: $username, limit: $limit) { id title titleSlug timestamp lang } }",
              "variables": {
                "username": "%s",
                "limit": %d
              }
            }
            """, username, limit);
        
        logger.info("Fetching recent submissions for user: {} with limit: {}", username, limit);
        
        return webClient.post()
                .bodyValue(graphqlQuery)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> {
                        logger.error("User submissions HTTP Error: {} - {}", response.statusCode(), response.statusCode().value());
                        return response.bodyToMono(String.class)
                            .flatMap(body -> {
                                logger.error("User submissions error response body: {}", body);
                                return Mono.error(new RuntimeException("User submissions HTTP " + response.statusCode() + ": " + body));
                            })
                            .defaultIfEmpty("")
                            .flatMap(body -> {
                                if (body == null || body.toString().isEmpty()) {
                                    return Mono.error(new RuntimeException("User submissions HTTP " + response.statusCode() + ": No response body"));
                                }
                                return Mono.error(new RuntimeException("User submissions HTTP " + response.statusCode() + ": " + body));
                            });
                    })
                .bodyToMono(UserSubmissionResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnSuccess(response -> {
                    if (response.getData() != null && response.getData().getRecentAcSubmissionList() != null) {
                        logger.info("Successfully fetched {} submissions for user: {}", 
                            response.getData().getRecentAcSubmissionList().size(), username);
                    } else {
                        logger.warn("User submissions response received but no submission data found for user: {}", username);
                    }
                })
                .doOnError(error -> {
                    logger.error("User submissions endpoint error for user {}: {}", username, error.getMessage());
                });
    }
} 