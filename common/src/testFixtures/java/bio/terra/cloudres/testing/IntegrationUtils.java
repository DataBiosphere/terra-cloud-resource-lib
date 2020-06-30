package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.*;
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
          .build();

  public static final ClientConfig DEFAULT_CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder()
          .setClient(DEFAULT_CLIENT_NAME)
          .setCleanupConfig(DEFAULT_CLEANUP_CONFIG)
          .build();

  /** Creates a new {@link InMemoryCleanupRecorder} and sets it to be used. */
  public static InMemoryCleanupRecorder provideInMemoryRecorder() {
    // TODO(CA-874): Set a real original CleanupRecorder based on configuration. The real
    // CleanupRecorder should always be used by default.
    InMemoryCleanupRecorder recorder = new InMemoryCleanupRecorder(new NullCleanupRecorder());
    CleanupRecorderLocator.provide(recorder);
    return recorder;
  }

  /** Generates a random name to use for a cloud resource. */
  public static String randomName() {
    return UUID.randomUUID().toString();
  }

  /** Generates a random name to and replace '-' with '_'. */
  public static String randomNameWithUnderscore() {
    return UUID.randomUUID().toString().replace('-', '_');
  }
}
