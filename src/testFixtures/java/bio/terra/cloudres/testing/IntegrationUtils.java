package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupConfig;
import com.google.api.client.http.HttpRequestInitializer;
import java.time.Duration;
import java.util.UUID;

/** Utilities for integration tests. */
public class IntegrationUtils {
  private IntegrationUtils() {}

  public static final String DEFAULT_CLIENT_NAME = "crl-integration-test";

  // TODO(CA-874): Consider setting per-integration run environment variable for the cleanup id.
  // TODO(yonghao): Figure a better config pulling solution to replace the hardcoded configs.
  public static final CleanupConfig DEFAULT_CLEANUP_CONFIG =
      CleanupConfig.builder()
          .setTimeToLive(Duration.ofHours(2))
          .setCleanupId("crl-integration")
          .setCredentials(IntegrationCredentials.getJanitorClientGoogleCredentialsOrDie())
          .setJanitorTopicName("crljanitor-tools-pubsub-topic")
          .setJanitorProjectId("terra-kernel-k8s")
          .build();

  public static final ClientConfig DEFAULT_CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder()
          .setClient(DEFAULT_CLIENT_NAME)
          .setCleanupConfig(DEFAULT_CLEANUP_CONFIG)
          .build();

  /** Generates a random name to use for a cloud resource. */
  public static String randomName() {
    return UUID.randomUUID().toString();
  }

  /** Generates a random name to and replace '-' with '_'. */
  public static String randomNameWithUnderscore() {
    return UUID.randomUUID().toString().replace('-', '_');
  }

  /**
   * Sets longer timeout because some operation(e.g. Dns.ManagedZones.Create) may take longer than
   * default timeout. We pass a {@link HttpRequestInitializer} to accept a requestInitializer to
   * allow chaining, since API clients have exactly one initializer and credentials are typically
   * required as well.
   */
  public static HttpRequestInitializer setHttpTimeout(
      final HttpRequestInitializer requestInitializer) {
    return httpRequest -> {
      requestInitializer.initialize(httpRequest);
      httpRequest.setConnectTimeout(5 * 60000); // 5 minutes connect timeout
      httpRequest.setReadTimeout(5 * 60000); // 5 minutes read timeout
    };
  }
}
