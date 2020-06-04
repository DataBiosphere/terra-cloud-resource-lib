package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for {@link Storage}. */
public class StorageCow {
  private final Logger logger = LoggerFactory.getLogger(StorageCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Storage storage;

  public StorageCow(ClientConfig clientConfig, StorageOptions storageOptions) {
    this.clientConfig = clientConfig;
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.storage = storageOptions.getService();
  }

  /** See {@link Storage#create(BucketInfo, Storage.BucketTargetOption...)}. */
  public BucketCow create(BucketInfo bucketInfo) {
    Bucket bucket =
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_CREATE_BUCKET,
            () -> storage.create(bucketInfo),
            () -> new Gson().toJsonTree(bucketInfo, BucketInfo.class).getAsJsonObject());
    return new BucketCow(clientConfig, bucket);
  }

  /**
   * See {@link Storage#get(String, Storage.BucketGetOption...)}. Returns null if no bucket is
   * found.
   */
  public BucketCow get(String bucket) {
    Bucket rawBucket =
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_GET_BUCKET,
            () -> storage.get(bucket),
            () -> serializeBucketName(bucket));
    if (rawBucket == null) {
      return null;
    }
    return new BucketCow(clientConfig, rawBucket);
  }

  /** See {@link Storage#delete(String, Storage.BucketSourceOption...)}. */
  public boolean delete(String bucket) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_BUCKET,
        () -> storage.delete(bucket),
        () -> serializeBucketName(bucket));
  }

  private static JsonObject serializeBucketName(String bucketName) {
    JsonObject result = new JsonObject();
    result.addProperty("bucket_name", bucketName);
    return result;
  }
}
