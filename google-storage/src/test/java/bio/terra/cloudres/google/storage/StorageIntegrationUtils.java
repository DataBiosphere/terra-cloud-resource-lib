package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.Channels;

/** Utilities for integration tests of the storage package. */
class StorageIntegrationUtils {
  private StorageIntegrationUtils() {}

  static StorageCow defaultStorageCow() {
    return new StorageCow(IntegrationUtils.createDefaultClientConfig(), defaultStorageOptions());
  }

  static StorageOptions defaultStorageOptions() {
    ServiceAccountCredentials googleCredentials =
        IntegrationCredentials.getAdminGoogleCredentialsOrDie();
    return StorageOptions.newBuilder()
        .setCredentials(googleCredentials)
        .setProjectId(googleCredentials.getProjectId())
        .build();
  }

  static String readContents(BlobCow blob) throws IOException {
    try (InputStreamReader reader = new InputStreamReader(Channels.newInputStream(blob.reader()))) {
      return CharStreams.toString(reader);
    }
  }
}
