package com.bajaj.quiz.quizleaderboard;

import com.bajaj.quiz.quizleaderboard.client.QuizApiClient;
import com.bajaj.quiz.quizleaderboard.model.LeaderboardEntry;
import com.bajaj.quiz.quizleaderboard.service.LeaderboardService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderboardServiceTest {

    @Test
    void buildLeaderboard_sortsDescendingByTotalScore() {
        QuizApiClient apiClient = Mockito.mock(QuizApiClient.class);
        LeaderboardService service = new LeaderboardService(apiClient, false);

        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 80);
        scores.put("Bob", 120);
        scores.put("Charlie", 95);

        List<LeaderboardEntry> leaderboard = service.buildLeaderboard(scores);

        assertEquals(3, leaderboard.size());
        assertEquals("Bob", leaderboard.get(0).getParticipant());
        assertEquals(120, leaderboard.get(0).getTotalScore());
        assertEquals("Charlie", leaderboard.get(1).getParticipant());
        assertEquals("Alice", leaderboard.get(2).getParticipant());
    }
}
