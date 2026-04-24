package com.bajaj.quiz.quizleaderboard.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class Event {
    @JsonAlias({"roundid", "round_id", "roundId"})
    private String roundId;
    private String participant;
    private int score;
}
