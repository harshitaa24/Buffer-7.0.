package com.example.security_system.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DetectionEngine {

    private Map<String, Set<String>> userPasswords = new HashMap<>();
    private Map<String, Integer> attempts = new HashMap<>();
    private Map<String, Queue<Long>> requestTimes = new HashMap<>();

    // =========================================================
    // HASHMAP FOR ATTACK PATTERNS
    // =========================================================

    private Map<String, String> attackPatterns = new HashMap<>();

    public DetectionEngine() {

        attackPatterns.put(" OR ", "SQL Injection");
        attackPatterns.put("' OR ", "SQL Injection");
        attackPatterns.put("--", "SQL Injection");
        attackPatterns.put("1=1", "SQL Injection");
        attackPatterns.put("<SCRIPT>", "XSS");
    }


    // =========================================================
    // EXISTING METHOD - KEPT
    // =========================================================

    public boolean isSuspicious(
            String ip,
            String username,
            String password,
            String userAgent) {

        return !getDetectionType(
                ip,
                username,
                password,
                userAgent
        ).equals("Normal");
    }


    // =========================================================
    // DETECTION TYPE
    // =========================================================

    public String getDetectionType(
            String ip,
            String username,
            String password,
            String userAgent) {

        attempts.put(
                ip,
                attempts.getOrDefault(ip, 0) + 1
        );


        // =====================================================
        // 1. RATE LIMITING
        // =====================================================

        if (isRateLimited(ip)) {
            return "Rate Limiting";
        }


        // =====================================================
        // 2. BRUTE FORCE
        // =====================================================

        String key = ip + ":" + username;

        userPasswords.putIfAbsent(
                key,
                new HashSet<>()
        );

        userPasswords
                .get(key)
                .add(password);

        if (userPasswords.get(key).size() >= 4) {
            return "Brute Force";
        }


        // =====================================================
        // 3. SQL INJECTION / XSS
        // =====================================================

        if (isPatternSuspicious(username)) {

            String input = username.toUpperCase();

            if (input.contains("<SCRIPT>")) {
                return "XSS";
            }

            return "SQL Injection";
        }


        // =====================================================
        // 4. BOT / AUTOMATED REQUEST
        // =====================================================

        if (userAgent != null) {

            String agent = userAgent.toLowerCase();

            if (agent.contains("curl")
                    || agent.contains("python")) {

                return "Bot Activity";
            }
        }


        // =====================================================
        // 5. NORMAL
        // =====================================================

        return "Normal";
    }


    // =========================================================
    // RATE LIMITING
    // =========================================================

    private boolean isRateLimited(String ip) {

        long now = System.currentTimeMillis();

        requestTimes.putIfAbsent(
                ip,
                new LinkedList<>()
        );

        Queue<Long> q =
                requestTimes.get(ip);

        q.add(now);

        while (!q.isEmpty()
                && now - q.peek() > 10000) {

            q.poll();
        }

        return q.size() > 5;
    }


    // =========================================================
    // HASHMAP PATTERN CHECK
    // =========================================================

    private boolean isPatternSuspicious(
            String input) {

        if (input == null) {
            return false;
        }

        String text = input.toUpperCase();

        for (String pattern : attackPatterns.keySet()) {

            if (text.contains(pattern.toUpperCase())) {
                return true;
            }
        }

        return false;
    }
}
