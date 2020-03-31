package bio.terra.cloudres.google.storage;

public interface GoogleCloudStorageActivities {
    boolean deleteBucket(String bucketName);

    void setDeleteLifecycleRaw(String bucketName, Integer ageDays);
}

