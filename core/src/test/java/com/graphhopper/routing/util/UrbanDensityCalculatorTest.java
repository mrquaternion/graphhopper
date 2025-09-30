package com.graphhopper.routing.util;

import com.graphhopper.routing.ev.*;
import com.graphhopper.storage.*;
import com.graphhopper.util.EdgeIteratorState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UrbanDensityCalculatorTest {

    @Test
    public void testTrackEdgeIsRural() {
        // EncodedValues nécessaires et enregistrés dans le graph
        EnumEncodedValue<UrbanDensity> urbanDensityEnc = UrbanDensity.create();
        EnumEncodedValue<RoadClass> roadClassEnc = RoadClass.create();
        BooleanEncodedValue roadClassLinkEnc = new SimpleBooleanEncodedValue("road_class_link", false);

        EncodingManager em = (
                new EncodingManager
                        .Builder()
                        .add(urbanDensityEnc)
                        .add(roadClassEnc)
                        .add(roadClassLinkEnc)
                        .build()
        );

        try (BaseGraph graph = new BaseGraph.Builder(em).create()) {
            NodeAccess na = graph.getNodeAccess();
            na.setNode(0, 0.0, 0.0);
            na.setNode(1, 0.0, 0.001);

            EdgeIteratorState eis = graph.edge(0, 1).setDistance(50);
            eis.set(roadClassEnc, RoadClass.TRACK);
            int eid = eis.getEdge();

            UrbanDensityCalculator.calcUrbanDensity(
                    graph,
                    urbanDensityEnc,
                    roadClassEnc,
                    roadClassLinkEnc,
                    200.0,
                    1.0,
                    2.0,
                    1.0,
                    1
            );

            // Relire l'état de l'edge après calcul
            EdgeIteratorState after = graph.getEdgeIteratorState(eid, Integer.MIN_VALUE);
            assertEquals(UrbanDensity.RURAL, after.get(urbanDensityEnc));
        }
    }

}
