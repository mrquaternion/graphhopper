package com.graphhopper.navigation;

import com.graphhopper.*;
import com.graphhopper.routing.ev.MaxSpeed;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.TransportationMode;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.TranslationMap;
import com.graphhopper.util.shapes.GHPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/* Sources :
    - https://dev.to/whathebea/how-to-use-junit-and-mockito-for-unit-testing-in-java-4pjb
    - https://www.baeldung.com/mockito-junit-5-extension
    - https://www.baeldung.com/mockito-argumentcaptor
 */
@ExtendWith(MockitoExtension.class)
public class NavigateResourceTest {

    @Mock private GraphHopper graphHopper; // commun
    @Mock private TranslationMap translationMap; // commun
    @Mock private EncodingManager encodingManager; // doPost
    @Mock private HttpServletRequest httpRequest; // doGet
    private NavigateResource resource;

    @Nested
    public class NestedNavigateResource {

        @BeforeEach
        void setup() {
            GraphHopperConfig ghConfig = new GraphHopperConfig();
            resource = new NavigateResource(graphHopper, translationMap, ghConfig);

            // Stubs communs
            when(graphHopper.route(any(GHRequest.class))).thenReturn(buildFakeResponse());
            when(graphHopper.getNavigationMode(anyString())).thenReturn(TransportationMode.CAR);
        }

        @Test
        void doGetTest() {
            double LON = -73.5;
            double LAT = 45.5;
            double OFFSET = 0.01;
            String formattedPath = String.format(
                    "/navigate/directions/v5/gh/driving/%f,%f;%f,%f",
                    LON, LAT, LON + OFFSET, LAT - OFFSET
            );
            when(httpRequest.getRequestURI()).thenReturn(formattedPath);

            try (Response response = resource.doGet(
                    httpRequest, mock(UriInfo.class), mock(ContainerRequestContext.class),
                    true, true, true, true,
                    "", "", "polyline6", "", "", "driving"
            )) {
                assertEquals(200, response.getStatus());

                ArgumentCaptor<GHRequest> captor = ArgumentCaptor.forClass(GHRequest.class);
                verify(graphHopper).route(captor.capture());

                GHRequest capturedRequest = captor.getValue();
                List<GHPoint> points = capturedRequest.getPoints();
                // System.out.println(points);
                assertEquals(LAT, points.get(0).getLat(), "La valeur latitudinale doit être pareil.");
                assertEquals(LON, points.get(0).getLon(), "La valeur longitudinale doit être pareil.");
            }
        }

        @Test
        void doPostTest() {
            when(graphHopper.getEncodingManager()).thenReturn(encodingManager);
            when(encodingManager.hasEncodedValue(MaxSpeed.KEY)).thenReturn(false);

            GHRequest request = new GHRequest();
            request.getHints().putObject("type", "mapbox");

            try (Response response = resource.doPost(request, httpRequest)) {
                assertEquals(200, response.getStatus());

                Object entity = response.getEntity();
                // System.out.println(entity);
                assertNotNull(entity, "Le corps de la réponse ne devrait pas être null.");
            }
        }

        private GHResponse buildFakeResponse() {
            GHResponse response = new GHResponse();
            ResponsePath path = new ResponsePath();
            InstructionList instructions = new InstructionList(new TranslationMap().getWithFallBack(Locale.ENGLISH));
            path.setInstructions(instructions);
            response.add(path);
            return response;
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
