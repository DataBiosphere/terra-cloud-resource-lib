package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.CowOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
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
    Bucket bucket = operationAnnotator.executeGoogleCall(new CreateBucketOperation(bucketInfo));
    return new BucketCow(clientConfig, bucket);
  }

  /** See {@link Storage#delete(String, Storage.BucketSourceOption...)}.*/
  public boolean delete(String bucket) {
    return operationAnnotator.executeGoogleCall(new DeleteBucketOperation(bucket));
  }

  /** A {@link CowOperation} for creating buckets. */
  private class CreateBucketOperation implements CowOperation<Bucket> {
    private final BucketInfo bucketInfo;

    private CreateBucketOperation(BucketInfo bucketInfo) {
      this.bucketInfo = bucketInfo;
    }

    @Override
    public CloudOperation getCloudOperation() {
      return CloudOperation.GOOGLE_CREATE_BUCKET;
    }

    @Override
    public Bucket execute() {
      return storage.create(bucketInfo);
    }

    @Override
    public String serializeRequest() {
      Gson gson = new Gson();
      return gson.toJson(bucketInfo, BucketInfo.class);
    }
  }

  /** A {@link CowOperation} for deleting buckets*/
  private class DeleteBucketOperation implements CowOperation<Boolean> {
      private final String bucket;

      private DeleteBucketOperation(String bucket) {
          this.bucket = bucket;
      }

      @Override
      public CloudOperation getCloudOperation() {
          return CloudOperation.GOOGLE_DELETE_BUCKET;
      }

      @Override
      public Boolean execute() {
          return storage.delete(bucket);
      }

      @Override
      public String serializeRequest() {
          return bucket;
      }
  }
}
