package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.util.SerializeHelper;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBlobUid;
import com.google.cloud.Policy;
import com.google.cloud.storage.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Utils for serializing {@link com.google.cloud.storage} objects. */
public class SerializeUtils extends SerializeHelper {
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

  static JsonObject convert(String bucketName, Storage.BlobListOption... options) {
    Gson gson = createGson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("bucketName", gson.toJsonTree(bucketName));
    jsonObject.add("blobListOption", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(BucketInfo bucketInfo, Storage.BucketTargetOption... options) {
    Gson gson = createGson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("bucketInfo", convertWithGson(bucketInfo, BucketInfo.class));
    jsonObject.add("bucketTargetOption", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(Policy policy) {
    return convertWithGson(policy, Policy.class);
  }
}
