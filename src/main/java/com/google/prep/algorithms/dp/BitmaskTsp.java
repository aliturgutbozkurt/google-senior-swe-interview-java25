package com.google.prep.algorithms.dp;

import java.util.*;

/**
 * Traveling Salesperson Problem (TSP) using Bitmask Dynamic Programming.
 *
 * <p>Solves shortest Hamiltonian cycle in O(2^N * N^2) time and O(2^N * N) space.
 * Includes complete path reconstruction of the optimal tour.
 */
public final class BitmaskTsp {

    public record TspResult(double minCost, List<Integer> tour) {}

    /**
     * Computes the minimum cost Hamiltonian cycle starting and ending at vertex 0.
     *
     * @param dist NxN distance matrix where dist[i][j] is the travel cost from i to j.
     */
    public static TspResult solveTsp(double[][] dist) {
        int n = dist.length;
        if (n <= 1) {
            return new TspResult(0.0, List.of(0));
        }
        if (n > 20) {
            throw new IllegalArgumentException("N > 20 exceeds practical memory for 2^N state space: " + n);
        }

        int fullMask = (1 << n) - 1;
        double[][] memo = new double[1 << n][n];
        int[][] nextCity = new int[1 << n][n];

        for (double[] row : memo) {
            Arrays.fill(row, -1.0);
        }

        double minCost = tspDfs(1, 0, n, fullMask, dist, memo, nextCity);

        // Reconstruct the tour
        List<Integer> tour = new ArrayList<>();
        int currMask = 1;
        int currCity = 0;
        tour.add(0);

        for (int step = 1; step < n; step++) {
            int next = nextCity[currMask][currCity];
            tour.add(next);
            currMask |= (1 << next);
            currCity = next;
        }
        tour.add(0); // Return to start

        return new TspResult(minCost, tour);
    }

    private static double tspDfs(int mask, int u, int n, int fullMask,
                                 double[][] dist, double[][] memo, int[][] nextCity) {
        if (mask == fullMask) {
            return dist[u][0]; // Return cost to starting vertex
        }
        if (memo[mask][u] != -1.0) {
            return memo[mask][u];
        }

        double bestCost = Double.MAX_VALUE;
        int bestNext = -1;

        for (int v = 0; v < n; v++) {
            if ((mask & (1 << v)) == 0) { // If city v not visited
                double cost = dist[u][v] + tspDfs(mask | (1 << v), v, n, fullMask, dist, memo, nextCity);
                if (cost < bestCost) {
                    bestCost = cost;
                    bestNext = v;
                }
            }
        }

        nextCity[mask][u] = bestNext;
        return memo[mask][u] = bestCost;
    }
}
