package com.example.security_system.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DetectionEngine {
    private Map<String, Set<String>> userPasswords = new HashMap<>();
    private Map<String, Integer> attempts = new HashMap<>();
    private Map<String, Queue<Long>> requestTimes = new HashMap<>();
    private Trie trie = new Trie();

    public DetectionEngine() {
        trie.insert(" OR ");
        trie.insert("' OR ");
        trie.insert("--");
        trie.insert("1=1");
        trie.insert("<SCRIPT>");
    }

    public boolean isSuspicious(String ip, String username, String password, String userAgent) {
    attempts.put(ip, attempts.getOrDefault(ip, 0) + 1);
    if (isRateLimited(ip)) return true;

    String key = ip + ":" + username;

    // initialize set
    userPasswords.putIfAbsent(key, new HashSet<>());

    // add PASSWORD 
    userPasswords.get(key).add(password);

    // brute force detection
    if (userPasswords.get(key).size() >= 4) {
        return true;
    }

    if (isPatternSuspicious(username)) return true;

    if (userAgent != null &&
            (userAgent.contains("curl") || userAgent.contains("python"))) {
        return true;
    }

    return false;
}

    private boolean isRateLimited(String ip) {

        long now = System.currentTimeMillis();

        requestTimes.putIfAbsent(ip, new LinkedList<>());
        Queue<Long> q = requestTimes.get(ip);

        q.add(now);

        while (!q.isEmpty() && now - q.peek() > 10000) {
            q.poll();
        }

        return q.size() > 5;
    }

    private boolean isPatternSuspicious(String input) {
        return trie.containsPattern(input);
    }

    // ===== TRIE =====
    private static class Trie {

        static class Node {
            Map<Character, Node> children = new HashMap<>();
            boolean isEnd;
        }

        private final Node root = new Node();

        public void insert(String word) {
            Node cur = root;

            for (char c : word.toUpperCase().toCharArray()) {
                cur.children.putIfAbsent(c, new Node());
                cur = cur.children.get(c);
            }

            cur.isEnd = true;
        }

        public boolean containsPattern(String text) {

            if (text == null) return false;

            text = text.toUpperCase();

            for (int i = 0; i < text.length(); i++) {

                Node cur = root;

                for (int j = i; j < text.length(); j++) {

                    char c = text.charAt(j);

                    if (!cur.children.containsKey(c)) break;

                    cur = cur.children.get(c);

                    if (cur.isEnd) return true;
                }
            }

            return false;
        }
    }
}
