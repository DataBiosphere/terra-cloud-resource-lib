package bio.terra.cloudres.google.storage;

import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBlobUid;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;

/** Utils for serializing {@link com.google.cloud.storage} objects. */
class SerializeUtils {
  private SerializeUtils() {}

  static CloudResourceUid create(BlobId blobId) {
    return new CloudResourceUid()
        .googleBlobUid(
            new GoogleBlobUid().blobName(blobId.getName()).bucketName(blobId.getBucket()));
  }

  static JsonObject convert(Acl acl) {
    return convertWithGson(acl, Acl.class);
  }

  static JsonObject convert(Acl.Entity entity) {
    return convertWithGson(entity, Acl.Entity.class);
  }

  static JsonObject convert(BlobId blobId) {
    return convertWithGson(blobId, BlobId.class);
  }

  static JsonObject convert(BlobInfo blobInfo) {
    return convertWithGson(blobInfo, BlobInfo.class);
  }

  static JsonObject convert(BucketInfo bucketInfo) {
    return convertWithGson(bucketInfo, BucketInfo.class);
  }

  /**
   * Helper for Gson convertible classes. Should only be used with classes that play nicely with
   * Gson.
   */
  private static <R> JsonObject convertWithGson(R r, Type t) {
    return new Gson().toJsonTree(r, t).getAsJsonObject();
  }
}
