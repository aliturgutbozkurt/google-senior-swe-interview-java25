package com.google.prep.algorithms;

import com.google.prep.algorithms.graph.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphAlgorithmsTest {

    @Test
    @DisplayName("TarjanSCC: Correctly identifies strongly connected components in directed graph")
    void testTarjanScc() {
        TarjanSCC graph = new TarjanSCC(8);
        // Component 1: 0 -> 1 -> 2 -> 0
        graph.addDirectedEdge(0, 1);
        graph.addDirectedEdge(1, 2);
        graph.addDirectedEdge(2, 0);

        // Component 2: 3 -> 4 -> 3
        graph.addDirectedEdge(2, 3);
        graph.addDirectedEdge(3, 4);
        graph.addDirectedEdge(4, 3);

        // Component 3: 5 -> 6 -> 7 -> 5
        graph.addDirectedEdge(4, 5);
        graph.addDirectedEdge(5, 6);
        graph.addDirectedEdge(6, 7);
        graph.addDirectedEdge(7, 5);

        List<List<Integer>> sccs = graph.findStronglyConnectedComponents();
        assertThat(sccs).hasSize(3);
    }

    @Test
    @DisplayName("TarjanSCC: Correctly detects critical bridges in undirected graph")
    void testTarjanBridges() {
        TarjanSCC graph = new TarjanSCC(5);
        // Triangle 0-1-2 with bridge 1-3, and edge 3-4
        graph.addUndirectedEdge(0, 1, 1);
        graph.addUndirectedEdge(1, 2, 2);
        graph.addUndirectedEdge(2, 0, 3);
        graph.addUndirectedEdge(1, 3, 4); // Bridge
        graph.addUndirectedEdge(3, 4, 5); // Bridge

        List<TarjanSCC.Bridge> bridges = graph.findBridges();
        assertThat(bridges).hasSize(2);
    }

    @Test
    @DisplayName("DinicMaxFlow: Computes correct maximum flow and minimum cut")
    void testDinicMaxFlow() {
        DinicMaxFlow flowNetwork = new DinicMaxFlow(6);
        // Source = 0, Sink = 5
        flowNetwork.addEdge(0, 1, 10);
        flowNetwork.addEdge(0, 2, 10);
        flowNetwork.addEdge(1, 2, 2);
        flowNetwork.addEdge(1, 3, 4);
        flowNetwork.addEdge(1, 4, 8);
        flowNetwork.addEdge(2, 4, 9);
        flowNetwork.addEdge(3, 5, 10);
        flowNetwork.addEdge(4, 5, 10);

        long maxFlow = flowNetwork.maxFlow(0, 5);
        assertThat(maxFlow).isEqualTo(14);

        boolean[] minCut = flowNetwork.minCut(0);
        assertThat(minCut[0]).isTrue();
        assertThat(minCut[5]).isFalse();
    }

    @Test
    @DisplayName("Dijkstra: Computes single-source shortest path and reconstructs trajectory")
    void testDijkstra() {
        DijkstraShortestPath dijkstra = new DijkstraShortestPath(6);
        dijkstra.addEdge(0, 1, 7);
        dijkstra.addEdge(0, 2, 9);
        dijkstra.addEdge(0, 5, 14);
        dijkstra.addEdge(1, 2, 10);
        dijkstra.addEdge(1, 3, 15);
        dijkstra.addEdge(2, 3, 11);
        dijkstra.addEdge(2, 5, 2);
        dijkstra.addEdge(3, 4, 6);
        dijkstra.addEdge(4, 5, 9);

        var result = dijkstra.computeShortestPaths(0);
        assertThat(result.distanceTo(4)).isEqualTo(26); // Path: 0 -> 2 -> 3 -> 4: 9 + 11 + 6 = 26
        assertThat(result.pathTo(4)).containsExactly(0, 2, 3, 4);
    }

    @Test
    @DisplayName("TopologicalSorter: Handles DAG ordering and detects directed cycles")
    void testTopologicalSorter() {
        // Case 1: Valid DAG
        TopologicalSorter dag = new TopologicalSorter(4);
        dag.addEdge(0, 1);
        dag.addEdge(0, 2);
        dag.addEdge(1, 3);
        dag.addEdge(2, 3);

        var resDag = dag.sort();
        assertThat(resDag.isDag()).isTrue();
        assertThat(resDag.order()).containsExactly(0, 2, 1, 3);

        // Case 2: Graph with cycle
        TopologicalSorter cyclic = new TopologicalSorter(3);
        cyclic.addEdge(0, 1);
        cyclic.addEdge(1, 2);
        cyclic.addEdge(2, 0);

        var resCyclic = cyclic.sort();
        assertThat(resCyclic.isDag()).isFalse();
        assertThat(resCyclic.cycle()).isNotEmpty();
    }
}
