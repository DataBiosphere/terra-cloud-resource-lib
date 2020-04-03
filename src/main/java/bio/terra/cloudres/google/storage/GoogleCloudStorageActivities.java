package bio.terra.cloudres.google.storage;

public interface GoogleCloudStorageActivities {
    void createBucket(String bucketName, String projectId);

    boolean deleteBucket(String bucketName);

    void setDeleteLifecycle(String bucketName, Integer ageDays);
}

