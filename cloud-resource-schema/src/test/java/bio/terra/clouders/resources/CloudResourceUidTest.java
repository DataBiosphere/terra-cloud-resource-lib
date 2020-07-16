package bio.terra.clouders.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import bio.terra.cloudres.resources.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Tests to verify properties of the CloudResourceUid generated models. */
@Tag("unit")
public class CloudResourceUidTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Asserts that serializing->deserializing a {@link CloudResourceUid} yields an "equals" instance.
   */
  public void assertSerializationIdempotency(CloudResourceUid resource, String getMethodName)
      throws Exception {
    String serialized = objectMapper.writeValueAsString(resource);
    CloudResourceUid deserialized = objectMapper.readValue(serialized, resource.getClass());
    // Asserts that serializing->deserializing a {@link CloudResourceUid} yields an "equals"
    // instance.
    assertEquals(resource, deserialized);
    assertNotNull(CloudResourceUid.class.getMethod(getMethodName));
  }

  @Test
  public void googleBigQueryDataset() throws Exception {
    CloudResourceUid dataset =
        new CloudResourceUid()
            .googleBigQueryDatasetUid(
                new GoogleBigQueryDatasetUid().projectId("my-project").datasetId("my-dataset"));

    assertSerializationIdempotency(dataset, "getGoogleBigQueryDatasetUid");
  }

  @Test
  public void googleBigQueryTable() throws Exception {
    CloudResourceUid table =
        new CloudResourceUid()
            .googleBigQueryTableUid(
                new GoogleBigQueryTableUid()
                    .projectId("my-project")
                    .datasetId("my-dataset")
                    .tableId("my-table"));
    assertSerializationIdempotency(table, "getGoogleBigQueryTableUid");
  }

  @Test
  public void googleBlob() throws Exception {
    CloudResourceUid blob =
        new CloudResourceUid()
            .googleBlobUid(new GoogleBlobUid().bucketName("my-bucket").blobName("my-blob"));
    assertSerializationIdempotency(blob, "getGoogleBlobUid");
  }

  @Test
  public void googleBucket() throws Exception {
    CloudResourceUid bucket =
        new CloudResourceUid().googleBucketUid(new GoogleBucketUid().bucketName("my-bucket"));
    assertSerializationIdempotency(bucket, "getGoogleBucketUid");
  }

  @Test
  public void googleProject() throws Exception {
    CloudResourceUid project =
        new CloudResourceUid().googleProjectUid(new GoogleProjectUid().projectId("my-project"));
    assertSerializationIdempotency(project, "getGoogleProjectUid");
  }
}
