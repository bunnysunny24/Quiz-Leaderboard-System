package com.bajaj.quiz.quizleaderboard.client;

import com.bajaj.quiz.quizleaderboard.model.ApiResponse;
import com.bajaj.quiz.quizleaderboard.model.SubmissionRequest;
import com.bajaj.quiz.quizleaderboard.model.SubmissionResponse;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Component
public class QuizApiClient {

    private static final String BASE_URL = "https://devapigw.vidalhealthtpa.com/srm-quiz-task";

    private final RestTemplate restTemplate;
    private final String regNo;

    public QuizApiClient(RestTemplate restTemplate, @Value("${quiz.reg-no:2024CS101}") String regNo) {
        this.restTemplate = restTemplate;
        this.regNo = regNo;
    }

    public String getRegNo() {
        return regNo;
    }

    public ApiResponse fetchPoll(int poll) {
        return fetchPoll(regNo, poll);
    }

    public ApiResponse fetchPoll(String requestRegNo, int poll) {
        String effectiveRegNo = (requestRegNo == null || requestRegNo.isBlank()) ? regNo : requestRegNo;
        String url = BASE_URL + "/quiz/messages?regNo=" + effectiveRegNo + "&poll=" + poll;
        return executeWithRetry(() -> restTemplate.getForObject(url, ApiResponse.class), "GET poll " + poll);
    }

    public SubmissionResponse submitLeaderboard(SubmissionRequest request) {
        String url = BASE_URL + "/quiz/submit";
        return executeWithRetry(() -> restTemplate.postForObject(url, request, SubmissionResponse.class), "POST submit");
    }

    private <T> T executeWithRetry(Supplier<T> action, String operation) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (RestClientResponseException ex) {
                int statusCode = ex.getStatusCode().value();
                if (attempt == maxAttempts || !isRetryableStatus(statusCode)) {
                    throw ex;
                }
                pauseBeforeRetry(operation, attempt, statusCode, ex.getResponseBodyAsString());
            } catch (ResourceAccessException ex) {
                if (attempt == maxAttempts) {
                    throw ex;
                }
                pauseBeforeRetry(operation, attempt, -1, ex.getMessage());
            }
        }
        throw new IllegalStateException("Retry loop exhausted for " + operation);
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private void pauseBeforeRetry(String operation, int attempt, int statusCode, String details) {
        try {
            Thread.sleep(1500L);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying " + operation, interruptedException);
        }
    }
}
