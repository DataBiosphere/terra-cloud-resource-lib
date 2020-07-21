package bio.terra.cloudres.common.cleanup;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class MockJanitorService {
    private final WireMockServer wireMockServer;

    public MockJanitorService() {
        wireMockServer = new WireMockServer(8089);
    }

    public void setup() {
        wireMockServer.start();
    stubFor(
        post(urlEqualTo("/api/janitor/v1/resource"))
            .willReturn(aResponse().withBody("{id: 123456}")));
    }

    public void stop() {
        wireMockServer.stop();
    }
}
