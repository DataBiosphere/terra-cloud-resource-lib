package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

/** Utilities for integration tests of the storage package. */
class StorageIntegrationUtils {
  private StorageIntegrationUtils() {}

  static StorageCow defaultStorageCow() {
    return new StorageCow(IntegrationUtils.DEFAULT_CLIENT_CONFIG, defaultStorageOptions());
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

  static BlobCow createBlobWithContents(StorageCow storageCow, BlobId blobId, String contents)
      throws IOException {
    try (WriteChannel writeChannel = storageCow.writer(BlobInfo.newBuilder(blobId).build())) {
      writeChannel.write(ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8)));
    }
    return storageCow.get(blobId);
  }
}
