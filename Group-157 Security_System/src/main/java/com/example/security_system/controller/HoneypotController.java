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
    private AttackGraph attackGraph;

    private List<String> fakeFiles = List.of(
            "salary_data.xlsx",
            "employee_records.csv",
            "confidential_report.pdf",
            "server_backup.zip"
    );

    // =========================
    // 🔥 COMMON IP METHOD
    // =========================
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request) {

        String ip = getClientIp(request);
        String agent = request.getHeader("User-Agent");

        attackGraph.recordRequest(ip, "/login");

        boolean suspicious = detector.isSuspicious(ip, username, password, agent);

        LogEntry log = new LogEntry(
                ip, "/login", username, password, agent,
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

    // =========================
    // ADMIN LOGIN
    // =========================
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

    // =========================
    // DASHBOARD STATS
    // =========================
    @GetMapping("/api/dashboard")
    public Map<String, Object> getDashboardStats(HttpSession session) {

        Boolean admin = (Boolean) session.getAttribute("admin");
        if (admin == null || !admin) return Map.of();

        int totalAttacks = realLogs.size();
        Set<String> uniqueIPs = new HashSet<>();

        for (LogEntry log : realLogs) {
            uniqueIPs.add(log.ip);
        }

        int activeAttackers = uniqueIPs.size();

        LocalDateTime now = LocalDateTime.now();
        int rpm = 0;

        for (LogEntry log : realLogs) {
            if (log.timestamp.isAfter(now.minusMinutes(1))) {
                rpm++;
            }
        }

        return Map.of(
                "totalAttacks", totalAttacks,
                "activeAttackers", activeAttackers,
                "requestsPerMinute", rpm
        );
    }

    // =========================
    // GRAPH API
    // =========================
    @GetMapping("/api/routes")
    public Map<String, List<List<String>>> getRoutes() {
        return attackGraph.getRoutes();
    }

    //BFS
    // =========================
        @GetMapping("/api/bfs")
public List<String> getShortestPath(
        @RequestParam String start,
        @RequestParam String target) {

    return attackGraph.bfsShortestPath(start, target);
}
    // =========================
    // LOGS
    // =========================
    @GetMapping("/logs")
    public Object getLogs(HttpSession session) {
        Boolean admin = (Boolean) session.getAttribute("admin");
        return (admin == null || !admin) ? "Access Denied" : realLogs;
    }

    // =========================
    // FILE DOWNLOAD
    // =========================
    @GetMapping("/api/download")
    public String download(@RequestParam String file, HttpServletRequest request) {

        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "DOWNLOAD_" + file);

        LogEntry log = new LogEntry(ip, "DOWNLOAD_" + file, "-", "-", "fake", "Suspicious");
        realLogs.add(log);
        saveToFile(log);

        return "Downloading " + file;
    }

    // =========================
    // 🔥 NEW ACTIONS (IMPORTANT)
    // =========================
    @GetMapping("/api/files")
public List<String> getFiles() {
    return fakeFiles;
}

    @PostMapping("/api/sync")
    public String sync(HttpServletRequest request) {
        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "SYNC");

        realLogs.add(new LogEntry(ip, "SYNC", "-", "-", "fake", "Suspicious"));
        saveToFile(realLogs.get(realLogs.size()-1));

        return "Database synced";
    }

    @PostMapping("/api/clear-logs")
    public String clearLogs(HttpServletRequest request) {
        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "CLEAR_LOGS");

        realLogs.add(new LogEntry(ip, "CLEAR_LOGS", "-", "-", "fake", "Critical"));
        saveToFile(realLogs.get(realLogs.size()-1));

        return "Logs cleared";
    }

    @PostMapping("/api/reset")
    public String resetSystem(HttpServletRequest request) {
        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "RESET_SYSTEM");

        realLogs.add(new LogEntry(ip, "RESET_SYSTEM", "-", "-", "fake", "Critical"));
        saveToFile(realLogs.get(realLogs.size()-1));

        return "System reset";
    }

    // =========================
    // EXISTING ACTIONS
    // =========================
    @PostMapping("/api/restart")
    public String restart(HttpServletRequest request) {
        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "RESTART");

        realLogs.add(new LogEntry(ip, "RESTART", "-", "-", "fake", "Suspicious"));
        saveToFile(realLogs.get(realLogs.size()-1));

        return "Server restarted";
    }

    @PostMapping("/api/delete-all")
    public String deleteAll(HttpServletRequest request) {
        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "DELETE_ALL");

        realLogs.add(new LogEntry(ip, "DELETE_ALL", "-", "-", "fake", "Critical"));
        saveToFile(realLogs.get(realLogs.size()-1));

        return "Deleted";
    }

    @PostMapping("/api/query")
    public String query(@RequestParam String query, HttpServletRequest request) {
        String ip = getClientIp(request);

        attackGraph.recordRequest(ip, "QUERY");

        realLogs.add(new LogEntry(ip, "QUERY", query, "-", "fake", "Suspicious"));
        saveToFile(realLogs.get(realLogs.size()-1));

        return "Executed";
    }

    
    // =========================
    // FILE LOGGER
    // =========================
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
