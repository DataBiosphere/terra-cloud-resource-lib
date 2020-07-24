package bio.terra.cloudres.testing;

import static org.mockito.Mockito.*;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.janitor.ApiClient;
import java.time.Duration;
import java.util.UUID;

/** Utilities for integration tests. */
public class IntegrationUtils {
  private IntegrationUtils() {}

  public static final String DEFAULT_CLIENT_NAME = "crl-integration-test";

  // TODO(CA-874): Consider setting per-integration run environment variable for the cleanup id.
  public static final CleanupConfig DEFAULT_CLEANUP_CONFIG =
      CleanupConfig.builder()
          .setTimeToLive(Duration.ofHours(2))
          .setCleanupId("crl-integration")
          // TODO(PF-14): Read value from config.
          .setAccessToken("access-token")
          .setJanitorBasePath("http://1.1.1.1")
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

  /** Use a fake ApiClient to avoid the call. TODO(PF-19): Switch to real Janitor. */
  public static void setUpSpyJanitorApi() throws Exception {
    ApiClient spyApiClient = spy(new ApiClient());
    doReturn(null)
        .when(spyApiClient)
        .invokeAPI(
            anyString(), anyString(), any(), any(), any(), any(), any(), any(), any(), any());
    CleanupRecorder.providerApiClient(spyApiClient);
  }
}
