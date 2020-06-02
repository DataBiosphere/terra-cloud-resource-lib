package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.CowOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
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
            new CowOperation<Blob>() {
              @Override
              public CloudOperation getCloudOperation() {
                return CloudOperation.GOOGLE_CREATE_BLOB;
              }

              @Override
              public Blob execute() {
                return storage.create(blobInfo);
              }

              @Override
              public String serializeRequest() {
                return SerializeUtils.convert(blobInfo);
              }
            });
    return new BlobCow(clientConfig, blob);
  }

  /** See {@link Storage#create(BucketInfo, Storage.BucketTargetOption...)}. */
  public BucketCow create(BucketInfo bucketInfo) {
    Bucket bucket =
        operationAnnotator.executeCowOperation(
            new CowOperation<Bucket>() {
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
                return SerializeUtils.convert(bucketInfo);
              }
            });
    return new BucketCow(clientConfig, bucket);
  }

  /** See {@link Storage#get(BlobId)}. */
  public BlobCow get(BlobId blob) {
    Blob rawBlob =
        operationAnnotator.executeCowOperation(
            new CowOperation<Blob>() {
              @Override
              public CloudOperation getCloudOperation() {
                return CloudOperation.GOOGLE_GET_BLOB;
              }

              @Override
              public Blob execute() {
                return storage.get(blob);
              }

              @Override
              public String serializeRequest() {
                return SerializeUtils.convert(blob);
              }
            });
    return new BlobCow(clientConfig, rawBlob);
  }

  /** See {@link Storage#get(String, Storage.BucketGetOption...)}. */
  public BucketCow get(String bucket) {
    Bucket rawBucket =
        operationAnnotator.executeCowOperation(
            new CowOperation<Bucket>() {
              @Override
              public CloudOperation getCloudOperation() {
                return CloudOperation.GOOGLE_GET_BUCKET;
              }

              @Override
              public Bucket execute() {
                return storage.get(bucket);
              }

              @Override
              public String serializeRequest() {
                return bucket;
              }
            });
    return new BucketCow(clientConfig, rawBucket);
  }

  /** See {@link Storage#delete(BlobId)}. */
  public boolean delete(BlobId blob) {
    return operationAnnotator.executeCowOperation(
        new CowOperation<Boolean>() {
          @Override
          public CloudOperation getCloudOperation() {
            return CloudOperation.GOOGLE_DELETE_BLOB;
          }

          @Override
          public Boolean execute() {
            return storage.delete(blob);
          }

          @Override
          public String serializeRequest() {
            return SerializeUtils.convert(blob);
          }
        });
  }

  /** See {@link Storage#delete(String, Storage.BucketSourceOption...)}. */
  public boolean delete(String bucket) {
    return operationAnnotator.executeCowOperation(
        new CowOperation<Boolean>() {
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
        });
  }

  /** See {@link Storage#writer(BlobInfo, Storage.BlobWriteOption...)} */
  public WriteChannel writer(BlobInfo blobInfo) {
    return operationAnnotator.executeCowOperation(
        new CowOperation<WriteChannel>() {
          @Override
          public CloudOperation getCloudOperation() {
            return CloudOperation.GOOGLE_CREATE_BLOB_AND_WRITER;
          }

          @Override
          public WriteChannel execute() {
            return storage.writer(blobInfo);
          }

          @Override
          public String serializeRequest() {
            return SerializeUtils.convert(blobInfo);
          }
        });
  }
}
