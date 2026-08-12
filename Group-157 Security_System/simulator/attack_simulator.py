<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Attack Routes</title>

    <style>
        body {
            margin: 0;
            font-family: "Segoe UI", Arial, sans-serif;
            background: #0f172a;
            color: #e2e8f0;
        }

        .layout {
            display: flex;
            min-height: 100vh;
        }

        .sidebar {
            width: 220px;
            background: #0f172a;
            border-right: 1px solid #334155;
            padding: 10px;
        }

        .nav-item {
            padding: 10px;
            margin: 6px 0;
            border-radius: 8px;
            cursor: pointer;
        }

        .nav-item:hover {
            background: #1e293b;
        }

        .nav-item.active {
            background: #3b82f6;
            color: white;
        }

        .content {
            flex: 1;
            padding: 20px;
        }

        .card {
            background: #1e293b;
            padding: 16px;
            border-radius: 12px;
        }

        .ip-block {
            margin-bottom: 20px;
            padding: 12px;
            border-radius: 10px;
            background: rgba(15, 23, 42, 0.7);
            border: 1px solid #334155;
        }

        .ip-title {
            font-weight: bold;
            margin-bottom: 8px;
            color: #60a5fa;
        }

        .route {
            font-family: monospace;
            padding: 6px 10px;
            margin: 4px 0;
            border-radius: 6px;
            background: #020617;
        }

        .danger { color: #ef4444; }
        .warn { color: #f59e0b; }
        .safe { color: #22c55e; }

        .status {
            margin-top: 10px;
            color: #94a3b8;
        }

        .loading {
            color: #60a5fa;
        }
    </style>
</head>

<body>

<div class="layout">

    <aside class="sidebar">
        <div class="nav-item" onclick="location.href='dashboard.html'">Dashboard</div>
        <div class="nav-item" onclick="location.href='logs.html'">Logs</div>
        <div class="nav-item" onclick="location.href='geo.html'">Geo</div>
        <div class="nav-item" onclick="location.href='attack.html'">Attack Graph</div>
        <div class="nav-item" onclick="location.href='profile.html'">Admin Profile</div>  
        <div class="nav-item active">Attack Routes</div>
    </aside>

    <main class="content">
        <div class="card">
            <h2>Attack Routes</h2>
            <p>Actual attacker paths (no visualization noise)</p>

            
            <div style="margin-bottom: 20px;">
                <h3>🔍 Shortest Attack Path (BFS)</h3>

                <input type="text" id="startNode" placeholder="Start (e.g. /login)" />
                <input type="text" id="targetNode" placeholder="Target (e.g. DELETE_ALL)" />

                <button onclick="getShortestPath()">Find Path</button>

                <div id="bfsResult" style="margin-top:10px; font-weight:bold;"></div>
            </div>

            <div id="status" class="status loading">Loading...</div>
            <div id="routesContainer"></div>
        </div>
    </main>

</div>

<script>

// ================================
// BACKEND URL
// ================================
const API_BASE = "http://localhost:8080";


// ================================
// BFS FUNCTION
// ================================
async function getShortestPath() {

    const start = document.getElementById("startNode").value.trim();
    const target = document.getElementById("targetNode").value.trim();
    const resultDiv = document.getElementById("bfsResult");

    if (!start || !target) {
        resultDiv.innerText = "⚠️ Please enter both start and target.";
        return;
    }

    try {

        const res = await fetch(
            `${API_BASE}/api/bfs?start=${encodeURIComponent(start)}&target=${encodeURIComponent(target)}`
        );

        if (!res.ok) {
            throw new Error("Backend returned " + res.status);
        }

        const data = await res.json();

        if (!data || data.length === 0) {
            resultDiv.innerText = "❌ No path found.";
        } else {
            resultDiv.innerText = "✅ " + data.join(" → ");
        }

    } catch (err) {

        console.error("BFS Error:", err);
        resultDiv.innerText = "❌ Error connecting to backend.";

    }
}


// ================================
// LOAD ATTACK ROUTES
// ================================
async function loadRoutes() {

    const status = document.getElementById("status");
    const container = document.getElementById("routesContainer");

    try {

        status.textContent = "Loading...";
        status.className = "status loading";

        const res = await fetch(`${API_BASE}/api/routes`);

        if (!res.ok) {
            throw new Error("Backend returned " + res.status);
        }

        const data = await res.json();

        console.log("Routes API:", data);

        container.innerHTML = "";

        if (!data || Object.keys(data).length === 0) {

            status.textContent =
                "No attack data yet. Trigger some activity.";

            return;
        }

        status.textContent = "Live data updated";

        Object.keys(data).forEach(ip => {

            const block = document.createElement("div");
            block.className = "ip-block";

            const title = document.createElement("div");
            title.className = "ip-title";
            title.textContent = "IP: " + ip;

            block.appendChild(title);

            // Sort routes by length
            const sortedRoutes = [...data[ip]]
                .sort((a, b) => b.length - a.length);

            let displayIndex = 1;

for (let i = 0; i < sortedRoutes.length; i++) {

    const route = sortedRoutes[i];

    // Count consecutive /login routes
    if (route.length === 1 && route[0] === "/login") {

        let count = 1;

        while (
            i + count < sortedRoutes.length &&
            sortedRoutes[i + count].length === 1 &&
            sortedRoutes[i + count][0] === "/login"
        ) {
            count++;
        }

        const routeEl = document.createElement("div");
        routeEl.className = "route safe";

        routeEl.textContent =
            `#${displayIndex}: /login × ${count}`;

        block.appendChild(routeEl);

        displayIndex++;
        i += count - 1;

        continue;
    }

    const routeEl = document.createElement("div");
    routeEl.className = "route";

    const routeText = route.join(" → ");

    routeEl.textContent =
        `#${displayIndex}: ${routeText}`;

    if (routeText.includes("DELETE_ALL")) {

        routeEl.classList.add("danger");

    } else if (routeText.includes("DOWNLOAD")) {

        routeEl.classList.add("warn");

    } else {

        routeEl.classList.add("safe");
    }

    block.appendChild(routeEl);

    displayIndex++;
}

            container.appendChild(block);
        });

    } catch (err) {

        console.error("Error loading routes:", err);

        status.textContent =
            "Error connecting to Spring Boot backend.";

        status.className = "status";
    }
}


// ================================
// REFRESH EVERY 5 SECONDS
// ================================
setInterval(loadRoutes, 5000);


// ================================
// INITIAL LOAD
// ================================
loadRoutes();


</script>

</body>
</html>
