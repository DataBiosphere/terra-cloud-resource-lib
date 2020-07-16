package bio.terra.clouders.resources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import bio.terra.cloudres.resources.*;
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
  public void assertSerializationIdempotency(CloudResourceUid resource, Class<?> resourceClass)
          throws JsonProcessingException {
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
    CloudResourceUid dataset =
            new CloudResourceUid()
                    .googleBigQueryDatasetUid(
                            new GoogleBigQueryDatasetUid().projectId("my-project").datasetId("my-dataset"));

    assertSerializationIdempotency(dataset, CloudResourceUid.class);
  }
}
