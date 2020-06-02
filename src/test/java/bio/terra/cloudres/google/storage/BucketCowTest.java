package bio.terra.cloudres.google.storage;

import static org.junit.Assert.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.storage.BucketInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class BucketCowTest {
  @Test
  public void deleteCreatedBucket() {
    StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    assertNull(storageCow.get(bucketName));

    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));
    assertEquals(storageCow.get(bucketName).getBucketInfo().getName(), bucketName);

    assertTrue(bucketCow.delete());
    assertNull(storageCow.get(bucketName));
  }
}
