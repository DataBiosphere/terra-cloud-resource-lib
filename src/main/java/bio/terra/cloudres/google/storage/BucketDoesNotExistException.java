package bio.terra.cloudres.google.storage;

public class BucketDoesNotExistException extends RuntimeException {
    public BucketDoesNotExistException(String bucketName) {
        super("bucket " + bucketName + "does not exist");
    }
}
