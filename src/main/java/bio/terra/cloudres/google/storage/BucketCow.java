package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.CowOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
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
        new CowOperation<Boolean>() {
          @Override
          public CloudOperation getCloudOperation() {
            return CloudOperation.GOOGLE_DELETE_BUCKET;
          }

          @Override
          public Boolean execute() {
            return bucket.delete();
          }

          @Override
          public String serializeRequest() {
            return bucket.getName();
          }
        });
  }
}
