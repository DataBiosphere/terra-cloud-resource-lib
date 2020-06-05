package bio.terra.cloudres.google.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class SerializeUtilsTest {
  @Test
  public void blobId() {
    JsonObject jsonObject = SerializeUtils.convert(BlobId.of("bucket-name", "blob-name"));
    assertEquals("{\"bucket\":\"bucket-name\",\"name\":\"blob-name\"}", jsonObject.toString());
  }

  @Test
  public void blobInfo() {
    JsonObject jsonObject =
        SerializeUtils.convert(BlobInfo.newBuilder("bucket-name", "blob-name").build());
    assertEquals(
        "{\"blobId\":{\"bucket\":\"bucket-name\",\"name\":\"blob-name\"},\"isDirectory\":false}",
        jsonObject.toString());
  }

  @Test
  public void bucketInfo() {
    JsonObject jsonObject =
        SerializeUtils.convert(
            BucketInfo.newBuilder("my-name").setLabels(ImmutableMap.of("k1", "v1")).build());
    assertEquals("{\"name\":\"my-name\",\"labels\":{\"k1\":\"v1\"}}", jsonObject.toString());
  }
}
