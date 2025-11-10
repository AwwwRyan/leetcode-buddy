package org.dailyquestionsms;

import org.dailyquestionsms.controller.DailyQuestionController;
import org.dailyquestionsms.model.DailyQuestion;
import org.dailyquestionsms.model.LeetCodeResponse;
import org.dailyquestionsms.model.QuestionDetailResponse;
import org.dailyquestionsms.model.UserSubmissionResponse;
import org.dailyquestionsms.service.GeminiService;
import org.dailyquestionsms.service.LeetCodeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;

import java.util.List;

@WebFluxTest(controllers = DailyQuestionController.class)
class DailyQuestionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LeetCodeService leetCodeService;

    @MockBean
    private GeminiService geminiService;

    private LeetCodeResponse buildDailyQuestionResponse(String title, String slug) {
        DailyQuestion.Question q = new DailyQuestion.Question();
        q.setTitle(title);
        q.setTitleSlug(slug);
        q.setDifficulty("Medium");
        DailyQuestion dq = new DailyQuestion();
        dq.setQuestion(q);

        LeetCodeResponse.Data data = new LeetCodeResponse.Data();
        try {
            java.lang.reflect.Field f = data.getClass().getDeclaredField("activeDailyCodingChallengeQuestion");
            f.setAccessible(true);
            f.set(data, dq);
        } catch (Exception ignored) {}

        LeetCodeResponse resp = new LeetCodeResponse();
        resp.setData(data);
        return resp;
    }

    private QuestionDetailResponse buildQuestionDetailResponse(String title, String slug, boolean withContent, boolean withSnippets) {
        QuestionDetailResponse.QuestionDetail detail = new QuestionDetailResponse.QuestionDetail();
        detail.setTitle(title);
        detail.setTitleSlug(slug);
        detail.setDifficulty("Medium");
        detail.setStats("acRate: 55.0");
        detail.setContent(withContent ? "<p>Problem statement</p>" : null);
        detail.setSampleTestCase("[1,2,3]");
        detail.setMetaData("{" + "signature: 'fn'" + "}");
        detail.setHints(List.of("Use a hashmap"));
        if (withSnippets) {
            QuestionDetailResponse.CodeSnippet cs = new QuestionDetailResponse.CodeSnippet();
            cs.setLang("Python");
            cs.setLangSlug("python");
            cs.setCode("def solve(): pass");
            detail.setCodeSnippets(List.of(cs));
        }

        QuestionDetailResponse.QuestionDetailData data = new QuestionDetailResponse.QuestionDetailData();
        data.setQuestion(detail);
        QuestionDetailResponse resp = new QuestionDetailResponse();
        resp.setData(data);
        return resp;
    }

    @Test
    @DisplayName("GET /api/daily-question returns today's daily question")
    void getDailyQuestion_ok() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));

        webTestClient.get()
                .uri("/api/daily-question")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.question.title").isEqualTo("Two Sum")
                .jsonPath("$.question.titleSlug").isEqualTo("two-sum");
    }

    @Test
    @DisplayName("GET /api/daily-question/detail/{slug} returns detail")
    void getQuestionDetail_ok() {
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));

        webTestClient.get()
                .uri("/api/daily-question/detail/two-sum")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Two Sum")
                .jsonPath("$.titleSlug").isEqualTo("two-sum");
    }

    @Test
    @DisplayName("GET /api/daily-question/daily-with-detail returns detailed daily question")
    void getDailyQuestionWithDetail_ok() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));

        webTestClient.get()
                .uri("/api/daily-question/daily-with-detail")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Two Sum");
    }

    @Test
    @DisplayName("GET /api/daily-question/daily-with-detail/{language} returns detail with snippet")
    void getDailyQuestionWithDetailAndLanguage_ok() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));

        webTestClient.get()
                .uri("/api/daily-question/daily-with-detail/python")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Two Sum")
                .jsonPath("$.codeSnippet.langSlug").isEqualTo("python");
    }

    @Test
    @DisplayName("GET /api/daily-question/ai-solution returns solution for daily question")
    void getAISolution_ok() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));
        Mockito.when(geminiService.generateSolution(Mockito.any(), Mockito.eq("python"), Mockito.eq(true)))
                .thenReturn(Mono.just("Solution text"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/daily-question/ai-solution")
                        .queryParam("language", "python")
                        .queryParam("includeCode", "true").build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.question").isEqualTo("Two Sum")
                .jsonPath("$.language").isEqualTo("python")
                .jsonPath("$.solutionType").isEqualTo("code")
                .jsonPath("$.solution").isEqualTo("Solution text");
    }

    @Test
    @DisplayName("GET /api/daily-question/ai-solution/{slug} returns solution for specific question")
    void getAISolutionForQuestion_ok() {
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));
        Mockito.when(geminiService.generateSolution(Mockito.any(), Mockito.eq("python"), Mockito.eq(true)))
                .thenReturn(Mono.just("Solution text"));

        webTestClient.get()
                .uri("/api/daily-question/ai-solution/two-sum?language=python&includeCode=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.question").isEqualTo("Two Sum");
    }

    @Test
    @DisplayName("GET /api/daily-question/ai-solution-text returns textual solution for daily question")
    void getAISolutionText_ok() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));
        Mockito.when(geminiService.generateSolution(Mockito.any(), Mockito.eq("python"), Mockito.eq(true)))
                .thenReturn(Mono.just("Solution text"));

        webTestClient.get()
                .uri("/api/daily-question/ai-solution-text?language=python&includeCode=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.solution").isEqualTo("Solution text");
    }

    @Test
    @DisplayName("GET /api/daily-question/ai-solution-text/{slug} returns textual solution for specific question")
    void getAISolutionTextForQuestion_ok() {
        Mockito.when(leetCodeService.getQuestionDetail("two-sum"))
                .thenReturn(Mono.just(buildQuestionDetailResponse("Two Sum", "two-sum", true, true)));
        Mockito.when(geminiService.generateSolution(Mockito.any(), Mockito.eq("python"), Mockito.eq(true)))
                .thenReturn(Mono.just("Solution text"));

        webTestClient.get()
                .uri("/api/daily-question/ai-solution-text/two-sum?language=python&includeCode=true")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.language").isEqualTo("python");
    }

    @Test
    @DisplayName("GET /api/daily-question/health returns 200")
    void health_ok() {
        webTestClient.get()
                .uri("/api/daily-question/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Service is running");
    }

    @Test
    @DisplayName("GET /api/daily-question/test-leetcode returns success on happy path")
    void testLeetCodeConnection_ok() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));

        webTestClient.get()
                .uri("/api/daily-question/test-leetcode")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("LeetCode connection successful");
    }

    @Test
    @DisplayName("GET /api/daily-question/check-user-status/{username} indicates solved when submission matches")
    void checkUserDailyQuestionStatus_solved() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));

        UserSubmissionResponse.Submission s = new UserSubmissionResponse.Submission();
        s.setId("1");
        s.setTitle("Two Sum");
        s.setTitleSlug("two-sum");
        s.setLang("python");
        s.setTimestamp("123456");

        UserSubmissionResponse.Data usd = new UserSubmissionResponse.Data();
        usd.setRecentAcSubmissionList(List.of(s));
        UserSubmissionResponse ur = new UserSubmissionResponse();
        ur.setData(usd);

        Mockito.when(leetCodeService.getUserRecentSubmissions("Awryan", 100))
                .thenReturn(Mono.just(ur));

        webTestClient.get()
                .uri("/api/daily-question/check-user-status/Awryan")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.hasSolved").isEqualTo(true)
                .jsonPath("$.submissionLanguage").isEqualTo("python")
                .jsonPath("$.submissionTimestamp").isEqualTo("123456");
    }

    @Test
    @DisplayName("GET /api/daily-question/check-user-status/{username} indicates not solved when no match")
    void checkUserDailyQuestionStatus_notSolved() {
        Mockito.when(leetCodeService.getDailyQuestion())
                .thenReturn(Mono.just(buildDailyQuestionResponse("Two Sum", "two-sum")));

        UserSubmissionResponse.Data usd = new UserSubmissionResponse.Data();
        usd.setRecentAcSubmissionList(List.of());
        UserSubmissionResponse ur = new UserSubmissionResponse();
        ur.setData(usd);

        Mockito.when(leetCodeService.getUserRecentSubmissions("user", 100))
                .thenReturn(Mono.just(ur));

        webTestClient.get()
                .uri("/api/daily-question/check-user-status/user")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.hasSolved").isEqualTo(false);
    }
}


 