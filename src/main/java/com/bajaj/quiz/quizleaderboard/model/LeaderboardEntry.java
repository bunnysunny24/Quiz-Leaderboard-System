package com.bajaj.quiz.quizleaderboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntry {
    private String participant;
    private int totalScore;
}
