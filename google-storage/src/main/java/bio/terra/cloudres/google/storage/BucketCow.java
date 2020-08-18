package bio.terra.cloudres.google.storage;

import static bio.terra.cloudres.google.storage.SerializeUtils.convert;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.TransformPage;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for {@link Bucket}. */
public class BucketCow {
  private final Logger logger = LoggerFactory.getLogger(BucketCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Bucket bucket;

  BucketCow(ClientConfig clientConfig, Bucket bucket) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bucket = bucket;
    this.clientConfig = clientConfig;
  }

  public BucketInfo getBucketInfo() {
    return bucket;
  }

  /** See {@link Bucket#list(Storage.BlobListOption...)} */
  public Page<BlobCow> list(Storage.BlobListOption... options) {
    return new TransformPage<>(
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_LIST_BLOB,
            () -> bucket.list(options),
            () -> convert(bucket.getName(), options)),
        (Blob t) -> new BlobCow(clientConfig, t));
  }

  /** See {@link Bucket#update(Storage.BucketTargetOption...)} */
  public BucketCow update(Storage.BucketTargetOption... options) {
    return new BucketCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_UPDATE_BUCKET,
            () -> bucket.update(options),
            () -> convert(bucket.getName(), options)));
  }

  /** See {@link Bucket#delete(Bucket.BucketSourceOption...)} */
  public boolean delete() {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_BUCKET,
        () -> bucket.delete(),
        () -> {
          JsonObject request = new JsonObject();
          request.addProperty("bucket_name", bucket.getName());
          return request;
        });
  }
}
