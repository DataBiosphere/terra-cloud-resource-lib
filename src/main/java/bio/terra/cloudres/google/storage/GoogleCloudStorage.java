package bio.terra.cloudres.google.storage;

import com.google.auth.Credentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GoogleCloudStorage {
  private Storage storage;
  private Credentials credentials;

  public GoogleCloudStorage(Credentials credentials) {
    this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    this.credentials = credentials;
  }

  public GoogleCloudStorage(Credentials credentials, String projectId) {
    this.storage = StorageOptions.newBuilder().setProjectId(projectId).setCredentials(credentials).build().getService();
    this.credentials = credentials;
  }

  public boolean deleteBucketRaw(String bucketName) {
    return storage.delete(bucketName);
  }

  public Bucket create(BucketInfo bucketInfo, Storage.BucketTargetOption... options) {
    return storage.create(bucketInfo, options);
  }

  public Bucket get(String bucketName, Storage.BucketGetOption... options) {
    return storage.get(bucketName, options);
  }
}
