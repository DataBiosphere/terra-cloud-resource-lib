package bio.terra.cloudres.testing;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.CreateResourceRequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MockJanitorService {

  public static final Integer PORT = 8089;
  public static final String SERVICE_BASE_PATH = "http://127.0.0.1:" + String.valueOf(PORT);
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
      return IntegrationCredentials.getUserGoogleCredentialsOrDie()
          .createScoped(SCOPES)
          .refreshAccessToken()
          .getTokenValue();
    } catch (IOException e) {
      throw new RuntimeException("Failed to obtain access token", e);
    }
  }

  public List<CloudResourceUid> getRecordedResources() throws Exception {
    return wireMockServer.findRequestsMatching(RequestPattern.everything()).getRequests().stream()
        .map(
            request -> {
              try {
                return new ObjectMapper()
                    .readValue(request.getBodyAsString(), CreateResourceRequestBody.class)
                    .getResourceUid();
              } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse request to CreateResourceRequestBody");
              }
            })
        .collect(Collectors.toList());
  }
}
