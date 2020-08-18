package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BucketInfo;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static bio.terra.cloudres.google.storage.StorageIntegrationUtils.createBlobWithContents;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
public class BucketCowTest {
  @Test
  public void deleteCreatedBucket() throws Exception {
    StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    assertNull(storageCow.get(bucketName));

    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));
    assertEquals(storageCow.get(bucketName).getBucketInfo().getName(), bucketName);

    assertTrue(bucketCow.delete());
    assertNull(storageCow.get(bucketName));
  }

  @Test
  public void updateBucket() throws Exception {
    StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));
    assertNull(storageCow.get(bucketName).getBucketInfo().getLifecycleRules());

    BucketInfo.LifecycleRule lifecycleRule =
        new BucketInfo.LifecycleRule(
            BucketInfo.LifecycleRule.LifecycleAction.newDeleteAction(),
            BucketInfo.LifecycleRule.LifecycleCondition.newBuilder().setAge(1).build());

    BucketCow updatedBucketCow = bucketCow.toBuilder().setLifecycleRules(ImmutableList.of(lifecycleRule)).build().update();

    assertEquals(1, storageCow.get(updatedBucketCow.getBucketInfo().getName()).getBucketInfo().getLifecycleRules().size());
    assertEquals(
        lifecycleRule, updatedBucketCow.getBucketInfo().getLifecycleRules().get(0));

    assertTrue(updatedBucketCow.delete());
    assertNull(storageCow.get(bucketName));
  }

  @Test
  public void listBlobs() throws Exception {
    StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));

    BlobId blobId1 = BlobId.of(bucketCow.getBucketInfo().getName(), IntegrationUtils.randomName());
    BlobId blobId2 = BlobId.of(bucketCow.getBucketInfo().getName(), IntegrationUtils.randomName());

    final String contents1 = "hello my blob1";
    final String contents2 = "hello my blob2";

    BlobCow blobCow1 = createBlobWithContents(storageCow, blobId1, contents1);
    BlobCow blobCow2 = createBlobWithContents(storageCow, blobId2, contents2);

    Map<String, String> actualBlobIdContentMap = extractBlobNameWithContent(bucketCow.list());

    assertThat(actualBlobIdContentMap, hasEntry(blobId1.getName(), contents1));
    assertThat(actualBlobIdContentMap, hasEntry(blobId2.getName(), contents2));

    assertTrue(blobCow1.delete());
    assertTrue(blobCow2.delete());
    assertTrue(bucketCow.delete());
    assertNull(storageCow.get(bucketName));
  }

  private static Map<String, String> extractBlobNameWithContent(Page<BlobCow> actual) {
    Map<String, String> actualBlobIdContentMap = new HashMap<>();
    actual
        .iterateAll()
        .forEach(
            blobCow -> {
              try {
                actualBlobIdContentMap.put(
                    blobCow.getBlobInfo().getBlobId().getName(),
                    StorageIntegrationUtils.readContents(blobCow));
              } catch (IOException e) {
                throw new RuntimeException(
                    "Fail to read contents from Blob" + blobCow.getBlobInfo().getBlobId(), e);
              }
            });

    return actualBlobIdContentMap;
  }
}
