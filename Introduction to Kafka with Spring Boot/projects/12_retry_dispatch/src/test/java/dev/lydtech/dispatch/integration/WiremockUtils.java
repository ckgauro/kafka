package dev.lydtech.dispatch.integration;

import com.github.tomakehurst.wiremock.client.WireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;


public final class WiremockUtils {

    private WiremockUtils() {
    }

    public static void reset() {
        WireMock.reset();
        WireMock.resetAllRequests();
        WireMock.resetAllScenarios();
        WireMock.resetToDefault();
    }

    public static void stubWiremock(String url, int status, String body) {
        stubWiremock(url, status, body, null, null, null);
    }

    public static void stubWiremock(
            String url,
            int status,
            String body,
            String scenario,
            String initialState,
            String nextState
    ) {
        if (scenario != null) {
            stubFor(get(urlEqualTo(url))
                    .inScenario(scenario)
                    .whenScenarioStateIs(initialState)
                    .willReturn(
                            aResponse()
                                    .withStatus(status)
                                    .withHeader("Content-Type", "text/plain")
                                    .withBody(body)
                    )
                    .willSetStateTo(nextState));
            return;
        }

        stubFor(get(urlEqualTo(url))
                .willReturn(
                        aResponse()
                                .withStatus(status)
                                .withHeader("Content-Type", "text/plain")
                                .withBody(body)
                ));
    }
}