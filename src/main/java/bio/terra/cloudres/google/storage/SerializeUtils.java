package bio.terra.cloudres.google.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;

/** Utils for serializing {@link com.google.cloud.storage} objects. */
class SerializeUtils {
  private SerializeUtils() {}

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
