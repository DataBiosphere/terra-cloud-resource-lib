package bio.terra.cloudres.aws.bucket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Note: For AWS APIs, we do not significantly modify the API surface, we just decorate with useful
 * features (currently, logging and metric generation). Unlike GCP APIs, these are entirely unit
 * tests that validate CRL behavior but do not call out to live AWS environments. Services should
 * perform connected or integration tests as necessary to validate integration with AWS.
 */
@Tag("unit")
public class S3BucketCowTest {

  private S3BucketCow bucketCow;

  @Mock private S3Client mockS3Client = mock(S3Client.class);
  @Mock private Logger mockLogger = mock(Logger.class);
  private final String fakeBucketName = "fakeBucketName";
  private final String fakeObjectPath = "fake/path/to/object";
  private final List<software.amazon.awssdk.services.s3.model.Tag> defaultTags =
      List.of(
          software.amazon.awssdk.services.s3.model.Tag.builder().key("foo").value("bar").build(),
          software.amazon.awssdk.services.s3.model.Tag.builder()
              .key("secondKey")
              .value("secondValue")
              .build());

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("S3BucketCowTest").build();
    S3BucketCow.setLogger(mockLogger);
    bucketCow = new S3BucketCow(unitTestConfig, mockS3Client);
  }

  @Test
  public void putBlobTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    RequestBody requestBody = RequestBody.fromString("thisIsABlob");
    bucketCow.putBlob(fakeBucketName, fakeObjectPath, defaultTags, requestBody);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest =
        bucketCow.serialize(fakeBucketName, fakeObjectPath, defaultTags, requestBody);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_CREATE_S3_OBJECT.toString(), json.get("operation").getAsString());
  }

  @Test
  public void createFolderTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String validFolderPath = "fake/path/to/folder/";
    // Create a folder with an invalid path, expect CRL to log an error
    try {
      bucketCow.createFolder(fakeBucketName, fakeObjectPath, defaultTags);
      fail("CRL should not allow creating a folder with a name that does not end in /");
    } catch (IllegalArgumentException e) {
      // Expected exception, do nothing.
    }
    // Because validation failed, the underlying API should not be called, and we should not log
    // anything.
    verify(mockLogger, times(0))
        .debug(
            stringArgumentCaptor.capture(),
            stringArgumentCaptor.capture(),
            stringArgumentCaptor.capture());
    // Create a folder with a valid path, expect success
    ArgumentCaptor<String> secondStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> secondGsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    bucketCow.createFolder(fakeBucketName, validFolderPath, defaultTags);
    verify(mockLogger)
        .debug(secondStringArgumentCaptor.capture(), secondGsonArgumentCaptor.capture());
    JsonObject json = secondGsonArgumentCaptor.getValue();
    assertEquals(
        S3BucketOperation.AWS_CREATE_S3_OBJECT.toString(), json.get("operation").getAsString());
    assertFalse(json.has("exception"));
  }

  @Test
  public void deleteBlobTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    bucketCow.deleteBlob(fakeBucketName, fakeObjectPath);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = bucketCow.serialize(fakeBucketName, fakeObjectPath);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_DELETE_S3_OBJECT.toString(), json.get("operation").getAsString());
  }

  @Test
  public void deleteFolderTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    // Mock a response, as we hit an NPE otherwise. The real S3 API will not return null as a
    // response.
    ListObjectsV2Response mockResponse = mock(ListObjectsV2Response.class);
    S3Object mockObject = mock(S3Object.class);
    when(mockResponse.contents()).thenReturn(List.of(mockObject));
    when(mockS3Client.listObjectsV2((ListObjectsV2Request) any())).thenReturn(mockResponse);

    bucketCow.deleteFolder(fakeBucketName, fakeObjectPath);
    // Expect one AWS call to list objects with a prefix and one call to delete them.
    verify(mockLogger, times(2))
        .debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    // Expect the serialized request to have a trailing / appended, as fakeObjectPath does not end
    // in /
    JsonObject serializedRequest = bucketCow.serialize(fakeBucketName, fakeObjectPath + "/", 1);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_DELETE_S3_FOLDER.toString(), json.get("operation").getAsString());

    // Repeat the folder delete using a folder path with a trailing / to verify the method does
    // not append an additional /.
    String fakeObjectPathWithTrailingSlash = fakeObjectPath + "/";
    ArgumentCaptor<String> secondStringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> secondGsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    verify(mockLogger, times(2))
        .debug(secondStringArgumentCaptor.capture(), secondGsonArgumentCaptor.capture());
    JsonObject secondJson = secondGsonArgumentCaptor.getValue();
    JsonObject serializedRequestWithSlash =
        bucketCow.serialize(fakeBucketName, fakeObjectPathWithTrailingSlash, 1);
    assertEquals(serializedRequestWithSlash, secondJson.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_DELETE_S3_FOLDER.toString(),
        secondJson.get("operation").getAsString());
  }

  @Test
  public void getBlobTest() throws IOException {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    // Mock a response, as we hit an NPE otherwise. The real S3 API will not return null as a
    // response.
    ResponseInputStream<GetObjectResponse> mockResponseStream = mock(ResponseInputStream.class);
    GetObjectResponse mockResponse = mock(GetObjectResponse.class);
    when(mockResponseStream.response()).thenReturn(mockResponse);
    when(mockS3Client.getObject((GetObjectRequest) any())).thenReturn(mockResponseStream);

    bucketCow.getBlob(fakeBucketName, fakeObjectPath);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = bucketCow.serialize(fakeBucketName, fakeObjectPath);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_GET_S3_OBJECT.toString(), json.get("operation").getAsString());
  }

  @Test
  public void listBlobsTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String fakeBlobPrefix = "fake/prefix/to/object";
    bucketCow.listBlobs(fakeBucketName, fakeBlobPrefix);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    ListObjectsV2Request expectedRequest =
        ListObjectsV2Request.builder().bucket(fakeBucketName).prefix(fakeBlobPrefix).build();
    JsonObject serializedRequest = bucketCow.serialize(expectedRequest);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_LIST_S3_OBJECTS.toString(), json.get("operation").getAsString());
  }

  @Test
  public void listBlobsWithContinuationTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String fakeBlobPrefix = "fake/prefix/to/object";
    String continuationToken = "more_objects_please";
    bucketCow.listBlobs(fakeBucketName, fakeBlobPrefix, continuationToken);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    ListObjectsV2Request expectedRequest =
        ListObjectsV2Request.builder()
            .bucket(fakeBucketName)
            .prefix(fakeBlobPrefix)
            .continuationToken(continuationToken)
            .build();
    JsonObject serializedRequest = bucketCow.serialize(expectedRequest);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_LIST_S3_OBJECTS.toString(), json.get("operation").getAsString());
  }

  @Test
  public void listBlobsWithMaxKeysTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String fakeBlobPrefix = "fake/prefix/to/object";
    bucketCow.listBlobs(fakeBucketName, fakeBlobPrefix, 100);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    ListObjectsV2Request expectedRequest =
        ListObjectsV2Request.builder()
            .bucket(fakeBucketName)
            .prefix(fakeBlobPrefix)
            .maxKeys(100)
            .build();
    JsonObject serializedRequest = bucketCow.serialize(expectedRequest);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_LIST_S3_OBJECTS.toString(), json.get("operation").getAsString());
  }

  @Test
  public void folderExistsTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String missingFolderPrefix = "this/path/does/not/exist/";
    String realFolderPrefix = "this/path/exists/";
    ListObjectsV2Response emptyResponse = ListObjectsV2Response.builder().build();
    S3Object fakeObject = mock(S3Object.class);
    ListObjectsV2Response responseWithObject =
        ListObjectsV2Response.builder().contents(fakeObject).build();
    // Return an empty response for the missingFolderPrefix folder and a non-empty response for
    // the realFolderPrefix.
    when(mockS3Client.listObjectsV2((ListObjectsV2Request) any())).thenReturn(emptyResponse);
    assertFalse(bucketCow.folderExists(fakeBucketName, missingFolderPrefix));
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    ListObjectsV2Request expectedRequest =
        ListObjectsV2Request.builder().bucket(fakeBucketName).prefix(missingFolderPrefix).build();
    JsonObject serializedRequest = bucketCow.serialize(expectedRequest);
    assertEquals(serializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_LIST_S3_OBJECTS.toString(), json.get("operation").getAsString());

    when(mockS3Client.listObjectsV2((ListObjectsV2Request) any())).thenReturn(responseWithObject);
    assertTrue(bucketCow.folderExists(fakeBucketName, realFolderPrefix));
  }

  @Test
  public void folderExistsHandlesTrailingSlash() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String prefixWithoutSlash = "this/path/exists";
    S3Object fakeObject = mock(S3Object.class);
    ListObjectsV2Response responseWithObject =
        ListObjectsV2Response.builder().contents(fakeObject).build();
    when(mockS3Client.listObjectsV2((ListObjectsV2Request) any())).thenReturn(responseWithObject);

    // Verify that prefix is always treated as if it has a trailing /, including for serialization
    assertTrue(bucketCow.folderExists(fakeBucketName, prefixWithoutSlash));
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    // Expect the serialized request to have a trailing / appended
    ListObjectsV2Request expectedRequest =
        ListObjectsV2Request.builder()
            .bucket(fakeBucketName)
            .prefix(prefixWithoutSlash + "/")
            .build();
    JsonObject expectedSerializedRequest = bucketCow.serialize(expectedRequest);
    assertEquals(expectedSerializedRequest, json.getAsJsonObject("requestData"));
    assertEquals(
        S3BucketOperation.AWS_LIST_S3_OBJECTS.toString(), json.get("operation").getAsString());
  }
}
