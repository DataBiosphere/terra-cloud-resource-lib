package bio.terra.cloudres.google.storage;

import static org.hamcrest.MatcherAssert.assertThat;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.StorageOptions;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;

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

  /** Helper to compare two list of {@link Acl}'s entity and role, but ignores the etag and id. */
  static void assertAclsMatch(List<Acl> expect, List<Acl> actual) {
    List<Acl> cleanActualAcl = new ArrayList<>();
    List<Acl> cleanExpectAcl = new ArrayList<>();
    actual.forEach(
        acl -> cleanActualAcl.add(Acl.newBuilder(acl.getEntity(), acl.getRole()).build()));
    expect.forEach(
        acl -> cleanExpectAcl.add(Acl.newBuilder(acl.getEntity(), acl.getRole()).build()));

    assertThat(cleanActualAcl, Matchers.containsInAnyOrder(cleanExpectAcl.toArray()));
  }
}
