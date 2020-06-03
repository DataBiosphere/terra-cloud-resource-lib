package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for {@link Bucket}. */
public class BucketCow {
  private final Logger logger = LoggerFactory.getLogger(BucketCow.class);

  private final OperationAnnotator operationAnnotator;
  private final Bucket bucket;

  BucketCow(ClientConfig clientConfig, Bucket bucket) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bucket = bucket;
  }

  public BucketInfo getBucketInfo() {
    return bucket;
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
