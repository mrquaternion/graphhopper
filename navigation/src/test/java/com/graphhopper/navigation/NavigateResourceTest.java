package com.graphhopper.navigation;

import com.graphhopper.*;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PMap;
import com.graphhopper.util.PointList;
import com.graphhopper.util.TranslationMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/* Sources :
    - https://dev.to/whathebea/how-to-use-junit-and-mockito-for-unit-testing-in-java-4pjb
    - https://www.baeldung.com/mockito-junit-5-extension
 */
@ExtendWith(MockitoExtension.class)
public class NavigateResourceTest {

    @Mock private GraphHopper graphHopper;
    @Mock private TranslationMap translationMap;
    @Mock private EncodingManager encodingManager;
    private NavigateResource resource;

    @Nested
    public class NestedNavigateResource {

        @BeforeEach
        void setup() {
            GraphHopperConfig ghConfig = new GraphHopperConfig();
            resource = new NavigateResource(graphHopper, translationMap, ghConfig);

            when(graphHopper.getEncodingManager()).thenReturn(encodingManager);
            when(encodingManager.hasEncodedValue(MaxSpeed.KEY)).thenReturn(false);
            when(graphHopper.getNavigationMode(anyString())).thenReturn(TransportationMode.CAR);
        }

        @Test
        void doGetReturnsTest() {
            GHRequest request = new GHRequest();
            request.getHints().putObject("type", "mapbox");

            ResponsePath path = new ResponsePath();
            path.setDistance(1000.0);
            path.setTime(3600);

            PointList points = new PointList();
            points.add(45.5, -73.5);
            points.add(45.51, -73.51);
            path.setWaypoints(points);

            InstructionList instructions = new InstructionList(new TranslationMap().getWithFallBack(Locale.ENGLISH));
            path.setInstructions(instructions);

            GHResponse fakeResponse = new GHResponse();
            fakeResponse.add(path);
            when(graphHopper.route(any(GHRequest.class))).thenReturn(fakeResponse);

            try (Response response = resource.doPost(request, mock(HttpServletRequest.class))) {
                assertEquals(200, response.getStatus());
                verify(graphHopper).route(any(GHRequest.class));
            }
        }
    }

    @Test
    public void voiceInstructionsTest() {

        List<Double> bearings = NavigateResource.getBearing("");
        assertEquals(0, bearings.size());
        assertEquals(Collections.EMPTY_LIST, bearings);

        bearings = NavigateResource.getBearing("100,1");
        assertEquals(1, bearings.size());
        assertEquals(100, bearings.get(0), .1);

        bearings = NavigateResource.getBearing(";100,1;;");
        assertEquals(4, bearings.size());
        assertEquals(100, bearings.get(1), .1);
    }
}
