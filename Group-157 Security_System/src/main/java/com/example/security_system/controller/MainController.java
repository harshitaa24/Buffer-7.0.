package com.example.security_system.controller;

import com.example.security_system.model.LogEntry;
import com.example.security_system.service.AttackGraph;
import com.example.security_system.service.DetectionEngine;
import com.example.security_system.service.FakeDatabase;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(
    origins = {
        "http://127.0.0.1:5500",
        "http://localhost:5500"
    },
    allowCredentials = "true"
)

@RestController
public class MainController {

    @Autowired
    private DetectionEngine detector;

    @Autowired
    private AttackGraph attackGraph;

    @Autowired
    private FakeDatabase fakeDB;

    // Real logs generated during the current application run
    private final List<LogEntry> realLogs =
            Collections.synchronizedList(new ArrayList<>());


    // =========================================================
    // PAGE ROUTES
    // =========================================================

    @GetMapping("/")
    public void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login.html");
    }

    @GetMapping("/login")
    public void loginPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login.html");
    }

    @GetMapping("/admin")
    public void adminPage(HttpServletResponse response) throws IOException {
        response.sendRedirect("/admin.html");
    }


    // =========================================================
    // LOGIN / HONEYPOT
    // =========================================================

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request) {

        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // DSA: Graph
        attackGraph.recordRequest(ip, "/login");

        // DSA: Trie + HashMap + HashSet + Queue
        String tag = detector.getDetectionType(
        ip,
        username,
        password,
        userAgent
);

boolean suspicious = !tag.equals("Normal");

        LogEntry log = new LogEntry(
                ip,
                "/login",
                username,
                password,
                userAgent,
                tag
        );

        realLogs.add(log);
        saveToFile(log);

        // Keep FakeDatabase for DSA/demo data
        fakeDB.addRealLog(Map.of(
                "ip", ip,
                "endpoint", "/login",
                "username", username,
                "password", password,
                "userAgent",
                userAgent == null ? "Unknown" : userAgent,
                "reason",
                suspicious ? "Suspicious Activity" : "Normal"
        ));

        /*
         * Existing project behaviour is preserved:
         *
         * Suspicious login -> SUCCESS
         * Normal login     -> FAILED
         *
         * This makes suspicious users believe
         * that they successfully entered the system.
         */
        return suspicious ? "SUCCESS" : "FAILED";
    }


    // =========================================================
    // ADMIN LOGIN
    // =========================================================
    // Keeping the existing simple login for now.
    // We are NOT adding database authentication yet.
    // =========================================================

    @PostMapping("/real-admin")
    public void adminLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        if ("realadmin".equals(username)
                && "1234".equals(password)) {

            response.sendRedirect("/admin.html");

        } else {

            response.getWriter().write("Access Denied");
        }
    }


    // =========================================================
    // DASHBOARD
    // =========================================================

    @GetMapping("/api/dashboard")
    public Map<String, Object> dashboard() {

        int totalAttacks = realLogs.size();

        Set<String> uniqueIPs = new HashSet<>();

        for (LogEntry log : realLogs) {
            uniqueIPs.add(log.ip);
        }

        int activeAttackers = uniqueIPs.size();

        LocalDateTime now = LocalDateTime.now();

        int requestsPerMinute = 0;

        for (LogEntry log : realLogs) {

            if (log.timestamp.isAfter(
                    now.minusMinutes(1))) {

                requestsPerMinute++;
            }
        }

        return Map.of(
                "totalAttacks", totalAttacks,
                "activeAttackers", activeAttackers,
                "requestsPerMinute", requestsPerMinute
        );
    }


    // =========================================================
    // LOGS
    // =========================================================

    @GetMapping("/api/logs")
    public List<LogEntry> getLogs() {
        return realLogs;
    }


    // Keep /logs temporarily so old frontend code
    // doesn't immediately break.
    @GetMapping("/logs")
    public List<LogEntry> getLogsOld() {
        return realLogs;
    }


    // =========================================================
    // ATTACK GRAPH - ROUTES
    // =========================================================

    @GetMapping("/api/routes")
    public Map<String, List<List<String>>> getRoutes() {
        return attackGraph.getRoutes();
    }


    // =========================================================
    // ATTACK GRAPH - BFS
    // =========================================================

    @GetMapping("/api/bfs")
    public List<String> bfs(
            @RequestParam String start,
            @RequestParam String target) {

        return attackGraph.bfsShortestPath(
                start,
                target
        );
    }


    // =========================================================
    // HONEYPOT FILES
    // =========================================================

    @GetMapping("/api/files")
    public List<String> getFiles() {

        return Arrays.asList(
                "config.pdf",
                "credentials.txt",
                "db_backup.sql",
                "payroll.xlsx"
        );
    }


    // =========================================================
    // FILE ACCESS
    // =========================================================

    @GetMapping("/api/download")
    public String download(
            @RequestParam String file,
            HttpServletRequest request) {

        String ip = getClientIp(request);

        String endpoint = "DOWNLOAD_" + file;

        // DSA: AttackGraph
        attackGraph.recordRequest(
                ip,
                endpoint
        );

        LogEntry log = new LogEntry(
                ip,
                endpoint,
                "-",
                "-",
                "fake",
                "Suspicious"
        );

        realLogs.add(log);
        saveToFile(log);

        return "Downloading " + file;
    }


    // Keep old endpoint temporarily
    // so existing frontend doesn't break.
    @GetMapping("/file-access")
    public String fileAccess(
            @RequestParam String file,
            HttpServletRequest request) {

        return download(file, request);
    }


    // =========================================================
    // HONEYPOT ACTIONS
    // =========================================================

    @PostMapping("/api/sync")
    public String sync(HttpServletRequest request) {

        return recordAction(
                request,
                "SYNC",
                "Suspicious",
                "Database synced"
        );
    }


    @PostMapping("/api/clear-logs")
    public String clearLogs(HttpServletRequest request) {

        return recordAction(
                request,
                "CLEAR_LOGS",
                "Critical",
                "Logs cleared"
        );
    }


    @PostMapping("/api/reset")
    public String reset(HttpServletRequest request) {

        return recordAction(
                request,
                "RESET",
                "Critical",
                "System reset"
        );
    }


    @PostMapping("/api/restart")
    public String restart(HttpServletRequest request) {

        return recordAction(
                request,
                "RESTART",
                "Suspicious",
                "Server restarted"
        );
    }


    @PostMapping("/api/delete-all")
    public String deleteAll(HttpServletRequest request) {

        return recordAction(
                request,
                "DELETE_ALL",
                "Critical",
                "Deleted"
        );
    }


    // =========================================================
    // QUERY
    // =========================================================

    @PostMapping("/api/query")
    public String query(
            @RequestParam String query,
            HttpServletRequest request) {

        String ip = getClientIp(request);

        attackGraph.recordRequest(
                ip,
                "QUERY"
        );

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


    // =========================================================
    // COMMON ACTION LOGGER
    // =========================================================

    private String recordAction(
            HttpServletRequest request,
            String endpoint,
            String severity,
            String message) {

        String ip = getClientIp(request);

        // DSA: Graph
        attackGraph.recordRequest(
                ip,
                endpoint
        );

        LogEntry log = new LogEntry(
                ip,
                endpoint,
                "-",
                "-",
                "fake",
                severity
        );

        realLogs.add(log);
        saveToFile(log);

        return message;
    }


    // =========================================================
    // GET CLIENT IP
    // =========================================================

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


    // =========================================================
    // FILE LOGGER
    // =========================================================

    private void saveToFile(LogEntry log) {

        try (FileWriter writer =
                     new FileWriter("logs.txt", true)) {

            writer.write(
                    log.ip + "," +
                    log.endpoint + "," +
                    log.username + "," +
                    log.password + "," +
                    log.userAgent + "," +
                    log.timestamp + "," +
                    log.tag +
                    "\n"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
