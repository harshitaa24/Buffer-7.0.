package com.example.security_system.controller;

import com.example.security_system.model.LogEntry;
import com.example.security_system.service.DetectionEngine;
import com.example.security_system.service.FakeDatabase;
import com.example.security_system.service.AttackGraph;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class HoneypotController {

    private List<LogEntry> realLogs = Collections.synchronizedList(new ArrayList<>());

    @Autowired
    private DetectionEngine detector;

    @Autowired
    private FakeDatabase fakeDB;

    @Autowired
    private AttackGraph attackGraph;   // ✅ NEW

    private List<String> fakeFiles = List.of(
            "salary_data.xlsx",
            "employee_records.csv",
            "confidential_report.pdf",
            "server_backup.zip"
    );

    // ===== LOGIN =====
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request) {

        String ip = request.getRemoteAddr();
        String agent = request.getHeader("User-Agent");

        attackGraph.recordRequest(ip, "/login");   // ✅ NEW

        boolean suspicious = detector.isSuspicious(ip, username, agent);

        LogEntry log = new LogEntry(
                ip,
                "/login",
                username,
                password,
                agent,
                suspicious ? "Suspicious" : "Normal"
        );

        realLogs.add(log);
        saveToFile(log);

        fakeDB.addRealLog(Map.of(
                "ip", ip,
                "endpoint", "/login",
                "username", username,
                "password", password,
                "userAgent", agent,
                "reason", suspicious ? "Suspicious Activity" : "Normal"
        ));

        return suspicious ? "SUCCESS" : "FAILED";
    }

    // ===== ADMIN LOGIN =====
    @PostMapping("/real-admin")
    public void adminLogin(@RequestParam String username,
                           @RequestParam String password,
                           HttpSession session,
                           HttpServletResponse response) throws IOException {

        if (username.equals("realadmin") && password.equals("1234")) {
            session.setAttribute("admin", true);
            response.sendRedirect("/admin.html");
            return;
        }

        response.getWriter().write("Access Denied");
    }

    // ===== DASHBOARD =====
    @GetMapping("/api/dashboard")
    public Map<String, Object> getDashboardStats(HttpSession session) {

        Boolean admin = (Boolean) session.getAttribute("admin");
        if (admin == null || !admin) {
            return Map.of();
        }

        int totalAttacks = realLogs.size();

        Set<String> uniqueIPs = new HashSet<>();
        for (LogEntry log : realLogs) {
            uniqueIPs.add(log.ip);
        }

        int activeAttackers = uniqueIPs.size();

        LocalDateTime now = LocalDateTime.now();
        int requestsPerMinute = 0;

        for (LogEntry log : realLogs) {
            if (log.timestamp.isAfter(now.minusMinutes(1))) {
                requestsPerMinute++;
            }
        }

        return Map.of(
                "totalAttacks", totalAttacks,
                "activeAttackers", activeAttackers,
                "requestsPerMinute", requestsPerMinute
        );
    }

    // ===== ATTACK GRAPH API =====
    @GetMapping("/api/routes")
    public Map<String, List<List<String>>> getRoutes() {
    return attackGraph.getRoutes();
}

    // ===== GET RAW LOGS =====
    @GetMapping("/logs")
    public Object getLogs(HttpSession session) {

        Boolean admin = (Boolean) session.getAttribute("admin");

        if (admin == null || !admin) {
            return "Access Denied";
        }

        return realLogs;
    }

    // ===== SUMMARY LOGS =====
    @GetMapping("/summary-logs")
    public List<Map<String, Object>> getSummaryLogs(HttpSession session) {

        Boolean admin = (Boolean) session.getAttribute("admin");

        if (admin == null || !admin) {
            return Collections.emptyList();
        }

        Map<String, List<LogEntry>> grouped = new HashMap<>();

        for (LogEntry log : realLogs) {
            grouped.computeIfAbsent(log.ip, k -> new ArrayList<>()).add(log);
        }

        List<Map<String, Object>> summary = new ArrayList<>();

        for (String ip : grouped.keySet()) {

            List<LogEntry> logs = grouped.get(ip);

            Set<String> uniqueEndpoints = new HashSet<>();
            LocalDateTime firstSeen = logs.get(0).timestamp;
            LocalDateTime lastSeen = logs.get(0).timestamp;

            for (LogEntry log : logs) {
                uniqueEndpoints.add(log.endpoint);

                if (log.timestamp.isBefore(firstSeen)) {
                    firstSeen = log.timestamp;
                }

                if (log.timestamp.isAfter(lastSeen)) {
                    lastSeen = log.timestamp;
                }
            }

            Map<String, Object> row = new HashMap<>();
            row.put("ip", ip);
            row.put("totalAttempts", logs.size());
            row.put("uniqueEndpoints", uniqueEndpoints.size());
            row.put("firstSeen", firstSeen);
            row.put("lastSeen", lastSeen);

            summary.add(row);
        }

        return summary;
    }

    // ===== FILES =====
    @GetMapping("/api/files")
    public List<String> getFiles() {
        return fakeFiles;
    }

    @GetMapping("/api/download")
    public String download(@RequestParam String file,
                           HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        attackGraph.recordRequest(ip, "DOWNLOAD_" + file);  // ✅ NEW

        LogEntry log = new LogEntry(
                ip,
                "DOWNLOAD_" + file,
                "-",
                "-",
                "fake",
                "Suspicious"
        );

        realLogs.add(log);
        saveToFile(log);

        return "Downloading " + file + "...";
    }

    // ===== BFS SHORTEST PATH =====
@GetMapping("/api/bfs")
public List<String> getShortestPath(
        @RequestParam String start,
        @RequestParam String target) {

    return attackGraph.bfsShortestPath(start, target);
}

    // ===== FAKE ACTIONS =====
    @PostMapping("/api/restart")
    public String restart(HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        attackGraph.recordRequest(ip, "RESTART");   // ✅ NEW

        LogEntry log = new LogEntry(
                ip,
                "RESTART",
                "-",
                "-",
                "fake",
                "Suspicious"
        );

        realLogs.add(log);
        saveToFile(log);

        return "Server restarted";
    }

    @PostMapping("/api/delete-all")
    public String deleteAll(HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        attackGraph.recordRequest(ip, "DELETE_ALL");   // ✅ NEW

        LogEntry log = new LogEntry(
                ip,
                "DELETE_ALL",
                "-",
                "-",
                "fake",
                "Critical"
        );

        realLogs.add(log);
        saveToFile(log);

        return "Deleted";
    }

    @PostMapping("/api/query")
    public String query(@RequestParam String query,
                        HttpServletRequest request) {

        String ip = request.getRemoteAddr();

        attackGraph.recordRequest(ip, "QUERY");   // ✅ NEW

        LogEntry log = new LogEntry(
                ip,
                "QUERY",
                query,
                "-",
                "fake",
                "Suspicious"
        );

        realLogs.add(log);
        saveToFile(log);

        return "Executed";
    }

    // ===== FILE LOGGING =====
    private void saveToFile(LogEntry log) {
        try (FileWriter fw = new FileWriter("logs.txt", true)) {

            fw.write(
                    log.ip + "," +
                    log.endpoint + "," +
                    log.username + "," +
                    log.password + "," +
                    log.userAgent + "," +
                    log.timestamp + "," +
                    log.tag + "\n"
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}