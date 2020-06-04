package bio.terra.cloudres.google.storage;

import static org.junit.Assert.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class StorageCowTest {
  private final StorageCow storageCow = StorageIntegrationUtils.defaultStorageCow();
  /**
   * A bucket to be re-used between tests that need a bucket but do not care about the bucket
   * itself. Used to avoid creating more buckets than necessary.
   */
  private static BucketCow reusableBucket;

  @BeforeAll
  public static void createReusableBucket() {
    reusableBucket =
        StorageIntegrationUtils.defaultStorageCow()
            .create(BucketInfo.of(IntegrationUtils.randomName()));
  }

  @AfterAll
  public static void deleteReusableBucket() {
    // This only succeeds if the reusableBucket is empty. If it's not empty, something about the
    // tests has failed.
    reusableBucket.delete();
  }

  @Test
  public void createGetDeleteBucket() {
    String bucketName = IntegrationUtils.randomName();
    assertNull(storageCow.get(bucketName));

    BucketCow createdBucket = storageCow.create(BucketInfo.of(bucketName));
    assertEquals(bucketName, createdBucket.getBucketInfo().getName());

    assertEquals(bucketName, storageCow.get(bucketName).getBucketInfo().getName());

    assertTrue(storageCow.delete(bucketName));
    assertNull(storageCow.get(bucketName));
  }

  @Test
  public void createGetDeleteBlob() {
    BlobId blobId =
        BlobId.of(reusableBucket.getBucketInfo().getName(), IntegrationUtils.randomName());
    assertNull(storageCow.get(blobId));

    BlobCow createdBlob = storageCow.create(BlobInfo.newBuilder(blobId).build());
    assertEquals(blobId.getName(), createdBlob.getBlobInfo().getName());

    assertEquals(blobId.getName(), storageCow.get(blobId).getBlobInfo().getName());

    assertTrue(storageCow.delete(blobId));
    assertNull(storageCow.get(blobId));
  }

  @Test
  public void blobWriter() throws Exception {
    BlobId blobId =
        BlobId.of(reusableBucket.getBucketInfo().getName(), IntegrationUtils.randomName());
    assertNull(storageCow.get(blobId));

    String contents = "hello blob";
    try (WriteChannel writeChannel = storageCow.writer(BlobInfo.newBuilder(blobId).build())) {
      writeChannel.write(ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8)));
    }
    BlobCow blob = storageCow.get(blobId);
    assertEquals(contents, BlobCowTest.readContents(blob));
    assertTrue(storageCow.delete(blobId));
  }
}
