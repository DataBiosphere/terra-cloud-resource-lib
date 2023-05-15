package bio.terra.cloudres.aws.notebook;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.common.ClientConfig;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import software.amazon.awssdk.services.sagemaker.SageMakerClient;
import software.amazon.awssdk.services.sagemaker.model.CreateNotebookInstanceRequest;
import software.amazon.awssdk.services.sagemaker.model.CreateNotebookInstanceResponse;
import software.amazon.awssdk.services.sagemaker.model.CreatePresignedNotebookInstanceUrlRequest;
import software.amazon.awssdk.services.sagemaker.model.CreatePresignedNotebookInstanceUrlResponse;
import software.amazon.awssdk.services.sagemaker.waiters.SageMakerWaiter;

/**
 * Note: For AWS APIs, we do not significantly modify the API surface, we just decorate with useful
 * features (currently, logging and metric generation). Unlike GCP APIs, these are entirely unit
 * tests that validate CRL behavior but do not call out to live AWS environments. Services should
 * perform connected or integration tests as necessary to validate integration with AWS.
 */
@Tag("unit")
public class SageMakerNotebookCowTest {

  private SageMakerNotebookCow notebookCow;
  @Mock SageMakerClient mockSageMakerClient = mock(SageMakerClient.class);
  @Mock SageMakerWaiter mockSageMakerWaiter = mock(SageMakerWaiter.class);
  @Mock private Logger mockLogger = mock(Logger.class);
  private final String instanceName = "fakeInstance";

  @BeforeEach
  public void setupMocks() {
    ClientConfig unitTestConfig =
        ClientConfig.Builder.newBuilder().setClient("S3BucketCowTest").build();
    SageMakerNotebookCow.setLogger(mockLogger);
    notebookCow =
        new SageMakerNotebookCow(unitTestConfig, mockSageMakerClient, mockSageMakerWaiter);
  }

  @Test
  public void createNotebookTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    CreateNotebookInstanceRequest request =
        CreateNotebookInstanceRequest.builder().notebookInstanceName("myInstance").build();
    when(mockSageMakerClient.createNotebookInstance((CreateNotebookInstanceRequest) any()))
        .thenReturn(
            CreateNotebookInstanceResponse.builder().notebookInstanceArn("myInstanceArn").build());
    notebookCow.create(request);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = notebookCow.createJsonObjectWithSingleField("request", request);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        SageMakerNotebookOperation.AWS_CREATE_NOTEBOOK.toString(),
        json.get("operation").getAsString());
  }

  @Test
  public void getNotebookTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    notebookCow.get(instanceName);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = notebookCow.serializeInstanceName(instanceName);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        SageMakerNotebookOperation.AWS_GET_NOTEBOOK.toString(),
        json.get("operation").getAsString());
  }

  @Test
  public void createPresignedUrlNotebookTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    String fakeUrl = "https://example.com";
    when(mockSageMakerClient.createPresignedNotebookInstanceUrl(
            (CreatePresignedNotebookInstanceUrlRequest) any()))
        .thenReturn(
            CreatePresignedNotebookInstanceUrlResponse.builder().authorizedUrl(fakeUrl).build());
    assertEquals(fakeUrl, notebookCow.createPresignedUrl(instanceName));
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = notebookCow.serializeInstanceName(instanceName);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        SageMakerNotebookOperation.AWS_CREATE_PRESIGNED_URL_NOTEBOOK.toString(),
        json.get("operation").getAsString());
  }

  @Test
  public void startNotebookTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    notebookCow.start(instanceName);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = notebookCow.serializeInstanceName(instanceName);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        SageMakerNotebookOperation.AWS_START_NOTEBOOK.toString(),
        json.get("operation").getAsString());
  }

  @Test
  public void stopNotebookTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    notebookCow.stop(instanceName);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = notebookCow.serializeInstanceName(instanceName);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        SageMakerNotebookOperation.AWS_STOP_NOTEBOOK.toString(),
        json.get("operation").getAsString());
  }

  @Test
  public void deleteNotebookTest() {
    ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
    notebookCow.delete(instanceName);
    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    JsonObject serializedRequest = notebookCow.serializeInstanceName(instanceName);
    assertEquals(json.getAsJsonObject("requestData"), serializedRequest);
    assertEquals(
        SageMakerNotebookOperation.AWS_DELETE_NOTEBOOK.toString(),
        json.get("operation").getAsString());
  }
}
