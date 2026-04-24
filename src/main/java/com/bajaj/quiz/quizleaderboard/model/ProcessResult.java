package com.bajaj.quiz.quizleaderboard.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProcessResult {
    private String regNo;
    private List<LeaderboardEntry> leaderboard;
    private int totalScore;
    private SubmissionResponse submissionResponse;
}
