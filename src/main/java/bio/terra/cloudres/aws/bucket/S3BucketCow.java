package bio.terra.cloudres.aws.bucket;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * A Cloud Object Wrapper(COW) for AWS S3 Bucket Library: {@link S3Client}. Generally, this should
 * be used inside a try-with-resources block in order to close the underlying S3Client properly
 * after use.
 */
public class S3BucketCow implements AutoCloseable {

  private static Logger logger = LoggerFactory.getLogger(S3BucketCow.class);
  private final OperationAnnotator operationAnnotator;
  private final S3Client bucketClient;

  @VisibleForTesting
  public static void setLogger(Logger newLogger) {
    logger = newLogger;
  }

  public S3BucketCow(ClientConfig clientConfig, S3Client bucketClient) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bucketClient = bucketClient;
  }

  /** Create a {@link S3BucketCow} with some default configurations for convenience. */
  public static S3BucketCow create(
      ClientConfig clientConfig, AwsCredentialsProvider awsCredential, String region) {
    S3Client bucketsClient =
        S3Client.builder().region(Region.of(region)).credentialsProvider(awsCredential).build();
    return new S3BucketCow(clientConfig, bucketsClient);
  }

  // NOTE: We do not provide endpoints to create new S3 buckets, as Terra's model of AWS resources
  // uses a single static S3 bucket per landing zone (~region).

  /**
   * In AWS, objects are really stored in a flat structure. However, they are displayed as if they
   * have a folder structure. Folders are really 0 byte objects with a path ending in "/". This is a
   * convenient wrapper around {@link #putBlob} for creating empty objects.
   */
  public void createFolder(String bucketName, String objPath) {
    if (objPath == null || !objPath.endsWith("/")) {
      throw new IllegalArgumentException("S3 folder paths must end in a / character.");
    }
    putBlob(bucketName, objPath, RequestBody.fromString(""));
  }

  public void putBlob(String bucketName, String objPath, RequestBody contents) {
    operationAnnotator.executeCowOperation(
        S3BucketOperation.AWS_CREATE_S3_OBJECT,
        () ->
            bucketClient.putObject(
                PutObjectRequest.builder().bucket(bucketName).key(objPath).build(), contents),
        () -> serialize(bucketName, objPath, contents));
  }

  public void deleteBlob(String bucketName, String objectPath) {
    operationAnnotator.executeCowOperation(
        S3BucketOperation.AWS_DELETE_S3_OBJECT,
        () ->
            bucketClient.deleteObject(
                DeleteObjectRequest.builder().bucket(bucketName).key(objectPath).build()),
        () -> serialize(bucketName, objectPath));
  }

  public GetObjectResponse getBlob(String bucketName, String objectPath) throws IOException {
    // try-with-resources because getObject returns a stream which we must close.
    try (ResponseInputStream<GetObjectResponse> response =
        operationAnnotator.executeCowOperation(
            S3BucketOperation.AWS_GET_S3_OBJECT,
            () ->
                bucketClient.getObject(
                    GetObjectRequest.builder().bucket(bucketName).key(objectPath).build()),
            () -> serialize(bucketName, objectPath))) {
      return response.response();
    }
  }

  public ListObjectsV2Response listBlobs(String bucketName, String prefix) {
    // A null continuationToken is ignored by the AWS client
    return listBlobs(bucketName, prefix, /*continuationToken=*/ null);
  }

  public ListObjectsV2Response listBlobs(
      String bucketName, String prefix, String continuationToken) {
    return operationAnnotator.executeCowOperation(
        S3BucketOperation.AWS_LIST_S3_OBJECTS,
        () ->
            bucketClient.listObjectsV2(
                ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .continuationToken(continuationToken)
                    .build()),
        () -> serialize(bucketName, prefix, continuationToken));
  }

  /**
   * Serialize several fields into a useful JSON object for logging with TCL. Note that this does
   * not log the entire request body, which could contain a large amount of data. Instead, this only
   * logs the content length.
   */
  @VisibleForTesting
  public JsonObject serialize(String bucketName, String pathPrefix, RequestBody contents) {
    var ser = new JsonObject();
    ser.addProperty("bucketName", bucketName);
    ser.addProperty("pathPrefix", pathPrefix);
    // We do not need to log all data that services put in S3 buckets, just log metadata instead.
    ser.addProperty("contentLength", contents.optionalContentLength().orElse(0L));
    return ser;
  }

  @VisibleForTesting
  public JsonObject serialize(String bucketName, String objectPath) {
    var ser = new JsonObject();
    ser.addProperty("bucketName", bucketName);
    ser.addProperty("objectPath", objectPath);
    return ser;
  }

  @VisibleForTesting
  public JsonObject serialize(String bucketName, String objectPath, String continuationToken) {
    var ser = new JsonObject();
    ser.addProperty("bucketName", bucketName);
    ser.addProperty("objectPath", objectPath);
    ser.addProperty("continuationToken", continuationToken);
    return ser;
  }

  @Override
  public void close() {
    bucketClient.close();
  }
}
