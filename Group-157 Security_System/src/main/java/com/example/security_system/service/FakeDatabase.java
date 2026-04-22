package com.example.security_system.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class FakeDatabase {

    private List<Map<String, String>> logs = new ArrayList<>();
    private Random random = new Random();

    public FakeDatabase() {
        generateFakeLogs(50);
    }

    private void generateFakeLogs(int count) {
        for (int i = 0; i < count; i++) {
            logs.add(generateRandomLog());
        }
    }

    public Map<String, String> generateRandomLog() {

        String[] endpoints = {"/login","/admin","/api/users","/dashboard"};
        String[] userAgents = {"Mozilla/5.0","curl/7.68","PostmanRuntime"};
        String[] reasons = {"Normal","SQL Injection","XSS","Brute Force"};

        return Map.of(
                "ip", generateIP(),
                "endpoint", endpoints[random.nextInt(endpoints.length)],
                "username", "user_" + random.nextInt(100),
                "password", "pass" + random.nextInt(9999),
                "userAgent", userAgents[random.nextInt(userAgents.length)],
                "reason", reasons[random.nextInt(reasons.length)]
        );
    }

    private String generateIP() {
        return random.nextInt(256) + "." +
               random.nextInt(256) + "." +
               random.nextInt(256) + "." +
               random.nextInt(256);
    }

    public List<Map<String, String>> getLogs() {
        return logs;
    }

    public void addRandomLog() {
        logs.add(generateRandomLog());
    }

    public void addRealLog(Map<String, String> log) {
        logs.add(log);
    }
}