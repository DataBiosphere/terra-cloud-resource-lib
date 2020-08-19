package bio.terra.cloudres.google.storage;

import static bio.terra.cloudres.google.storage.SerializeUtils.convert;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.TransformPage;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.gson.JsonObject;
import java.util.Map;
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

  /**
   * See {@link Bucket#update(Storage.BucketTargetOption...)}
   *
   * <p>Example of updating the bucket's information.
   *
   * <pre>{@code
   * BucketCow updatedBucket = bucketCow.toBuilder().setVersioningEnabled(true).build().update();
   * }</pre>
   */
  public BucketCow update(Storage.BucketTargetOption... options) {
    return new BucketCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_UPDATE_BUCKET,
            () -> bucket.update(options),
            () -> convert(bucket, options)));
  }

  /** See {@link Bucket#delete(Bucket.BucketSourceOption...)} */
  public boolean delete() {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_BUCKET,
        bucket::delete,
        () -> {
          JsonObject request = new JsonObject();
          request.addProperty("bucket_name", bucket.getName());
          return request;
        });
  }

  public Builder toBuilder() {
    return new Builder(bucket.toBuilder(), clientConfig);
  }

  /** Builder for {@link BucketCow} and setters are mapped to {@link Bucket.Builder}. */
  public static class Builder {
    private final Bucket.Builder bucketBuilder;
    private final ClientConfig clientConfig;

    private Builder(Bucket.Builder bucketBuilder, ClientConfig clientConfig) {
      this.bucketBuilder = bucketBuilder;
      this.clientConfig = clientConfig;
    }

    public Builder setVersioningEnabled(Boolean enable) {
      bucketBuilder.setVersioningEnabled(enable);
      return this;
    }

    public Builder setLifecycleRules(Iterable<? extends BucketInfo.LifecycleRule> rules) {
      bucketBuilder.setLifecycleRules(rules);
      return this;
    }

    public Builder setAcl(Iterable<Acl> acl) {
      bucketBuilder.setAcl(acl);
      return this;
    }

    public Builder setLabels(Map<String, String> labels) {
      bucketBuilder.setLabels(labels);
      return this;
    }

    public BucketCow build() {
      return new BucketCow(clientConfig, bucketBuilder.build());
    }
  }
}
