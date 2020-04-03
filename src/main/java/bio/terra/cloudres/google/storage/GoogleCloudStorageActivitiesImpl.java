package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.DoNotRetrySupport;

public class GoogleCloudStorageActivitiesImpl implements GoogleCloudStorageActivities {
    private GoogleCloudStorage googleCloudStorage;

    public GoogleCloudStorageActivitiesImpl(GoogleCloudStorage googleCloudStorage) {
        this.googleCloudStorage = googleCloudStorage;
    }

    @Override
    public void createBucket(String bucketName, String projectId) {
        DoNotRetrySupport.wrapDoNotRetry(() -> this.googleCloudStorage.createBucketRaw(bucketName, projectId));
    }

    @Override
    public boolean deleteBucket(String bucketName) {
        return DoNotRetrySupport.wrapDoNotRetry(() -> this.googleCloudStorage.deleteBucketRaw(bucketName));
    }

    @Override
    public void setDeleteLifecycle(String bucketName, Integer ageDays) {
        DoNotRetrySupport.wrapDoNotRetry(() -> {
            try {
                this.googleCloudStorage.setDeleteLifecycleRaw(bucketName, ageDays);
            } catch (BucketDoesNotExistException bdnee) {
                throw new DoNotRetrySupport.DoNotRetryException(bdnee);
            }
        });
    }


}

