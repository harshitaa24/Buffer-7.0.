import requests
import random
import time
from datetime import datetime


BASE_URL = "http://localhost:8080"


# ============================================================
# ATTACKER DATA
# ============================================================

IPS = [
    "192.168.1.10",
    "10.0.0.5",
    "172.16.0.7",
    "203.0.113.9",
    "45.33.32.156",
    "103.21.244.0"
]

USER_AGENTS = {
    "browser": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/151.0",
    "curl": "curl/7.68.0",
    "python": "python-requests/2.28"
}

USERNAMES = [
    "admin",
    "root",
    "administrator",
    "test",
    "guest"
]

PASSWORDS = [
    "1234",
    "admin",
    "root",
    "toor",
    "password",
    "letmein",
    "qwerty"
]

SQL_PAYLOADS = [
    "' OR '1'='1",
    "' OR 1=1 --",
    "admin' --",
    "' OR 'a'='a"
]


# ============================================================
# SESSION
# ============================================================

session = requests.Session()


# ============================================================
# LOGGING
# ============================================================

def log(message):
    now = datetime.now().strftime("%H:%M:%S")
    print(f"[{now}] {message}")


# ============================================================
# HEADERS
# ============================================================

def get_headers(ip, agent="browser"):

    return {
        "User-Agent": USER_AGENTS[agent],
        "X-Forwarded-For": ip
    }


# ============================================================
# LOGIN
# ============================================================

def send_login(ip, username, password, agent="browser"):

    try:

        session.post(
            f"{BASE_URL}/login",
            headers=get_headers(ip, agent),
            data={
                "username": username,
                "password": password
            },
            timeout=2
        )

        log(
            f"{ip} → POST /login "
            f"(user={username})"
        )

    except requests.RequestException:
        log(f"{ip} → /login failed")


# ============================================================
# GENERIC REQUEST
# ============================================================

def hit(ip, endpoint, method="GET", agent="browser"):

    try:

        if method == "POST":

            session.post(
                f"{BASE_URL}{endpoint}",
                headers=get_headers(ip, agent),
                timeout=2
            )

        else:

            session.get(
                f"{BASE_URL}{endpoint}",
                headers=get_headers(ip, agent),
                timeout=2
            )

        log(f"{ip} → {method} {endpoint}")

    except requests.RequestException:

        log(f"{ip} → {method} {endpoint} failed")


# ============================================================
# FILE DISCOVERY
# ============================================================

def get_files(ip, agent="python"):

    try:

        response = session.get(
            f"{BASE_URL}/api/files",
            headers=get_headers(ip, agent),
            timeout=2
        )

        if response.status_code == 200:

            files = response.json()

            log(
                f"{ip} → GET /api/files "
                f"[{len(files)} files discovered]"
            )

            return files

    except requests.RequestException:
        pass

    return []


# ============================================================
# BRUTE FORCE
# ============================================================

def brute_force(ip):

    log(f"🔐 {ip} starting credential attack")

    for password in PASSWORDS:

        send_login(
            ip,
            "admin",
            password,
            "python"
        )

        time.sleep(random.uniform(0.25, 0.6))


# ============================================================
# SQL INJECTION
# ============================================================

def sql_attack(ip):

    log(f"💉 {ip} probing login for SQL injection")

    for payload in SQL_PAYLOADS:

        send_login(
            ip,
            payload,
            "1234",
            "python"
        )

        time.sleep(random.uniform(0.3, 0.7))


# ============================================================
# RATE LIMIT ATTACK
# ============================================================

def rate_attack(ip):

    log(f"⚡ {ip} generating rapid requests")

    for i in range(15):

        send_login(
            ip,
            "admin",
            "1234",
            "python"
        )

        time.sleep(0.05)


# ============================================================
# RECONNAISSANCE
# ============================================================

def reconnaissance(ip):

    log(f"🔎 {ip} performing reconnaissance")

    # Discover available files
    files = get_files(ip, "python")

    time.sleep(random.uniform(0.5, 1.0))

    return files


# ============================================================
# SYSTEM EXPLORATION
# ============================================================

def explore_system(ip):

    log(f"🧭 {ip} probing administrative endpoints")

    endpoints = [
        ("/api/query", "POST"),
        ("/api/sync", "POST"),
        ("/api/restart", "POST")
    ]

    # Don't hit everything every time.
    selected = random.sample(
        endpoints,
        random.randint(1, 2)
    )

    for endpoint, method in selected:

        hit(
            ip,
            endpoint,
            method,
            "curl"
        )

        time.sleep(
            random.uniform(0.5, 1.2)
        )


# ============================================================
# DATA COLLECTION
# ============================================================

def collect_files(ip, files):

    if not files:

        return

    log(f"📂 {ip} attempting file collection")

    chosen = random.sample(
        files,
        min(
            len(files),
            random.randint(1, 3)
        )
    )

    for file in chosen:

        endpoint = (
            "/api/download?file="
            + str(file)
        )

        hit(
            ip,
            endpoint,
            "GET",
            "curl"
        )

        time.sleep(
            random.uniform(0.5, 1.0)
        )


# ============================================================
# DESTRUCTIVE / CLEANUP ACTION
# ============================================================

def cleanup(ip):

    if random.random() < 0.35:

        log(f"🧹 {ip} attempting cleanup")

        hit(
            ip,
            "/api/clear-logs",
            "POST",
            "curl"
        )

        time.sleep(
            random.uniform(0.5, 1.0)
        )


# ============================================================
# ATTACK CAMPAIGN
# ============================================================

def attacker_campaign(ip, attack_type):

    print()
    print("=" * 65)

    log(
        f"🚨 NEW ATTACK CAMPAIGN"
    )

    log(
        f"Source IP : {ip}"
    )

    log(
        f"Attack type : {attack_type}"
    )

    print("=" * 65)

    # --------------------------------------------------------
    # PHASE 1 — RECONNAISSANCE
    # --------------------------------------------------------

    files = reconnaissance(ip)

    time.sleep(
        random.uniform(0.8, 1.5)
    )

    # --------------------------------------------------------
    # PHASE 2 — CREDENTIAL ATTACK
    # --------------------------------------------------------

    if attack_type == "BRUTE":

        brute_force(ip)

    elif attack_type == "SQL":

        sql_attack(ip)

    elif attack_type == "RATE":

        rate_attack(ip)

    else:

        # Random attacker behaviour
        attack = random.choice([
            brute_force,
            sql_attack,
            rate_attack
        ])

        attack(ip)

    time.sleep(
        random.uniform(0.8, 1.5)
    )

    # --------------------------------------------------------
    # PHASE 3 — SYSTEM EXPLORATION
    # --------------------------------------------------------

    explore_system(ip)

    time.sleep(
        random.uniform(0.8, 1.5)
    )

    # --------------------------------------------------------
    # PHASE 4 — DATA COLLECTION
    # --------------------------------------------------------

    collect_files(ip, files)

    time.sleep(
        random.uniform(0.8, 1.5)
    )

    # --------------------------------------------------------
    # PHASE 5 — OPTIONAL CLEANUP
    # --------------------------------------------------------

    cleanup(ip)

    log(
        f"🏁 Campaign from {ip} completed"
    )


# ============================================================
# MAIN SIMULATION
# ============================================================

def simulate():

    print()
    print("=" * 65)
    print("        HONEYPOT ATTACK SIMULATOR")
    print("=" * 65)
    print()

    # Guarantee three different attack behaviours
    campaigns = [
        ("BRUTE", IPS[0]),
        ("SQL", IPS[1]),
        ("RATE", IPS[2])
    ]

    # Additional realistic campaigns
    remaining_ips = IPS[3:]

    for ip in remaining_ips:

        campaigns.append(
            (
                random.choice([
                    "BRUTE",
                    "SQL",
                    "RATE"
                ]),
                ip
            )
        )

    # Randomize campaign order
    random.shuffle(campaigns)

    for index, (attack_type, ip) in enumerate(campaigns):

        print()

        log(
            f"Campaign {index + 1}/{len(campaigns)}"
        )

        attacker_campaign(
            ip,
            attack_type
        )

        # Pause between attackers
        time.sleep(
            random.uniform(2.0, 4.0)
        )

    print()
    print("=" * 65)
    print("        SIMULATION COMPLETE")
    print("=" * 65)


# ============================================================
# ENTRY POINT
# ============================================================

if __name__ == "__main__":

    simulate()
