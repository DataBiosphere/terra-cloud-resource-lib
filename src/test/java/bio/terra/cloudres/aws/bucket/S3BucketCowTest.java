package bio.terra.cloudres.aws.bucket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import java.io.IOException;
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
    bucketCow.putBlob(fakeBucketName, fakeObjectPath, requestBody);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = bucketCow.serialize(fakeBucketName, fakeObjectPath, requestBody);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        json.get("operation").getAsString(), S3BucketOperation.AWS_CREATE_S3_OBJECT.toString());
  }

  @Test
  public void createFolderTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String validFolderPath = "fake/path/to/folder/";
    // Create a folder with an invalid path, expect CRL to log an error
    try {
      bucketCow.createFolder(fakeBucketName, fakeObjectPath);
      // We should never reach this point without throwing an exception.
      fail();
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
    bucketCow.createFolder(fakeBucketName, validFolderPath);
    verify(mockLogger)
        .debug(secondStringArgumentCaptor.capture(), secondGsonArgumentCaptor.capture());
    JsonObject json = secondGsonArgumentCaptor.getValue();
    assertEquals(
        json.get("operation").getAsString(), S3BucketOperation.AWS_CREATE_S3_OBJECT.toString());
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
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        json.get("operation").getAsString(), S3BucketOperation.AWS_DELETE_S3_OBJECT.toString());
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
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        json.get("operation").getAsString(), S3BucketOperation.AWS_GET_S3_OBJECT.toString());
  }

  @Test
  public void listBlobsTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String fakeBlobPrefix = "fake/prefix/to/object";
    bucketCow.listBlobs(fakeBucketName, fakeBlobPrefix);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest =
        bucketCow.serialize(fakeBucketName, fakeBlobPrefix, (String) null);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        json.get("operation").getAsString(), S3BucketOperation.AWS_LIST_S3_OBJECTS.toString());
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
    JsonObject serializedRequest =
        bucketCow.serialize(fakeBucketName, fakeBlobPrefix, continuationToken);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        json.get("operation").getAsString(), S3BucketOperation.AWS_LIST_S3_OBJECTS.toString());
  }
}
