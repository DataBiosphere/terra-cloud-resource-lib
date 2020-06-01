package bio.terra.cloudres.google.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.testing.IntegrationCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.StorageOptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

@Tag("integration")
public class StorageCowTest {
  private static final ClientConfig DEFAULT_CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder().setClient("integration").build();

  private StorageOptions defaultStorageOptions() {
    ServiceAccountCredentials googleCredentials =
        IntegrationCredentials.getGoogleCredentialsOrDie();
    return StorageOptions.newBuilder()
        .setCredentials(googleCredentials)
        .setProjectId(googleCredentials.getProjectId())
        .build();
  }

  @Test
  public void createThenDeleteBucket() {
    StorageCow storageCow = new StorageCow(DEFAULT_CLIENT_CONFIG, defaultStorageOptions());
    String bucketName = randomName();
    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));
    assertEquals(bucketCow.getBucketInfo().getName(), bucketName);
    assertTrue(storageCow.delete(bucketName));
  }

  String randomName() {
    return UUID.randomUUID().toString();
  }
}
