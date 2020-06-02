package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import java.util.UUID;

/** Utilities for integration tests of the storage package. */
class StorageIntegrationUtils {
  private StorageIntegrationUtils() {}

  static StorageCow defaultStorageCow() {
    return new StorageCow(IntegrationUtils.DEFAULT_CLIENT_CONFIG, defaultStorageOptions());
  }

  static StorageOptions defaultStorageOptions() {
    ServiceAccountCredentials googleCredentials =
        IntegrationCredentials.getGoogleCredentialsOrDie();
    return StorageOptions.newBuilder()
        .setCredentials(googleCredentials)
        .setProjectId(googleCredentials.getProjectId())
        .build();
  }
}
