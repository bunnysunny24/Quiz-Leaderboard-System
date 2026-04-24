package com.bajaj.quiz.quizleaderboard.runner;

import com.bajaj.quiz.quizleaderboard.service.LeaderboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "quiz.runner.enabled", havingValue = "true", matchIfMissing = false)
public class AppRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AppRunner.class);

    private final LeaderboardService service;

    public AppRunner(LeaderboardService service) {
        this.service = service;
    }

    @Override
    public void run(String... args) {
        try {
            service.process();
        } catch (Exception ex) {
            log.error("Startup process failed. App will continue running for manual API testing.", ex);
        }
    }
}
