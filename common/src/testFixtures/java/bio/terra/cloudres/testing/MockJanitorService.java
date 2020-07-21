package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.ClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import bio.terra.janitor.model.CloudResourceUid;
import com.github.tomakehurst.wiremock.matching.MultipartValuePattern;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class MockJanitorService {
    public static final String SERVICE_BASE_PATH = "http://127.0.0.1:8089";
    private static final List<String> SCOPES = Arrays.asList("openid", "email", "profile");

    private final WireMockServer wireMockServer;

    public MockJanitorService() {
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(8089));
    }

    public void setup() {
        wireMockServer.start();
        wireMockServer.stubFor(
                post(urlEqualTo("/api/janitor/v1/resource"))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "text/json")
                                        .withBody("{\"id\": \"123456\"}")));
    }

    public void stop() {
        wireMockServer.stop();
    }

    public static String getDefaultAccessToken() {
        try {
            System.out.println("~~~~~~~~!!!!");
            System.out.println(IntegrationCredentials
                    .getUserGoogleCredentialsOrDie().createScoped(SCOPES).refreshAccessToken().getTokenValue());
            return IntegrationCredentials
                    .getUserGoogleCredentialsOrDie().createScoped(SCOPES).refreshAccessToken().getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException("Failed", e);
        }
    }

    public void assertCreateRequestMatch(CloudResourceUid resource, ClientConfig clientConfig) throws Exception {
        verify(postRequestedFor(urlEqualTo("/api/janitor/v1/resource"))
                .withRequestBody(matching(new ObjectMapper().writeValueAsString(resource))));
    }

    private static CreateResourceRequestBody createResourceRequestBody(ClientConfig clientConfig) {

    }
}
