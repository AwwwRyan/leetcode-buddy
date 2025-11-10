package org.dailyquestionsms.controller;

import org.dailyquestionsms.model.UserQuestionStatusResponse;
import org.dailyquestionsms.service.GeminiService;
import org.dailyquestionsms.service.LeetCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/daily-question")
public class DailyQuestionController {

    private static final Logger logger = LoggerFactory.getLogger(DailyQuestionController.class);

    @Autowired
    private LeetCodeService leetCodeService;

    @Autowired
    private GeminiService geminiService;

    @GetMapping
    public Mono<ResponseEntity<?>> getDailyQuestion() {
        return leetCodeService.getDailyQuestion()
                .map(response -> {
                    if (response.getData() != null
                            && response.getData().getActiveDailyCodingChallengeQuestion() != null) {
                        return ResponseEntity.ok(response.getData().getActiveDailyCodingChallengeQuestion());
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @GetMapping("/detail/{titleSlug}")
    public Mono<ResponseEntity<?>> getQuestionDetail(@PathVariable String titleSlug) {
        return leetCodeService.getQuestionDetail(titleSlug)
                .map(response -> {
                    if (response.getData() != null && response.getData().getQuestion() != null) {
                        return ResponseEntity.ok(response.getData().getQuestion());
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @GetMapping("/daily-with-detail")
    public Mono<ResponseEntity<?>> getDailyQuestionWithDetail() {
        return leetCodeService.getDailyQuestion()
                .flatMap(dailyResponse -> {
                    if (dailyResponse.getData() != null &&
                            dailyResponse.getData().getActiveDailyCodingChallengeQuestion() != null) {

                        String titleSlug = dailyResponse.getData()
                                .getActiveDailyCodingChallengeQuestion()
                                .getQuestion()
                                .getTitleSlug();

                        return leetCodeService.getQuestionDetail(titleSlug)
                                .map(detailResponse -> {
                                    if (detailResponse.getData() != null
                                            && detailResponse.getData().getQuestion() != null) {
                                        return ResponseEntity.ok(detailResponse.getData().getQuestion());
                                    } else {
                                        return ResponseEntity.notFound().build();
                                    }
                                });
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @GetMapping("/daily-with-detail/{language}")
    public Mono<ResponseEntity<?>> getDailyQuestionWithDetailAndLanguage(@PathVariable String language) {
        return leetCodeService.getDailyQuestion()
                .flatMap(dailyResponse -> {
                    if (dailyResponse.getData() != null &&
                            dailyResponse.getData().getActiveDailyCodingChallengeQuestion() != null) {

                        String titleSlug = dailyResponse.getData()
                                .getActiveDailyCodingChallengeQuestion()
                                .getQuestion()
                                .getTitleSlug();

                        return leetCodeService.getQuestionDetail(titleSlug)
                                .map(detailResponse -> {
                                    if (detailResponse.getData() != null
                                            && detailResponse.getData().getQuestion() != null) {
                                        // Create a map response for simplicity
                                        var question = detailResponse.getData().getQuestion();
                                        var codeSnippet = question.getCodeSnippets().stream()
                                                .filter(cs -> cs.getLangSlug().equalsIgnoreCase(language))
                                                .findFirst()
                                                .orElse(null);

                                        var responseMap = java.util.Map.of(
                                                "title", question.getTitle(),
                                                "difficulty", question.getDifficulty(),
                                                "content", question.getContent(),
                                                "sampleTestCase", question.getSampleTestCase(),
                                                "metaData", question.getMetaData(),
                                                "codeSnippet", codeSnippet,
                                                "hints", question.getHints().toArray(new String[0]));

                                        return ResponseEntity.ok(responseMap);
                                    } else {
                                        return ResponseEntity.notFound().build();
                                    }
                                });
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @GetMapping("/ai-solution")
    public Mono<ResponseEntity<?>> getAISolution(
            @RequestParam(defaultValue = "python") String language,
            @RequestParam(defaultValue = "true") boolean includeCode) {

        return leetCodeService.getDailyQuestion()
                .flatMap(dailyResponse -> {
                    if (dailyResponse.getData() != null &&
                            dailyResponse.getData().getActiveDailyCodingChallengeQuestion() != null) {

                        String titleSlug = dailyResponse.getData()
                                .getActiveDailyCodingChallengeQuestion()
                                .getQuestion()
                                .getTitleSlug();

                        return leetCodeService.getQuestionDetail(titleSlug)
                                .flatMap(detailResponse -> {
                                    if (detailResponse.getData() != null
                                            && detailResponse.getData().getQuestion() != null) {
                                        var question = detailResponse.getData().getQuestion();

                                        // Validate that we have the necessary data for AI solution generation
                                        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                                            return Mono.just(ResponseEntity.badRequest()
                                                    .body(java.util.Map.of("error", "Question content is missing")));
                                        }

                                        if (includeCode && (question.getCodeSnippets() == null
                                                || question.getCodeSnippets().isEmpty())) {
                                            return Mono.just(ResponseEntity.badRequest()
                                                    .body(java.util.Map.of("error",
                                                            "Code snippets are missing for the requested language")));
                                        }

                                        return geminiService.generateSolution(question, language, includeCode)
                                                .map(solution -> ResponseEntity.ok(java.util.Map.of(
                                                        "question", question.getTitle(),
                                                        "language", language,
                                                        "solutionType", includeCode ? "code" : "approach",
                                                        "solution", solution,
                                                        "hasCodeSnippets",
                                                        question.getCodeSnippets() != null
                                                                && !question.getCodeSnippets().isEmpty(),
                                                        "contentLength",
                                                        question.getContent() != null ? question.getContent().length()
                                                                : 0)));
                                    } else {
                                        return Mono.just(ResponseEntity.notFound().build());
                                    }
                                });
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @GetMapping("/ai-solution/{titleSlug}")
    public Mono<ResponseEntity<?>> getAISolutionForQuestion(
            @PathVariable String titleSlug,
            @RequestParam(defaultValue = "python") String language,
            @RequestParam(defaultValue = "true") boolean includeCode) {

        return leetCodeService.getQuestionDetail(titleSlug)
                .flatMap(detailResponse -> {
                    if (detailResponse.getData() != null && detailResponse.getData().getQuestion() != null) {
                        var question = detailResponse.getData().getQuestion();

                        // Validate that we have the necessary data for AI solution generation
                        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                            return Mono.just(ResponseEntity.badRequest()
                                    .body(java.util.Map.of("error", "Question content is missing")));
                        }

                        if (includeCode
                                && (question.getCodeSnippets() == null || question.getCodeSnippets().isEmpty())) {
                            return Mono.just(ResponseEntity.badRequest()
                                    .body(java.util.Map.of("error",
                                            "Code snippets are missing for the requested language")));
                        }

                        return geminiService.generateSolution(question, language, includeCode)
                                .map(solution -> ResponseEntity.ok(java.util.Map.of(
                                        "question", question.getTitle(),
                                        "language", language,
                                        "solutionType", includeCode ? "code" : "approach",
                                        "solution", solution,
                                        "hasCodeSnippets",
                                        question.getCodeSnippets() != null && !question.getCodeSnippets().isEmpty(),
                                        "contentLength",
                                        question.getContent() != null ? question.getContent().length() : 0)));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                })
                .onErrorReturn(ResponseEntity.status(500).body("Error generating solution"));
    }

    @GetMapping("/ai-solution-text")
    public Mono<ResponseEntity<?>> getAISolutionText(
            @RequestParam(defaultValue = "python") String language,
            @RequestParam(defaultValue = "true") boolean includeCode) {

        return leetCodeService.getDailyQuestion()
                .flatMap(dailyResponse -> {
                    if (dailyResponse.getData() != null &&
                            dailyResponse.getData().getActiveDailyCodingChallengeQuestion() != null) {

                        String titleSlug = dailyResponse.getData()
                                .getActiveDailyCodingChallengeQuestion()
                                .getQuestion()
                                .getTitleSlug();

                        return leetCodeService.getQuestionDetail(titleSlug)
                                .flatMap(detailResponse -> {
                                    if (detailResponse.getData() != null
                                            && detailResponse.getData().getQuestion() != null) {
                                        var question = detailResponse.getData().getQuestion();

                                        // Validate that we have the necessary data for AI solution generation
                                        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                                            return Mono.just(ResponseEntity.badRequest()
                                                    .body(java.util.Map.of("error", "Question content is missing")));
                                        }

                                        if (includeCode && (question.getCodeSnippets() == null
                                                || question.getCodeSnippets().isEmpty())) {
                                            return Mono.just(ResponseEntity.badRequest()
                                                    .body(java.util.Map.of("error",
                                                            "Code snippets are missing for the requested language")));
                                        }

                                        return geminiService.generateSolution(question, language, includeCode)
                                                .map(solution -> ResponseEntity.ok(java.util.Map.of(
                                                        "question", question.getTitle(),
                                                        "language", language,
                                                        "solutionType", includeCode ? "code" : "approach",
                                                        "solution", solution,
                                                        "hasCodeSnippets",
                                                        question.getCodeSnippets() != null
                                                                && !question.getCodeSnippets().isEmpty(),
                                                        "contentLength",
                                                        question.getContent() != null ? question.getContent().length()
                                                                : 0)));
                                    } else {
                                        return Mono.just(ResponseEntity.notFound().build());
                                    }
                                });
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                })
                .onErrorReturn(ResponseEntity.status(500).body("Error generating solution"));
    }

    @GetMapping("/ai-solution-text/{titleSlug}")
    public Mono<ResponseEntity<?>> getAISolutionTextForQuestion(
            @PathVariable String titleSlug,
            @RequestParam(defaultValue = "python") String language,
            @RequestParam(defaultValue = "true") boolean includeCode) {

        return leetCodeService.getQuestionDetail(titleSlug)
                .flatMap(detailResponse -> {
                    if (detailResponse.getData() != null && detailResponse.getData().getQuestion() != null) {
                        var question = detailResponse.getData().getQuestion();

                        // Validate that we have the necessary data for AI solution generation
                        if (question.getContent() == null || question.getContent().trim().isEmpty()) {
                            return Mono.just(ResponseEntity.badRequest()
                                    .body(java.util.Map.of("error", "Question content is missing")));
                        }

                        if (includeCode
                                && (question.getCodeSnippets() == null || question.getCodeSnippets().isEmpty())) {
                            return Mono.just(ResponseEntity.badRequest()
                                    .body(java.util.Map.of("error",
                                            "Code snippets are missing for the requested language")));
                        }

                        return geminiService.generateSolution(question, language, includeCode)
                                .map(solution -> ResponseEntity.ok(java.util.Map.of(
                                        "question", question.getTitle(),
                                        "language", language,
                                        "solutionType", includeCode ? "code" : "approach",
                                        "solution", solution,
                                        "hasCodeSnippets",
                                        question.getCodeSnippets() != null && !question.getCodeSnippets().isEmpty(),
                                        "contentLength",
                                        question.getContent() != null ? question.getContent().length() : 0)));
                    } else {
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                })
                .onErrorReturn(ResponseEntity.status(500).body("Error generating solution"));
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return Mono.just(ResponseEntity.ok("Service is running and im the goat"));
    }

    @GetMapping("/test-leetcode")
    public Mono<ResponseEntity<String>> testLeetCodeConnection() {
        return leetCodeService.getDailyQuestion()
                .map(response -> ResponseEntity.ok("LeetCode connection successful"))
                .onErrorResume(error -> Mono.just(ResponseEntity.status(500)
                        .body("LeetCode connection failed: " + error.getMessage())));
    }

    @GetMapping("/check-user-status/{username}")
    public Mono<ResponseEntity<?>> checkUserDailyQuestionStatus(@PathVariable String username) {
        return leetCodeService.getDailyQuestion()
                .flatMap(dailyResponse -> {
                    if (dailyResponse.getData() != null &&
                            dailyResponse.getData().getActiveDailyCodingChallengeQuestion() != null) {

                        var dailyQuestion = dailyResponse.getData().getActiveDailyCodingChallengeQuestion()
                                .getQuestion();
                        String todayTitleSlug = dailyQuestion.getTitleSlug();
                        String todayTitle = dailyQuestion.getTitle();

                        return leetCodeService.getUserRecentSubmissions(username, 100)
                                .map(userSubmissions -> {
                                    if (userSubmissions.getData() != null &&
                                            userSubmissions.getData().getRecentAcSubmissionList() != null) {

                                        // Check if today's question exists in user's recent submissions
                                        var submission = userSubmissions.getData().getRecentAcSubmissionList().stream()
                                                .filter(sub -> sub.getTitleSlug().equals(todayTitleSlug))
                                                .findFirst()
                                                .orElse(null);

                                        if (submission != null) {
                                            // User has solved today's question
                                            var response = new UserQuestionStatusResponse(
                                                    username, todayTitle, todayTitleSlug, true,
                                                    "User has successfully solved today's daily question!");
                                            response.setSubmissionLanguage(submission.getLang());
                                            response.setSubmissionTimestamp(submission.getTimestamp());
                                            return ResponseEntity.ok(response);
                                        } else {
                                            // User hasn't solved today's question
                                            return ResponseEntity.ok(new UserQuestionStatusResponse(
                                                    username, todayTitle, todayTitleSlug, false,
                                                    "User has not solved today's daily question yet."));
                                        }
                                    } else {
                                        // No submission data found
                                        return ResponseEntity.ok(new UserQuestionStatusResponse(
                                                username, todayTitle, todayTitleSlug, false,
                                                "Unable to fetch user submission data."));
                                    }
                                })
                                .onErrorResume(error -> {
                                    logger.error("Error fetching user submissions for {}: {}", username,
                                            error.getMessage());
                                    return Mono.just(ResponseEntity.ok(new UserQuestionStatusResponse(
                                            username, todayTitle, todayTitleSlug, false,
                                            "Error fetching user submissions: " + error.getMessage())));
                                });
                    } else {
                        return Mono.just(ResponseEntity.status(404)
                                .body("Today's daily question not found"));
                    }
                })
                .onErrorReturn(ResponseEntity.status(500)
                        .body("Error checking user status"));
    }
}