package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.testing.IntegrationCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import java.util.UUID;

/** Utilities for integration tests of the storage package. */
class IntegrationUtils {
  private IntegrationUtils() {}

  static final ClientConfig DEFAULT_CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder().setClient("integration").build();

  static StorageCow defaultStorageCow() {
    return new StorageCow(DEFAULT_CLIENT_CONFIG, defaultStorageOptions());
  }

  static StorageOptions defaultStorageOptions() {
    ServiceAccountCredentials googleCredentials =
        IntegrationCredentials.getGoogleCredentialsOrDie();
    return StorageOptions.newBuilder()
        .setCredentials(googleCredentials)
        .setProjectId(googleCredentials.getProjectId())
        .build();
  }

  /** Generates a random name to use for a cloud resource. */
  static String randomName() {
    return UUID.randomUUID().toString();
  }
}
