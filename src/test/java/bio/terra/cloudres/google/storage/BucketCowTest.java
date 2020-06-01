package bio.terra.cloudres.google.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.storage.BucketInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class BucketCowTest {
  @Test
  public void deleteBucket() {
    StorageCow storageCow = IntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));
    assertEquals(bucketCow.getBucketInfo().getName(), bucketName);
    assertTrue(bucketCow.delete());
  }
}
