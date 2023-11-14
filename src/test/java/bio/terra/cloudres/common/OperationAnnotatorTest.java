package bio.terra.cloudres.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.testing.StubCloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.ResourceManagerException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.time.Duration;
import java.util.OptionalInt;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

/** Test for {@link OperationAnnotator} */
@Tag("unit")
public class OperationAnnotatorTest {
  private static final String CLIENT = "TestClient";
  private static final Gson JSON_PARSER = new GsonBuilder().setLenient().create();
  // A fake but somewhat-realistic create-project request payload
  private static final JsonObject PROJECT_REQUEST =
      JSON_PARSER.fromJson(
          "{name: 'myProj', projectId: 'project-id', labels: {k1: 'v1', k2: 'v2'}}",
          JsonObject.class);

  private static final String ERROR_MESSAGE = "error!";

  /** use the {@link ResourceManagerException} as an example of a BaseHttpServiceException. */
  private static final ResourceManagerException RM_EXCEPTION =
      new ResourceManagerException(404, ERROR_MESSAGE);

  private static final OperationAnnotator.CowExecute<?> FAILED_COW_EXECUTE =
      () -> {
        throw new ResourceManagerException(404, ERROR_MESSAGE);
      };

  private static final OperationAnnotator.CowExecute<?> SUCCESS_COW_EXECUTE =
      () -> {
        try {
          Thread.sleep(4100);
          return null;
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      };

  private static final OperationAnnotator.CowSerialize SERIALIZE =
      () -> {
        return PROJECT_REQUEST;
      };

  ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
  ArgumentCaptor<JsonObject> gsonArgumentCaptor = ArgumentCaptor.forClass(JsonObject.class);
  ArgumentCaptor<Exception> exceptionArgumentCaptor = ArgumentCaptor.forClass(Exception.class);

  @Mock private Logger mockLogger;
  @Mock private MetricsHelper mockMetricsHelper;

  private ClientConfig clientConfig;
  private OperationAnnotator operationAnnotator;
  private AutoCloseable closeableMocks;

  @BeforeEach
  public void setup() {
    closeableMocks = MockitoAnnotations.openMocks(this);
    clientConfig =
        ClientConfig.Builder.newBuilder()
            .setClient(CLIENT)
            .setMetricsHelper(mockMetricsHelper)
            .build();
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);
  }

  @AfterEach
  public void tearDown() throws Exception {
    closeableMocks.close();
  }

  @Test
  public void testExecuteGoogleCloudCall_success() {
    operationAnnotator.executeCowOperation(
        StubCloudOperation.TEST_OPERATION, SUCCESS_COW_EXECUTE, SERIALIZE);

    verify(mockMetricsHelper)
        .recordLatency(
            eq(clientConfig.getClientName()), eq(StubCloudOperation.TEST_OPERATION), any());
    verify(mockMetricsHelper)
        .recordError(
            clientConfig.getClientName(), StubCloudOperation.TEST_OPERATION, OptionalInt.of(200));
    verify(mockMetricsHelper)
        .recordApiCount(clientConfig.getClientName(), StubCloudOperation.TEST_OPERATION);
    verify(mockLogger).debug(anyString(), any(JsonObject.class));
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() {
    Assert.assertThrows(
        ResourceManagerException.class,
        () ->
            operationAnnotator.executeCowOperation(
                StubCloudOperation.TEST_OPERATION, FAILED_COW_EXECUTE, SERIALIZE));

    verify(mockMetricsHelper)
        .recordError(
            clientConfig.getClientName(), StubCloudOperation.TEST_OPERATION, OptionalInt.of(404));
    verify(mockMetricsHelper)
        .recordApiCount(clientConfig.getClientName(), StubCloudOperation.TEST_OPERATION);
    verify(mockLogger)
        .debug(anyString(), any(JsonObject.class), any(ResourceManagerException.class));
  }

  @Test
  public void testExecuteGoogleCloudCall_withCheckedException() throws Exception {
    Assert.assertThrows(
        InterruptedException.class,
        () ->
            operationAnnotator.executeCheckedCowOperation(
                StubCloudOperation.TEST_OPERATION,
                () -> {
                  throw new InterruptedException(ERROR_MESSAGE);
                },
                SERIALIZE));

    verify(mockMetricsHelper)
        .recordError(
            clientConfig.getClientName(), StubCloudOperation.TEST_OPERATION, OptionalInt.empty());
    verify(mockMetricsHelper)
        .recordApiCount(clientConfig.getClientName(), StubCloudOperation.TEST_OPERATION);
    verify(mockLogger).debug(anyString(), any(JsonObject.class), any(InterruptedException.class));
  }

  @Test
  public void testLogEvent() {
    operationAnnotator.logEvent(
        OperationData.builder()
            .setCloudOperation(StubCloudOperation.TEST_OPERATION)
            .setRequestData(PROJECT_REQUEST)
            .setDuration(Duration.ofMillis(2345))
            .build());

    verify(mockLogger).debug(stringArgumentCaptor.capture(), gsonArgumentCaptor.capture());
    JsonObject json = gsonArgumentCaptor.getValue();
    assertThat(
        stringArgumentCaptor.getValue(),
        Matchers.containsString("CRL completed TEST_OPERATION (2.3s)"));
    assertEquals(json.getAsJsonPrimitive("clientName").getAsString(), "TestClient");
    assertEquals(json.getAsJsonPrimitive("durationMs").getAsLong(), 2345l);
    assertEquals(json.getAsJsonPrimitive("operation").getAsString(), "TEST_OPERATION");
    assertEquals(json.getAsJsonObject("requestData"), PROJECT_REQUEST);
    // Exception should not be included in JSON if not present.
    assertFalse(json.has("exception"));
  }

  @Test
  public void testLogEvent_withException() {
    operationAnnotator.logEvent(
        OperationData.builder()
            .setCloudOperation(StubCloudOperation.TEST_OPERATION)
            .setRequestData(PROJECT_REQUEST)
            .setDuration(Duration.ofMillis(2345))
            .setExecutionException(RM_EXCEPTION)
            .setHttpStatusCode(404)
            .build());

    verify(mockLogger)
        .debug(
            stringArgumentCaptor.capture(),
            gsonArgumentCaptor.capture(),
            exceptionArgumentCaptor.capture());
    assertThat(
        stringArgumentCaptor.getValue(),
        Matchers.containsString("CRL exception in TEST_OPERATION (HTTP code 404, 2.3s)"));
    assertTrue(gsonArgumentCaptor.getValue().has("exception"));
    // Verify that the exception is also included as a separate logger argument, so it can be picked
    // up by SLF4J.
    assertEquals(exceptionArgumentCaptor.getValue(), RM_EXCEPTION);
  }
}
