package com.example.security_system.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AttackGraph {

    // ================================
    // INNER CLASS
    // ================================
    static class RequestNode {
        String endpoint;
        LocalDateTime time;

        RequestNode(String endpoint) {
            this.endpoint = endpoint;
            this.time = LocalDateTime.now();
        }
    }

    // ================================
    // DATA STORE
    // ================================
    private Map<String, List<RequestNode>> attackPaths = new HashMap<>();


    // ================================
    // RECORD REQUEST
    // ================================
    public void recordRequest(String ip, String endpoint) {
        attackPaths.putIfAbsent(ip, new ArrayList<>());
        attackPaths.get(ip).add(new RequestNode(endpoint));
    }


    // ================================
    // GET FULL PATH
    // ================================
    public List<String> getPath(String ip) {
        List<RequestNode> nodes = attackPaths.getOrDefault(ip, new ArrayList<>());
        List<String> path = new ArrayList<>();

        for (RequestNode n : nodes) {
            path.add(n.endpoint);
        }

        return path;
    }


    // ================================
    // BUILD ADJACENCY LIST (FOR BFS)
    // ================================
    public Map<String, List<String>> buildAdjacencyList() {

        Map<String, List<String>> graph = new HashMap<>();

        for (String ip : attackPaths.keySet()) {

            List<RequestNode> path = attackPaths.get(ip);

            for (int i = 0; i < path.size() - 1; i++) {

                String from = path.get(i).endpoint;
                String to = path.get(i + 1).endpoint;

                graph.putIfAbsent(from, new ArrayList<>());
                graph.get(from).add(to);
            }
        }

        return graph;
    }


    // ================================
    // BFS → SHORTEST PATH
    // ================================
    public List<String> bfsShortestPath(String start, String target) {

        Map<String, List<String>> graph = buildAdjacencyList();

        Queue<List<String>> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();

        queue.add(Arrays.asList(start));

        while (!queue.isEmpty()) {

            List<String> path = queue.poll();
            String last = path.get(path.size() - 1);

            if (last.equals(target)) {
                return path;
            }

            if (!visited.contains(last)) {
                visited.add(last);

                for (String neighbor : graph.getOrDefault(last, new ArrayList<>())) {

                    List<String> newPath = new ArrayList<>(path);
                    newPath.add(neighbor);

                    queue.add(newPath);
                }
            }
        }

        return new ArrayList<>();
    }


    // ================================
    // ROUTES (MAIN FEATURE)
    // ================================
    public Map<String, List<List<String>>> getRoutes() {

        Map<String, List<List<String>>> result = new HashMap<>();

        for (String ip : attackPaths.keySet()) {

            List<RequestNode> path = attackPaths.get(ip);

            List<List<String>> routes = new ArrayList<>();
            List<String> currentRoute = new ArrayList<>();

            for (int i = 0; i < path.size(); i++) {

                currentRoute.add(path.get(i).endpoint);

                if (currentRoute.size() >= 5 || path.get(i).endpoint.equals("/login")) {

                    routes.add(new ArrayList<>(currentRoute));
                    currentRoute.clear();
                }
            }

            if (!currentRoute.isEmpty()) {
                routes.add(currentRoute);
            }

            result.put(ip, routes);
        }

        return result;
    }
}
