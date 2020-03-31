package bio.terra.cloudres.google.storage;

import com.google.auth.Credentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableList;

public class GoogleCloudStorage {
    private Storage storage;

    public GoogleCloudStorage(Credentials credentials) {
        storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    public boolean deleteBucketRaw(String bucketName) {
        return storage.delete(bucketName);
    }

    public void setDeleteLifecycleRaw(String bucketName, Integer ageDays) {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            throw new BucketDoesNotExistException(bucketName);
        }
        bucket
                .toBuilder()
                .setLifecycleRules(
                        ImmutableList.of(
                                new BucketInfo.LifecycleRule(
                                        BucketInfo.LifecycleRule.LifecycleAction.newDeleteAction(),
                                        BucketInfo.LifecycleRule.LifecycleCondition.newBuilder().setAge(ageDays).build())))
                .build()
                .update();
    }
}

