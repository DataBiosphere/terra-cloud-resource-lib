package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.resources.GoogleBucketUid;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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

  /** See {@link Storage#create(BlobInfo, Storage.BlobTargetOption...)}. */
  public BlobCow create(BlobInfo blobInfo) {
    Blob blob =
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_CREATE_BLOB,
            () -> storage.create(blobInfo),
            () -> SerializeUtils.convert(blobInfo));
    return new BlobCow(clientConfig, blob);
  }

  /** See {@link Storage#create(BucketInfo, Storage.BucketTargetOption...)}. */
  public BucketCow create(BucketInfo bucketInfo) {
    clientConfig.recordForCleanup(new GoogleBucketUid().bucketName(bucketInfo.getName()));
    Bucket bucket =
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_CREATE_BUCKET,
            () -> storage.create(bucketInfo),
            () -> SerializeUtils.convert(bucketInfo));
    return new BucketCow(clientConfig, bucket);
  }

  /** See {@link Storage#createAcl(BlobId, Acl)} */
  public Acl createAcl(BlobId blob, Acl acl) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_CREATE_ACL_BLOB,
        () -> storage.createAcl(blob, acl),
        () -> {
          JsonObject request = new JsonObject();
          request.add("blob", SerializeUtils.convert(blob));
          request.add("acl", SerializeUtils.convert(acl));
          return request;
        });
  }

  /** See {@link Storage#get(BlobId)}. Returns null if blob is not found. */
  public BlobCow get(BlobId blob) {
    Blob rawBlob =
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_GET_BLOB,
            () -> storage.get(blob),
            () -> SerializeUtils.convert(blob));
    return (rawBlob == null) ? null : new BlobCow(clientConfig, rawBlob);
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
    return (rawBucket == null) ? null : new BucketCow(clientConfig, rawBucket);
  }

  /** See {@link Storage#getAcl(BlobId, Acl.Entity)}. */
  public Acl getAcl(BlobId blob, Acl.Entity entity) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_GET_ACL_BLOB,
        () -> storage.getAcl(blob, entity),
        () -> {
          JsonObject request = new JsonObject();
          request.add("blob", SerializeUtils.convert(blob));
          request.add("entity", SerializeUtils.convert(entity));
          return request;
        });
  }

  /** See {@link Storage#delete(BlobId)}. */
  public boolean delete(BlobId blob) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_BLOB,
        () -> storage.delete(blob),
        () -> SerializeUtils.convert(blob));
  }

  /** See {@link Storage#delete(String, Storage.BucketSourceOption...)}. */
  public boolean delete(String bucket) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_BUCKET,
        () -> storage.delete(bucket),
        () -> serializeBucketName(bucket));
  }

  /** See {@link Storage#deleteAcl(BlobId, Acl.Entity)}. */
  public boolean deleteAcl(BlobId blob, Acl.Entity entity) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_ACL_BLOB,
        () -> storage.deleteAcl(blob, entity),
        () -> {
          JsonObject request = new JsonObject();
          request.add("blob", SerializeUtils.convert(blob));
          request.add("entity", SerializeUtils.convert(entity));
          return request;
        });
  }

  /** See {@link Storage#writer(BlobInfo, Storage.BlobWriteOption...)} */
  public WriteChannel writer(BlobInfo blobInfo) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_CREATE_BLOB_AND_WRITER,
        () -> storage.writer(blobInfo),
        () -> SerializeUtils.convert(blobInfo));
  }

  private static JsonObject serializeBucketName(String bucketName) {
    JsonObject result = new JsonObject();
    result.addProperty("bucket_name", bucketName);
    return result;
  }
}
