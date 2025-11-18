/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import com.graphhopper.coll.GHIntLongHashMap;
import com.graphhopper.routing.AStar;
import com.graphhopper.routing.DijkstraBidirectionRef;
import com.graphhopper.routing.Path;
import com.graphhopper.routing.ev.BooleanEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValue;
import com.graphhopper.routing.ev.DecimalEncodedValueImpl;
import com.graphhopper.routing.ev.SimpleBooleanEncodedValue;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TraversalMode;
import com.graphhopper.routing.weighting.SpeedWeighting;
import com.graphhopper.routing.weighting.Weighting;
import com.graphhopper.storage.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Peter Karich
 */
public class GHUtilityTest {
    final int DEFAULT_SIZE = 100;

    public record Node(int nodeId, double lat, double lon, double ele) { }
    public record Edge(int nodeA, int nodeB, int distance) { }

    @Test
    public void testEdgeStuff() {
        assertEquals(2, GHUtility.createEdgeKey(1, false));
        assertEquals(3, GHUtility.createEdgeKey(1, true));
    }

    @Test
    public void testZeroValue() {
        GHIntLongHashMap map1 = new GHIntLongHashMap();
        assertFalse(map1.containsKey(0));
        // assertFalse(map1.containsValue(0));
        map1.put(0, 3);
        map1.put(1, 0);
        map1.put(2, 1);

        // assertTrue(map1.containsValue(0));
        assertEquals(3, map1.get(0));
        assertEquals(0, map1.get(1));
        assertEquals(1, map1.get(2));

        // instead of assertEquals(-1, map1.get(3)); with hppc we have to check before:
        assertTrue(map1.containsKey(0));

        // trove4j behaviour was to return -1 if non existing:
//        TIntLongHashMap map2 = new TIntLongHashMap(100, 0.7f, -1, -1);
//        assertFalse(map2.containsKey(0));
//        assertFalse(map2.containsValue(0));
//        map2.add(0, 3);
//        map2.add(1, 0);
//        map2.add(2, 1);
//        assertTrue(map2.containsKey(0));
//        assertTrue(map2.containsValue(0));
//        assertEquals(3, map2.get(0));
//        assertEquals(0, map2.get(1));
//        assertEquals(1, map2.get(2));
//        assertEquals(-1, map2.get(3));
    }

    @Test
    public void testGetProblems() {
        Directory dir = new RAMDirectory();
        List<Edge> edges = List.of(
                new Edge(0, 1, 200),
                new Edge(0, 2, 300)
        );

        // Création des noeuds de manière safe
        try (BaseGraph graph = new BaseGraph(dir, true, true, 100, 8)) {
            graph.create(DEFAULT_SIZE);

            // Avec problèmes
            List<Node> nodes = List.of(
                    new Node(0, 100.0, 0.0, 0.0), // incorrect
                    new Node(1, 90.0, 0.0, 50.0), // correct
                    new Node(2, 40.0, -190.0, 100.0) // incorrect
            );
            setNodes(graph, nodes); // création des noeuds
            setEdges(graph, edges, null); // création des arêtes

            List<String> problems = GHUtility.getProblems(graph);
            assertFalse(problems.isEmpty(), "Devrait retourner des erreurs à propos des coordonnées");

            // Aucun problèmes
            nodes = List.of(
                    new Node(0, 80.0, 0.0, 0.0), // correct
                    new Node(1, 90.0, 0.0, 50.0), // correct
                    new Node(2, 40.0, -170.0, 100.0) // correct
            );
            setNodes(graph, nodes); // création des noeuds
            problems = GHUtility.getProblems(graph);
            assertTrue(problems.isEmpty(), "Devrait ne pas avoir de messages d'erreur");
        }
    }

    // Fonction réutilisable
    private void setNodes(BaseGraph graph, List<Node> nodes) {
        NodeAccess na = graph.getNodeAccess();
        for (Node node : nodes) {
            na.setNode(node.nodeId, node.lat, node.lon, node.ele);
        }
    }

    // Fonction réutilisable
    private List<EdgeIteratorState> setEdges(BaseGraph graph, List<Edge> edges, DecimalEncodedValue speedEnc) {
        List<EdgeIteratorState> eiss = new ArrayList<>();
        for (Edge edge : edges) {
            EdgeIteratorState eis = graph.edge(edge.nodeA, edge.nodeB);
            eis.setDistance(edge.distance);
            if (speedEnc != null) { eis.set(speedEnc, 10, 10); }
            eiss.add(eis);
        }
        return eiss;
    }

    @Test
    public void testPathsEqualExceptOneEdge() {
        // Stock la vitesse pour calculer le coût
        DecimalEncodedValue speedEnc = new DecimalEncodedValueImpl("speed", 5, 5, true);
        EncodingManager encodingManager = new EncodingManager.Builder().add(speedEnc).build();
        List<Node> nodes = List.of(
                new Node(0, 0.0, -90.0, 0.0),
                new Node(1, 90.0, 0.0, 50.0),
                new Node(2, 0.0, 180.0, 100.0),
                new Node(3, 90.0, -180.0, 50.0)
        );
        // 0-1-2 et 0-3-2 : ça fait une forme de diamant (ou losange lol)
        List<Edge> edges = List.of(
                new Edge(0, 1, 100),
                new Edge(1, 2, 100),
                new Edge(0, 3, 100),
                new Edge(3, 2, 100)
        );

        long seed = System.nanoTime();
        try (BaseGraph graph = new BaseGraph.Builder(encodingManager).create()) {
            setNodes(graph, nodes);
            setEdges(graph, edges, speedEnc);

            Weighting weighting = new SpeedWeighting(speedEnc);

            // Deux algos différents, mais sur le même graphe
            int source = 0, target = 2;

            Path refPath = new DijkstraBidirectionRef(graph, weighting, TraversalMode.NODE_BASED)
                    .calcPath(source, target);

            Path path = new AStar(graph, graph.wrapWeighting(weighting), TraversalMode.NODE_BASED)
                    .calcPath(source, target);

            // Vérifications qu'on viole certaines règles
            List<String> violations = GHUtility.comparePaths(refPath, path, source, target, seed);
            // System.out.println(violations);

            assertFalse(violations.isEmpty(), "Devrait entrer dans la branche de violations strictes");
        }
    }

    @Test
    public void testGetCommonNodes() {
        Directory dir = new RAMDirectory();
        final int commonNode = 3;
        List<Edge> edges = List.of(
                new Edge(1, 2, 100),
                new Edge(1, commonNode, 100),
                new Edge(2, commonNode, 100)
        );

        try (BaseGraph graph = new BaseGraph(dir, true, true, 100, 8)) {
            graph.create(DEFAULT_SIZE);

            List<EdgeIteratorState> eiss = setEdges(graph, edges, null);

            int eid1 = eiss.get(edges.get(0).nodeA).getEdge();
            int eid2 = eiss.get(edges.get(0).nodeB).getEdge();

            int result = GHUtility.getCommonNode(graph, eid1, eid2);
            assertEquals(commonNode, result, "Les deux arêtes devraient avoir un sommet commun");
        }
    }

    @Test
    public void testGetCommonNodesShouldThrowExceptionAndAdjNode() {
        Directory dir = new RAMDirectory();
        List<Edge> edges = List.of(
                new Edge(1, 2, 100),
                new Edge(2, 1, 100)
        );

        try (BaseGraph graph = new BaseGraph(dir, true, true, 100, 8)) {
            graph.create(DEFAULT_SIZE);

            List<EdgeIteratorState> eiss = setEdges(graph, edges, null);

            int eid1 = eiss.get(edges.get(0).nodeA - 1).getEdge();
            int eid2 = eiss.get(edges.get(0).nodeB - 1).getEdge();

            assertThrows(IllegalArgumentException.class, () -> GHUtility.getCommonNode(graph, eid1, eid2));

            // Pour getAdjNode()
            assertEquals(1, GHUtility.getAdjNode(graph, eid1, 1));
            assertEquals(2, GHUtility.getAdjNode(graph, eid1, 2));
        }
    }
}
