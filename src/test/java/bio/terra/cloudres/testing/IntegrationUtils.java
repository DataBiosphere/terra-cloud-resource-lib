package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.ClientConfig;

import java.util.UUID;

/** Utilities for integration tests. */
public class IntegrationUtils {
  private IntegrationUtils() {}

  public static final String DEFAULT_CLIENT_NAME = "crl-integration-test";

  public static final ClientConfig DEFAULT_CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder().setClient(DEFAULT_CLIENT_NAME).build();

  /** Generates a random name to use for a cloud resource. */
  public static String randomName() {
    return UUID.randomUUID().toString();
  }
}
