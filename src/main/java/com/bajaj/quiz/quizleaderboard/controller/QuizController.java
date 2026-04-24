package com.bajaj.quiz.quizleaderboard.controller;

import com.bajaj.quiz.quizleaderboard.model.ApiResponse;
import com.bajaj.quiz.quizleaderboard.model.Event;
import com.bajaj.quiz.quizleaderboard.model.ProcessResult;
import com.bajaj.quiz.quizleaderboard.model.SubmissionRequest;
import com.bajaj.quiz.quizleaderboard.model.SubmissionResponse;
import java.util.ArrayList;
import java.util.List;
import com.bajaj.quiz.quizleaderboard.service.LeaderboardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final LeaderboardService leaderboardService;

    public QuizController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/messages")
    public ApiResponse getMessages(@RequestParam(required = false) String regNo, @RequestParam int poll) {
        if (poll < 0 || poll > 9) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "poll must be between 0 and 9");
        }
        return normalizeMessageResponse(leaderboardService.fetchPoll(regNo, poll), regNo, poll);
    }

    @PostMapping("/submit")
    public SubmissionResponse submit(@RequestBody SubmissionRequest request) {
        return leaderboardService.submitLeaderboard(request);
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestParam(required = false) String regNo) throws InterruptedException {
        ProcessResult result = leaderboardService.processAndReturn(regNo);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Submission already attempted in this application run.");
        }
        return ResponseEntity.ok(result);
    }

    private ApiResponse normalizeMessageResponse(ApiResponse response, String regNo, int poll) {
        if (response == null) {
            return null;
        }

        if (response.getSetId() == null || response.getSetId().isBlank()) {
            response.setSetId("SET_1");
        }

        if (response.getRegNo() == null || response.getRegNo().isBlank()) {
            response.setRegNo(regNo != null ? regNo : "2024CS101");
        }

        response.setPollIndex(poll);

        if (response.getEvents() == null) {
            response.setEvents(new ArrayList<>());
        } else {
            List<Event> normalizedEvents = new ArrayList<>();
            for (Event event : response.getEvents()) {
                normalizedEvents.add(event);
            }
            response.setEvents(normalizedEvents);
        }

        return response;
    }
}
