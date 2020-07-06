package bio.terra.cloudres.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Tests to verify properties of the CloudResourceUid generated models. */
@Tag("unit")
public class CloudResourceUidTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Asserts that serializing->deserializing a {@link CloudResourceUid} yields an "equals" instance.
   */
  public void assertSerializationIdempotency(
      CloudResourceUid resource, java.lang.Class<?> resourceClass) throws JsonProcessingException {
    String serialized = objectMapper.writeValueAsString(resource);
    CloudResourceUid deserialized = objectMapper.readValue(serialized, resource.getClass());
    // Asserts that serializing->deserializing a {@link CloudResourceUid} yields an "equals"
    // instance.
    assertEquals(resource, deserialized);
    // Assert that the deserialized instance is an instance of the same class.
    assertThat(resource, Matchers.instanceOf(resourceClass));
  }

  @Test
  public void googleBigQueryDataset() throws Exception {
    GoogleBigQueryDatasetUid dataset =
        new GoogleBigQueryDatasetUid().projectId("my-project").datasetId("my-dataset");
    assertSerializationIdempotency(dataset, GoogleBigQueryDatasetUid.class);
  }

  @Test
  public void googleBigQueryTable() throws Exception {
    GoogleBigQueryTableUid table =
        new GoogleBigQueryTableUid()
            .projectId("my-project")
            .datasetId("my-dataset")
            .tableName("my-table");
    assertSerializationIdempotency(table, GoogleBigQueryTableUid.class);
  }

  @Test
  public void googleBlob() throws Exception {
    GoogleBlobUid blob = new GoogleBlobUid().bucketName("my-bucket").blobName("my-blob");
    assertSerializationIdempotency(blob, GoogleBlobUid.class);
  }

  @Test
  public void googleBucket() throws Exception {
    GoogleBucketUid bucket = new GoogleBucketUid().bucketName("my-bucket");
    assertSerializationIdempotency(bucket, GoogleBucketUid.class);
  }

  @Test
  public void googleProject() throws Exception {
    GoogleProjectUid project = new GoogleProjectUid().projectId("my-project");
    assertSerializationIdempotency(project, GoogleProjectUid.class);
  }
}
