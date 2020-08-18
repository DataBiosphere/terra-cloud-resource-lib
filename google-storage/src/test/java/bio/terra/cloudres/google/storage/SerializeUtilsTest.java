package bio.terra.cloudres.google.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.storage.*;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class SerializeUtilsTest {
  @Test
  public void acl() {
    JsonObject jsonObject =
        SerializeUtils.convert(
            Acl.newBuilder(new Acl.User("test@gmail.com"), Acl.Role.READER).build());
    assertEquals(
        "{\"entity\":{\"type\":\"USER\",\"value\":\"test@gmail.com\"},"
            + "\"role\":{\"constant\":\"READER\"}}",
        jsonObject.toString());
  }

  @Test
  public void aclEntity() {
    JsonObject jsonObject = SerializeUtils.convert(new Acl.Group("test@googlegroups.com"));
    assertEquals("{\"type\":\"GROUP\",\"value\":\"test@googlegroups.com\"}", jsonObject.toString());
  }

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

  @Test
  public void bucketNameAndBlobListOption() {
    JsonObject jsonObject = SerializeUtils.convert("my-name", Storage.BlobListOption.pageSize(1));
    assertEquals(
        "{\"bucketName\":\"my-name\",\"blobListOption\":[{\"rpcOption\":\"MAX_RESULTS\",\"value\":1}]}",
        jsonObject.toString());
  }

  @Test
  public void bucketNameAndBucketTargetOption() {
    JsonObject jsonObject =
        SerializeUtils.convert(
            BucketInfo.newBuilder("my-name").build(),
            Storage.BucketTargetOption.metagenerationMatch());
    assertEquals(
        "{\"bucketInfo\":{\"name\":\"my-name\"},\"bucketTargetOption\":[{\"rpcOption\":\"IF_METAGENERATION_MATCH\"}]}",
        jsonObject.toString());
  }
}
