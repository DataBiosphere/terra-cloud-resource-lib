package bio.terra.cloudres.aws.console;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.services.sts.model.Credentials;

@Tag("unit")
public class ConsoleCowTest {

  private ConsoleCow consoleCow;

  @Mock private ConsoleCow.UrlRequester mockUrlRequester = mock(ConsoleCow.UrlRequester.class);
  @Mock private Logger mockLogger = mock(Logger.class);

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("ConsoleCowTest").build();
    ConsoleCow.setLogger(mockLogger);
    consoleCow = new ConsoleCow(mockUrlRequester, unitTestConfig);
  }

  /**
   * Validate that the base URL components match the expected constants in {@link ConsoleCow}, and
   * that the query string contains all the query parameters specified in the passed map.
   */
  private void validateUrl(URL url, Map<String, Object> queryMap) throws URISyntaxException {
    Assertions.assertEquals(ConsoleCow.URL_SCHEME, url.getProtocol());
    Assertions.assertEquals(ConsoleCow.URL_HOST, url.getHost());
    Assertions.assertEquals(ConsoleCow.URL_PATH, url.toURI().getPath());

    int count = 0;
    for (String queryParameter : url.getQuery().split("&")) {
      String[] keyValue = queryParameter.split("=");
      String key = keyValue[0];
      String value = keyValue[1];

      Assertions.assertTrue(queryMap.containsKey(key));
      Assertions.assertEquals(queryMap.get(key), value);
      ++count;
    }
    Assertions.assertEquals(queryMap.size(), count);
  }

  @Test
  public void createSignedUrlTest() throws IOException, URISyntaxException {

    // Fake response stream for mock HTTP request
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("SigninToken", "signintoken");
    InputStream anyInputStream = new ByteArrayInputStream(jsonObject.toString().getBytes());
    when(mockUrlRequester.requestUrl(any())).thenReturn(anyInputStream);

    // Build a fake inputs that we can verify captured arguments against.

    Credentials credentials =
        Credentials.builder()
            .accessKeyId("accesskeyid")
            .secretAccessKey("secretaccesskey")
            .sessionToken("sessiontoken")
            .expiration(Instant.EPOCH)
            .build();

    Integer duration = 3600;
    URL destination = new URL("https://console.aws.com");

    // Call the createSignedUrl method
    URL url = consoleCow.createSignedUrl(credentials, duration, destination);

    // Validate that the returned URL is as expected
    validateUrl(
        url,
        Map.ofEntries(
            Map.entry("Action", "login"),
            Map.entry("Issuer", "terra.verily.com"),
            Map.entry(
                "Destination", URLEncoder.encode(destination.toString(), StandardCharsets.UTF_8)),
            Map.entry("SigninToken", "signintoken")));

    // Validate that the SigninToken request URL passed to the mock was as expected.
    ArgumentCaptor<URL> urlArgumentCaptor = ArgumentCaptor.forClass(URL.class);
    verify(mockUrlRequester).requestUrl(urlArgumentCaptor.capture());
    validateUrl(
        urlArgumentCaptor.getValue(),
        Map.ofEntries(
            Map.entry("Action", "getSigninToken"),
            Map.entry("DurationSeconds", duration.toString()),
            Map.entry("SessionType", "json"),
            Map.entry(
                "Session",
                URLEncoder.encode(
                    ConsoleCow.encodeCredential(credentials), StandardCharsets.UTF_8))));

    // Validate that the OperationAnnotator captured the execution.

    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = consoleCow.serialize(credentials, duration, destination);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        json.get("operation").getAsString(),
        ConsoleOperation.AWS_CONSOLE_CREATE_SIGNED_URL.toString());
  }
}
