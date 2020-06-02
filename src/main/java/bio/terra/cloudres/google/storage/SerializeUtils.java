package bio.terra.cloudres.google.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.BucketInfo;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/** Utils for serializing {@link com.google.cloud.storage} objects. */
class SerializeUtils {
  private SerializeUtils() {}

  static String convert(BucketInfo bucketInfo) {
    return convertWithGson(bucketInfo, BucketInfo.class);
  }

  static String convert(BlobInfo blobInfo) {
    return convertWithGson(blobInfo, BlobInfo.class);
  }

  static String convert(BlobId blobId) {
    return convertWithGson(blobId, BlobId.class);
  }

  /**
   * Helper for Gson convertible classes. Should only be used with classes that play nicely with
   * Gson.
   */
  private static <R> String convertWithGson(R r, Type t) {
    Gson gson = new Gson();
    return gson.toJson(r, t);
  }
}
