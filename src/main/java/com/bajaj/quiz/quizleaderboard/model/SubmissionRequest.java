package com.bajaj.quiz.quizleaderboard.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubmissionRequest {
    private String regNo;
    private List<LeaderboardEntry> leaderboard;
}
