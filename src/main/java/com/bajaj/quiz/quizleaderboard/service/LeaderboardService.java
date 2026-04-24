package com.bajaj.quiz.quizleaderboard.service;

import com.bajaj.quiz.quizleaderboard.client.QuizApiClient;
import com.bajaj.quiz.quizleaderboard.model.ApiResponse;
import com.bajaj.quiz.quizleaderboard.model.Event;
import com.bajaj.quiz.quizleaderboard.model.LeaderboardEntry;
import com.bajaj.quiz.quizleaderboard.model.ProcessResult;
import com.bajaj.quiz.quizleaderboard.model.SubmissionRequest;
import com.bajaj.quiz.quizleaderboard.model.SubmissionResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService {

    private static final Logger log = LoggerFactory.getLogger(LeaderboardService.class);

    private final QuizApiClient apiClient;
    private final boolean mockSubmitSuccess;
    private final AtomicBoolean submitted = new AtomicBoolean(false);

    public LeaderboardService(QuizApiClient apiClient, @Value("${quiz.submit.mock-success:false}") boolean mockSubmitSuccess) {
        this.apiClient = apiClient;
        this.mockSubmitSuccess = mockSubmitSuccess;
    }

    public ApiResponse fetchPoll(int poll) {
        return apiClient.fetchPoll(poll);
    }

    public ApiResponse fetchPoll(String regNo, int poll) {
        return apiClient.fetchPoll(regNo, poll);
    }

    public SubmissionResponse submitLeaderboard(SubmissionRequest request) {
        if (mockSubmitSuccess) {
            SubmissionResponse mockResponse = new SubmissionResponse();
            int submittedTotal = request.getLeaderboard() == null
                ? 0
                : request.getLeaderboard().stream().mapToInt(LeaderboardEntry::getTotalScore).sum();
            mockResponse.setCorrect(true);
            mockResponse.setIdempotent(true);
            mockResponse.setSubmittedTotal(submittedTotal);
            mockResponse.setExpectedTotal(submittedTotal);
            mockResponse.setMessage("Correct!");
            return mockResponse;
        }
        return apiClient.submitLeaderboard(request);
    }

    public void process() throws InterruptedException {
        processAndReturn();
    }

    public ProcessResult processAndReturn() throws InterruptedException {
        return processAndReturn(apiClient.getRegNo());
    }

    public ProcessResult processAndReturn(String regNo) throws InterruptedException {
        if (submitted.get()) {
            log.warn("Submission already attempted in this application run. Skipping.");
            return null;
        }

        Set<String> seenEventKeys = new HashSet<>();
        Map<String, Integer> participantScores = new HashMap<>();

        for (int poll = 0; poll < 10; poll++) {
            ApiResponse response = apiClient.fetchPoll(regNo, poll);
            if (response != null && response.getEvents() != null) {
                for (Event event : response.getEvents()) {
                    String dedupKey = event.getRoundId() + "|" + event.getParticipant();
                    if (!seenEventKeys.add(dedupKey)) {
                        continue;
                    }
                    participantScores.merge(event.getParticipant(), event.getScore(), Integer::sum);
                }
            }

            // Mandatory 5-second delay between poll requests.
            if (poll < 9) {
                Thread.sleep(5000);
            }
        }

        List<LeaderboardEntry> leaderboard = buildLeaderboard(participantScores);
        int totalScore = leaderboard.stream().mapToInt(LeaderboardEntry::getTotalScore).sum();

        log.info("Leaderboard: {}", leaderboard);
        log.info("Total Score: {}", totalScore);

        SubmissionRequest request = new SubmissionRequest(regNo, leaderboard);
        SubmissionResponse submitResponse = apiClient.submitLeaderboard(request);
        submitted.set(true);

        log.info("Submission Response: {}", submitResponse);
        return new ProcessResult(regNo, leaderboard, totalScore, submitResponse);
    }

    public List<LeaderboardEntry> buildLeaderboard(Map<String, Integer> participantScores) {
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        participantScores.forEach((participant, totalScore) ->
            leaderboard.add(new LeaderboardEntry(participant, totalScore))
        );
        leaderboard.sort((a, b) -> Integer.compare(b.getTotalScore(), a.getTotalScore()));
        return leaderboard;
    }
}
