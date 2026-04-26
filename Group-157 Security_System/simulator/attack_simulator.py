import requests
import random
import time

BASE_URL = "http://localhost:8080"

# =========================
# DATA
# =========================

IPS = [
    "192.168.1.10",
    "10.0.0.5",
    "172.16.0.7",
    "203.0.113.9",
    "45.33.32.156",
    "103.21.244.0"
]

USER_AGENTS = [
    "Mozilla/5.0",
    "curl/7.68.0",
    "python-requests/2.28"
]

USERNAMES = ["admin", "root", "test", "guest"]
PASSWORDS = ["1234", "admin", "root", "toor", "password", "letmein"]

SQL_PAYLOADS = [
    "' OR '1'='1",
    "' OR 1=1 --",
    "admin' --",
    "' OR 'a'='a"
]


# =========================
# HELPERS
# =========================

def get_headers(ip):
    return {
        "User-Agent": random.choice(USER_AGENTS),
        "X-Forwarded-For": ip
    }


def send_login(ip, username, password):
    try:
        requests.post(
            f"{BASE_URL}/login",
            headers=get_headers(ip),
            data={"username": username, "password": password},
            timeout=2
        )
    except:
        pass


def hit(ip, endpoint, method="GET"):
    try:
        if method == "POST":
            requests.post(f"{BASE_URL}{endpoint}", headers=get_headers(ip), timeout=2)
        else:
            requests.get(f"{BASE_URL}{endpoint}", headers=get_headers(ip), timeout=2)
    except:
        pass


def get_files(ip):
    try:
        res = requests.get(f"{BASE_URL}/api/files", headers=get_headers(ip), timeout=2)
        if res.status_code == 200:
            return res.json()
    except:
        pass
    return []


# =========================
# ATTACK TYPES
# =========================

def brute_force(ip):
    for pwd in PASSWORDS:
        send_login(ip, "admin", pwd)
        time.sleep(0.2)


def sql_attack(ip):
    for payload in SQL_PAYLOADS:
        send_login(ip, payload, "1234")
        time.sleep(0.3)


def rate_attack(ip):
    for _ in range(15):
        send_login(ip, "admin", "1234")
        time.sleep(0.05)


# =========================
# ATTACKER JOURNEY
# =========================

def attacker_journey(ip):

    # 🔍 Step 1: reconnaissance (get file list)
    files = get_files(ip)

    # ⚙ Step 2: perform system actions
    actions = [
        ("/api/restart", "POST"),
        ("/api/delete-all", "POST"),
        ("/api/query", "POST"),
        ("/api/sync", "POST"),
        ("/api/clear-logs", "POST"),
        ("/api/reset", "POST")
    ]

    for endpoint, method in random.sample(actions, random.randint(1, 2)):
        hit(ip, endpoint, method)
        time.sleep(0.2)

    # 📂 Step 3: download sensitive files
    if files:
        chosen_files = random.sample(files, min(len(files), random.randint(1, 3)))
        for file in chosen_files:
            hit(ip, f"/api/download?file={file}", "GET")
            time.sleep(0.2)

    # 🔁 Step 4: optional repeat action
    if random.random() < 0.5:
        hit(ip, "/api/query", "POST")
        time.sleep(0.2)


# =========================
# MAIN SIMULATION
# =========================

def simulate():

    print("🚀 Starting Attack Simulation...\n")

    attackers = []

    # ✅ guarantee all 3 attack types
    attackers.append(("BRUTE", brute_force))
    attackers.append(("SQL", sql_attack))
    attackers.append(("RATE", rate_attack))

    # remaining attackers → random
    for _ in range(12):
        attackers.append(("RANDOM", random.choice([brute_force, sql_attack, rate_attack])))

    random.shuffle(attackers)

    for i, (name, attack_fn) in enumerate(attackers):

        ip = random.choice(IPS)

        print(f"[{i+1}] {name} attacker from {ip}")

        # Step 1: perform login attack
        attack_fn(ip)

        # Step 2: simulate movement
        attacker_journey(ip)

        time.sleep(random.uniform(0.5, 1.5))


if __name__ == "__main__":
    simulate()