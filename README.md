# 🔐 Intelligent Honeypot-Based Security System

A Spring Boot-based honeypot security system designed to detect, classify, and monitor suspicious login activity. The system simulates a vulnerable login environment, identifies common attack patterns, tracks attacker behavior, and provides an admin dashboard for monitoring security events.

## 🚀 Demo

🔗 [View Project Demo](https://drive.google.com/file/d/1-rUZf4Q0koSLeQvX30dlFgNAe-bTTULS/view?usp=sharing)

---

## 📖 Overview

A **honeypot** is a security mechanism that intentionally exposes a simulated vulnerable environment to attract and observe malicious activity.

This project implements a fake login system that acts as a controlled honeypot. Incoming login requests are analyzed based on:

- Login attempt frequency
- Repeated password usage
- Suspicious input patterns
- Automated request indicators
- Request timing
- User/IP behavior

Detected activity is classified and recorded in the security logs, allowing administrators to analyze attack patterns through a web-based dashboard.

---

## 🎯 Objectives

The main objectives of the project are:

- Detect suspicious login behavior
- Identify common attack patterns such as SQL Injection and XSS
- Detect brute-force and rate-limiting attacks
- Identify automated requests and bot activity
- Track attacker request flows using a graph
- Maintain detailed security logs
- Provide an admin dashboard for real-time monitoring
- Demonstrate the use of data structures in a practical cybersecurity application

---

## ✨ Features

### 🔐 Honeypot Login System

A simulated login endpoint is used as a decoy environment for capturing suspicious requests.

The system records information such as:

- IP address
- Username
- Password
- User-Agent
- Endpoint
- Timestamp
- Detection type
- Request status

---

### 🛡️ Attack Detection

The detection engine classifies incoming requests into categories including:

| Detection Type | Description |
|---|---|
| `SQL Injection` | Detects known SQL injection patterns |
| `XSS` | Detects script-based payloads |
| `Brute Force` | Detects repeated login attempts with different passwords |
| `Rate Limiting` | Detects excessive requests within a short time window |
| `Bot Activity` | Detects automated clients such as curl or Python scripts |
| `Normal` | No suspicious behavior detected |

---
## 🧠 DSA Used

- **HashMap** – Stores IP addresses, attempts, user-password mappings, and attack patterns
- **HashSet** – Tracks unique passwords for brute-force detection
- **Queue** – Maintains request timestamps for rate limiting
- **Graph** – Represents attacker request flow
- **BFS** – Traverses the attack graph to analyze attack progression
- **Sliding Window** – Detects excessive requests within a fixed time interval
---
