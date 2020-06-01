package bio.terra.cloudres.google.storage;

import static org.junit.Assert.assertTrue;

import com.google.cloud.storage.BucketInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class BucketCowTest {
  @Test
  public void deleteCreatedBucket() {
    StorageCow storageCow = IntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));

    assertTrue(bucketCow.delete());
  }
}
