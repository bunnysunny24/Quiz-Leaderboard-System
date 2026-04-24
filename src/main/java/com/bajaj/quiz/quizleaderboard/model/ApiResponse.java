package com.bajaj.quiz.quizleaderboard.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class ApiResponse {
    private String regNo;
    @JsonAlias({"setID", "set_id", "setId"})
    private String setId;
    private int pollIndex;
    private List<Event> events;
}
