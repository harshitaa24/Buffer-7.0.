# 🔐 Intelligent Honeypot-Based Security System with Attack Detection

---
## 🚀 Demo

🔗 [Click here to view project demo](https://drive.google.com/file/d/1-rUZf4Q0koSLeQvX30dlFgNAe-bTTULS/view?usp=sharing)

## What is a Honeypot?

A **honeypot** is a cybersecurity mechanism designed to attract attackers by simulating vulnerable systems. Instead of directly protecting real systems, it acts as a decoy environment where malicious activities can be safely observed and analyzed.

### 💡 Concept Of This Project

In this project:
- Fake endpoints (like login pages) are exposed  
- Suspicious users are redirected to a honeypot  
- Their actions are recorded & analysed for pattern detection
- A graph-based system tracks how attacks evolve  

The system focuses on monitoring attacker behavior rather than immediately blocking it.

---

## 🚀 Features

- Honeypot Simulation  
  Fake login system to trap attackers and capture credentials safely  

- Login Tries Tracking  
  Tracks number of login attempts per IP/user and detects brute-force attacks  

- Attack Detection  
  Identifies suspicious patterns such as SQL injections, Random tries & Rate Limiting  

- Attack Graph Tracking  
  Stores request flow using a graph and analyzes it using BFS  

- Logging System  
  Records IP, endpoint, username, password, tries count, timestamp, and tag  

---

## 📌Tech Stack

### 1] Data Structures Used

- Graph – Attack flow tracking  
- Trie – Pattern detection  
- Queue – BFS traversal  
- HashMap & HashSet – Store IPs and number of attempts  
- Sliding Window – Timestamp-based activity tracking  

### Backend

- Java, Spring Boot (REST APIs)  
- Spring MVC (Controllers & routing)  
- Service Layer (Attack detection, Honeypot logic, BFS-based attack graph, Fake DB)  
- Data Storage: HashMap, List + file logging  
- Logging: IP, endpoint, credentials, tries, timestamp, tag, status  

### Frontend
- HTML  
- CSS  
- JavaScript (Admin Dashboard UI)  

---
## ⚙️ Working

The system begins by capturing each incoming request, such as a login attempt, along with essential details including IP address, username, user agent and timestamp. It maintains a count of login attempts using HashMap-based storage to identify repeated access patterns while a sliding window mechanism analyzes request frequency over time to detect abnormal or rapid activity. For efficient pattern recognition a Trie data structure is utilized to identify repeated or suspicious input sequences. 
All requests are simultaneously recorded in a graph structure that models the flow of actions performed by a user. This graph is analyzed using Breadth-First Search (BFS) to understand the sequence and progression of potential attack paths. If the system detects suspicious behavior such as excessive login tries, unusual request patterns or anomalies in access behavior then the user is redirected to a honeypot environment designed to safely capture and study malicious activity. Every interaction is logged with detailed metadata and classified as normal or suspicious, and the admin dashboard provides a centralized interface for monitoring, analysis, and decision-making.

---

## 💼 Applications

- **Cybersecurity Systems** – Detection and analysis of intrusion attempts  
- **Banking & Financial Platforms** – Prevention of brute-force and unauthorized login attacks  
- **Web Applications** – Monitoring bots, credential stuffing, and abnormal traffic  
- **Enterprise Security** – Tracking internal threats and suspicious user behavior  
- **Research & Education** – Studying real-world attack patterns in a controlled environment  
---
