package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.storage.BucketInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.*;

@Tag("integration")
public class StorageCowTest {
  @Test
  public void createGetDeleteBucket() {
    StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    assertNull(storageCow.get(bucketName));

    BucketCow createdBucket = storageCow.create(BucketInfo.of(bucketName));
    assertEquals(createdBucket.getBucketInfo().getName(), bucketName);

    assertEquals(storageCow.get(bucketName).getBucketInfo().getName(), bucketName);

    assertTrue(storageCow.delete(bucketName));
    assertNull(storageCow.get(bucketName));
  }
}
