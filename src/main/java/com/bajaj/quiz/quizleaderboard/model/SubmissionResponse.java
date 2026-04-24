package com.bajaj.quiz.quizleaderboard.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SubmissionResponse {
    @JsonProperty("isCorrect")
    @JsonAlias({"correct", "isCorrect"})
    private boolean correct;

    @JsonProperty("isIdempotent")
    @JsonAlias({"idempotent", "isIdempotent"})
    private boolean idempotent;

    private int submittedTotal;
    private int expectedTotal;
    private String message;
}
