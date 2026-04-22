package com.example.security_system.model;

import java.time.LocalDateTime;

public class LogEntry {

    public String ip;
    public String endpoint;
    public String username;
    public String password;
    public String userAgent;
    public LocalDateTime timestamp;
    public String tag; // Normal / Suspicious

    public LogEntry(String ip, String endpoint, String username,
                    String password, String userAgent, String tag) {

        this.ip = ip;
        this.endpoint = endpoint;
        this.username = username;
        this.password = password;
        this.userAgent = userAgent;
        this.timestamp = LocalDateTime.now(); // auto time
        this.tag = tag;
    }
}