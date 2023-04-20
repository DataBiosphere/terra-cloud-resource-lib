package bio.terra.cloudres.aws.bucket;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;

/**
 * A Cloud Object Wrapper(COW) for AWS S3 Bucket Library: {@link S3Client}. Generally, this should
 * be used inside a try-with-resources block in order to close the underlying S3Client properly
 * after use.
 */
public class S3BucketCow implements AutoCloseable {

  private static Logger logger = LoggerFactory.getLogger(S3BucketCow.class);
  private final OperationAnnotator operationAnnotator;
  private final S3Client bucketClient;
  public static final int MAX_RESULTS_PER_REQUEST_S3 = 1000;

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
   * have a folder structure. Folders are really 0 byte objects with a path ending in "/", or they
   * may not actually exist (e.g. the existence of a file named /foo/bar/file.txt does not mean a 0
   * byte /foo/bar/ object exists).
   *
   * <p>This is a convenient wrapper around {@link #putBlob} for creating empty objects.
   */
  public void createFolder(String bucketName, String objPath, Collection<Tag> tags)
      throws AwsServiceException, SdkClientException, S3Exception {
    if (objPath == null || !objPath.endsWith("/")) {
      throw new IllegalArgumentException("S3 folder paths must end in a / character.");
    }
    putBlob(bucketName, objPath, tags, RequestBody.fromString(""));
  }

  public void putBlob(String bucketName, String objPath, Collection<Tag> tags, RequestBody contents)
      throws AwsServiceException, SdkClientException, S3Exception {
    operationAnnotator.executeCheckedCowOperation(
        S3BucketOperation.AWS_CREATE_S3_OBJECT,
        () ->
            bucketClient.putObject(
                PutObjectRequest.builder()
                    .bucket(bucketName)
                    .tagging(Tagging.builder().tagSet(tags).build())
                    .key(objPath)
                    .build(),
                contents),
        () -> serialize(bucketName, objPath, tags, contents));
  }

  public void deleteBlob(String bucketName, String objectPath)
      throws AwsServiceException, SdkClientException, S3Exception {
    operationAnnotator.executeCowOperation(
        S3BucketOperation.AWS_DELETE_S3_OBJECT,
        () ->
            bucketClient.deleteObject(
                DeleteObjectRequest.builder().bucket(bucketName).key(objectPath).build()),
        () -> serialize(bucketName, objectPath));
  }

  /**
   * Delete all objects in an AWS bucket with a common prefix, including the folder itself. Because
   * AWS can only support up to 1000 object deletions from a single request, this method may make
   * multiple calls to AWS infrastructure, each of which will be logged separately.
   */
  public void deleteFolder(String bucketName, String prefix)
      throws AwsServiceException, SdkClientException, S3Exception {
    String folderKey = prefix.endsWith("/") ? prefix : String.format("%s/", prefix);
    List<ObjectIdentifier> fullObjectList =
        listBlobs(bucketName, folderKey).contents().stream()
            .map(o -> ObjectIdentifier.builder().key(o.key()).build())
            .collect(Collectors.toList());
    Lists.partition(fullObjectList, MAX_RESULTS_PER_REQUEST_S3)
        .forEach(
            partitionedObjectList -> {
              operationAnnotator.executeCowOperation(
                  S3BucketOperation.AWS_DELETE_S3_FOLDER,
                  () ->
                      bucketClient.deleteObjects(
                          DeleteObjectsRequest.builder()
                              .bucket(bucketName)
                              .delete(Delete.builder().objects(partitionedObjectList).build())
                              .build()),
                  () -> serialize(bucketName, folderKey, partitionedObjectList.size()));
            });
  }

  public GetObjectResponse getBlob(String bucketName, String objectPath)
      throws IOException, AwsServiceException, SdkClientException, S3Exception {
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

  public ListObjectsV2Response listBlobs(String bucketName, String prefix)
      throws AwsServiceException, SdkClientException, S3Exception {
    // A null continuationToken is ignored by the AWS client
    return listBlobs(ListObjectsV2Request.builder().bucket(bucketName).prefix(prefix).build());
  }

  public ListObjectsV2Response listBlobs(String bucketName, String prefix, String continuationToken)
      throws AwsServiceException, SdkClientException, S3Exception {
    return listBlobs(
        ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .continuationToken(continuationToken)
            .build());
  }

  public ListObjectsV2Response listBlobs(String bucketName, String prefix, int maxObjects)
      throws AwsServiceException, SdkClientException, S3Exception {
    return listBlobs(
        ListObjectsV2Request.builder()
            .bucket(bucketName)
            .prefix(prefix)
            .maxKeys(maxObjects)
            .build());
  }

  public ListObjectsV2Response listBlobs(ListObjectsV2Request request)
      throws AwsServiceException, SdkClientException, S3Exception {
    return operationAnnotator.executeCowOperation(
        S3BucketOperation.AWS_LIST_S3_OBJECTS,
        () -> bucketClient.listObjectsV2(request),
        () -> serialize(request));
  }

  /**
   * In AWS, objects are really stored in a flat structure. However, they are displayed as if they
   * have a folder structure. Folders are really 0 byte objects with a path ending in "/", or they
   * may not actually exist (e.g. the existence of a file named /foo/bar/file.txt does not mean a 0
   * byte /foo/bar/ object exists).
   *
   * <p>This is a convenient wrapper around {@link #listBlobs} checking if any object names begin
   * with a particular prefix.
   *
   * @param bucketName The name of the bucket to query
   * @param folderPath The folder string to check for. This must be the full path from the top-level
   *     of the bucket, it cannot be a partial path.
   */
  public boolean folderExists(String bucketName, String folderPath)
      throws AwsServiceException, SdkClientException, S3Exception {
    String folderKey = folderPath.endsWith("/") ? folderPath : String.format("%s/", folderPath);
    return listBlobs(bucketName, folderKey).contents().size() > 0;
  }

  /**
   * Serialize several fields into a useful JSON object for logging with TCL. Note that this does
   * not log the entire request body, which could contain a large amount of data. Instead, this only
   * logs the content length.
   */
  @VisibleForTesting
  public JsonObject serialize(
      String bucketName, String pathPrefix, Collection<Tag> tags, RequestBody contents) {
    var ser = new JsonObject();
    ser.addProperty("bucketName", bucketName);
    ser.addProperty("pathPrefix", pathPrefix);
    // Tags represent key-value pairs, serialize them in the format "k1:v1,k2:v2,..."
    String serializedTags =
        tags.stream().map(tag -> tag.key() + ":" + tag.value()).collect(Collectors.joining(","));
    ser.addProperty("tags", String.join(",", serializedTags));
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
  public JsonObject serialize(String bucketName, String objectPath, int numObjects) {
    var ser = new JsonObject();
    ser.addProperty("bucketName", bucketName);
    ser.addProperty("objectPath", objectPath);
    ser.addProperty("numObjects", numObjects);
    return ser;
  }

  @VisibleForTesting
  public JsonObject serialize(ListObjectsV2Request request) {
    var ser = new JsonObject();
    ser.addProperty("bucketName", request.bucket());
    ser.addProperty("objectPath", request.prefix());
    ser.addProperty("continuationToken", request.continuationToken());
    ser.addProperty("maxKeys", request.maxKeys());
    return ser;
  }

  @Override
  public void close() {
    bucketClient.close();
  }
}
