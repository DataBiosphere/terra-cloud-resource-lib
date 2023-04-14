package bio.terra.cloudres.aws.console;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sts.model.Credentials;

/** A Cloud Object Wrapper(COW) for methods providing access to the AWS Console. */
public class ConsoleCow {
  private static Logger logger = LoggerFactory.getLogger(ConsoleCow.class);
  @VisibleForTesting public static final String URL_SCHEME = "https";
  @VisibleForTesting public static final String URL_HOST = "signin.aws.amazon.com";
  @VisibleForTesting public static final String URL_PATH = "/federation";
  private final UrlRequester urlRequester;
  private final OperationAnnotator operationAnnotator;

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  /** Injection point for mocking HTTP calls. */
  @VisibleForTesting
  public static class UrlRequester {
    public InputStream requestUrl(URL url) throws IOException {
      return url.openConnection().getInputStream();
    }
  }

  /**
   * Constructor used to inject a mock {@link UrlRequester} instance to mock HTTP requests. Please
   * use {@link #create(ClientConfig)} factory function to instantiate this class.
   */
  @VisibleForTesting
  public ConsoleCow(UrlRequester urlRequester, ClientConfig clientConfig) {
    this.urlRequester = urlRequester;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
  }

  /**
   * Factory method to create an instance of the {@link ConsoleCow} class.
   *
   * @param clientConfig CRL client configuration instance.
   */
  public static ConsoleCow create(ClientConfig clientConfig) {
    return new ConsoleCow(new UrlRequester(), clientConfig);
  }

  /**
   * AWS currently does not provide SDK methods for generating signed console URL's, rather it is a
   * multi-step process as described <a
   * href="https://docs.aws.amazon.com/IAM/latest/UserGuide/id_roles_providers_enable-console-custom-url.html">in
   * this document</a>:
   *
   * <ul>
   *   <li>Obtain an AWS session credential (external to this method, passed in as parameter
   *       'credentials').
   *   <li>Build and submit an HTTP request using this credential to obtain a SigninToken from the
   *       AWS federation endpoint, requesting a duration between 900 and 43200 seconds (passed in
   *       as parameter `duration`).
   *   <li>Construct a URL, also pointed at the AWS federation endpoint, containing the SigninToken
   *       obtained in the previous step and the destination AWS console page to redirect to (passed
   *       in as parameter 'destination').
   * </ul>
   *
   * @param credentials AWS session {@link Credentials} to use when accessing the console
   * @param duration duration (in seconds) to request console access (900 - 43200)
   * @param destination console URL to provide access to
   * @return A signed URL providing authenticated access to the AWS Console URL specified in
   *     parameter 'destination'; this URL must be used within 15 minutes of creation (not to be
   *     mistaken with the session duration requested in parameter 'duration').
   * @throws IOException
   */
  public URL createSignedUrl(Credentials credentials, Integer duration, URL destination)
      throws IOException {
    return operationAnnotator.executeCheckedCowOperation(
        ConsoleOperation.AWS_CONSOLE_CREATE_SIGNED_URL,
        () -> buildSignedConsoleUrl(retrieveSigninToken(credentials, duration), destination),
        () -> serialize(credentials, duration, destination));
  }

  /**
   * Helper method to build a URL for the AWS federation endpoint, adding the query parameters
   * defined in the passed {@link Map} to the common base URL.
   */
  private static URL buildUrl(Map<String, String> parameterMap) {
    try {
      URIBuilder uriBuilder =
          new URIBuilder().setScheme(URL_SCHEME).setHost(URL_HOST).setPath(URL_PATH);
      parameterMap.entrySet().stream()
          .forEach((entry) -> uriBuilder.addParameter(entry.getKey(), entry.getValue()));
      return uriBuilder.build().toURL();
    } catch (MalformedURLException | URISyntaxException e) {
      throw new CrlConsoleException(
          "Unexpected exception encountered while doing parameterized URL build.", e);
    }
  }

  /**
   * Use the retrieved Sign-in Token and the passed console destination URL to build the signed URL
   * to return to the user.
   */
  private static URL buildSignedConsoleUrl(String signinToken, URL destination) {
    return buildUrl(
        Map.ofEntries(
            Map.entry("Action", "login"),
            Map.entry("Issuer", "terra.verily.com"),
            Map.entry("Destination", destination.toString()),
            Map.entry("SigninToken", signinToken)));
  }

  /**
   * Encode the required fields from the passed user credential into the format expected by the
   * federation endpoint's 'getSigninToken' action.
   */
  @VisibleForTesting
  public static String encodeCredential(Credentials credentials) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("sessionId", credentials.accessKeyId());
    jsonObject.addProperty("sessionKey", credentials.secretAccessKey());
    jsonObject.addProperty("sessionToken", credentials.sessionToken());
    return jsonObject.toString();
  }

  /**
   * Helper to call out to the AWS federation endpoint and request a token with the passed
   * credentials and duration. This returns the retrieved sign-in token as a string.
   */
  private String retrieveSigninToken(Credentials credentials, Integer duration) throws IOException {
    URL url =
        buildUrl(
            Map.ofEntries(
                Map.entry("Action", "getSigninToken"),
                Map.entry("DurationSeconds", duration.toString()),
                Map.entry("SessionType", "json"),
                Map.entry("Session", encodeCredential(credentials))));

    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(urlRequester.requestUrl(url)))) {
      return JsonParser.parseReader(bufferedReader)
          .getAsJsonObject()
          .get("SigninToken")
          .getAsString();
    }
  }

  @VisibleForTesting
  public JsonObject serialize(Credentials credentials, Integer duration, URL destination) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("accessKeyId", credentials.accessKeyId());
    jsonObject.addProperty(
        "credentialExpiration",
        Optional.ofNullable(credentials.expiration()).map(Instant::toString).orElse("none"));
    jsonObject.addProperty("duration", duration);
    jsonObject.addProperty("destination", destination.toString());
    return jsonObject;
  }
}
