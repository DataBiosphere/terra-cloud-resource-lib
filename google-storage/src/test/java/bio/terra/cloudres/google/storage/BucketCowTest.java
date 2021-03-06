package bio.terra.cloudres.google.storage;

import static bio.terra.cloudres.google.storage.StorageIntegrationUtils.assertAclsMatch;
import static bio.terra.cloudres.google.storage.StorageIntegrationUtils.createBlobWithContents;
import static bio.terra.cloudres.google.storage.StorageIntegrationUtils.getTestUserEmailAddress;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BucketInfo;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class BucketCowTest {
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
    // getLifecycleRules changed behavior and returns an empty list instead of null
    assertThat(storageCow.get(bucketName).getBucketInfo().getLifecycleRules().size(), equalTo(0));

    BucketInfo.LifecycleRule lifecycleRule =
        new BucketInfo.LifecycleRule(
            BucketInfo.LifecycleRule.LifecycleAction.newDeleteAction(),
            BucketInfo.LifecycleRule.LifecycleCondition.newBuilder().setAge(1).build());

    BucketCow updatedBucketCow =
        bucketCow.toBuilder().setLifecycleRules(ImmutableList.of(lifecycleRule)).build().update();

    assertEquals(1, updatedBucketCow.getBucketInfo().getLifecycleRules().size());
    assertEquals(lifecycleRule, updatedBucketCow.getBucketInfo().getLifecycleRules().get(0));

    assertTrue(updatedBucketCow.delete());
    assertNull(storageCow.get(bucketName));
  }

  @Test
  public void updateBucketAcl() throws Exception {
    StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
    String bucketName = IntegrationUtils.randomName();
    BucketCow bucketCow = storageCow.create(BucketInfo.of(bucketName));

    List<Acl> defaultAcl = storageCow.get(bucketName).getBucketInfo().getAcl();
    Acl acl = Acl.newBuilder(new Acl.User(getTestUserEmailAddress()), Acl.Role.READER).build();
    bucketCow.updateAcl(acl);

    // Verify that new ACL is added and previous Acls still exist.
    List<Acl> expectedAcl = new ArrayList<>(defaultAcl);
    expectedAcl.add(acl);
    assertAclsMatch(expectedAcl, storageCow.get(bucketName).getBucketInfo().getAcl());

    assertTrue(bucketCow.delete());
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
}
