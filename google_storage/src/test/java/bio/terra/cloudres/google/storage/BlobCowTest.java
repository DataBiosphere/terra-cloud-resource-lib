package bio.terra.cloudres.google.storage;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.CopyWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class BlobCowTest {

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
  public void deleteCreatedBlob() {
    BlobId blobId =
        BlobId.of(reusableBucket.getBucketInfo().getName(), IntegrationUtils.randomName());
    assertNull(storageCow.get(blobId));

    BlobCow blob = storageCow.create(BlobInfo.newBuilder(blobId).build());
    assertEquals(blobId.getName(), storageCow.get(blobId).getBlobInfo().getName());

    assertTrue(blob.delete());
    assertNull(storageCow.get(blobId));
  }

  @Test
  public void copyTo() throws Exception {
    BlobId sourceBlobId =
        BlobId.of(reusableBucket.getBucketInfo().getName(), IntegrationUtils.randomName());
    BlobId targetBlobId =
        BlobId.of(reusableBucket.getBucketInfo().getName(), IntegrationUtils.randomName());

    final String contents = "hello my blob";
    BlobCow source = createBlobWithContents(sourceBlobId, contents);
    assertEquals(contents, StorageIntegrationUtils.readContents(source));

    assertNull(storageCow.get(targetBlobId));
    CopyWriter copyWriter = source.copyTo(targetBlobId);
    copyWriter.getResult();
    BlobCow target = storageCow.get(targetBlobId);
    assertEquals(contents, StorageIntegrationUtils.readContents(target));

    assertTrue(source.delete());
    assertTrue(target.delete());
  }

  @Test
  public void reader() throws Exception {
    BlobId blobId =
        BlobId.of(reusableBucket.getBucketInfo().getName(), IntegrationUtils.randomName());

    final String contents = "hello my blob";
    BlobCow blob = createBlobWithContents(blobId, contents);

    assertEquals(contents, StorageIntegrationUtils.readContents(blob));

    assertTrue(blob.delete());
  }

  private BlobCow createBlobWithContents(BlobId blobId, String contents) throws IOException {
    try (WriteChannel writeChannel = storageCow.writer(BlobInfo.newBuilder(blobId).build())) {
      writeChannel.write(ByteBuffer.wrap(contents.getBytes(StandardCharsets.UTF_8)));
    }
    return storageCow.get(blobId);
  }
}
