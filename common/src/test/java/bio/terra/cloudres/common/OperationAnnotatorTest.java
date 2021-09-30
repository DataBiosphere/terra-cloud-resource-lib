package bio.terra.cloudres.common;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.testing.StubCloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.ResourceManagerException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.opencensus.stats.AggregationData;
import java.time.Duration;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private final Logger logger = LoggerFactory.getLogger(OperationAnnotatorTest.class);

  @Mock private Logger mockLogger = mock(Logger.class);

  private ClientConfig clientConfig = ClientConfig.Builder.newBuilder().setClient(CLIENT).build();
  private OperationAnnotator operationAnnotator = new OperationAnnotator(clientConfig, logger);

  @Test
  public void testExecuteGoogleCloudCall_success() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT_404);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    operationAnnotator.executeCowOperation(
        StubCloudOperation.TEST_OPERATION, SUCCESS_COW_EXECUTE, SERIALIZE);

    sleepForSpansExport();

    // One cloud api count
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // No error
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT_404, errorCount, 0);

    // This rely on the latency DistributionData defined in {@link MetricHelper} where 4s - 8s are
    // in the same bucket.
    // This would expect the latency falls into the 4s-8s bucket(25th).
    AggregationData.DistributionData data =
        (AggregationData.DistributionData)
            MetricsHelper.viewManager.getView(LATENCY_VIEW_NAME).getAggregationMap().get(API_COUNT);

    // 4~8s bucket,
    assertEquals(data.getBucketCounts().get(24).longValue(), 1);
  }

  @Test
  public void testExecuteGoogleCloudCall_withException() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT_404);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    Assert.assertThrows(
        ResourceManagerException.class,
        () ->
            operationAnnotator.executeCowOperation(
                StubCloudOperation.TEST_OPERATION, FAILED_COW_EXECUTE, SERIALIZE));

    sleepForSpansExport();

    // Assert cloud api count increase by 1
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // Assert error count increase by 1
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT_404, errorCount, 1);
  }

  @Test
  public void testExecuteGoogleCloudCall_withCheckedException() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT_404);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    Assert.assertThrows(
        InterruptedException.class,
        () ->
            operationAnnotator.executeCheckedCowOperation(
                StubCloudOperation.TEST_OPERATION,
                () -> {
                  throw new InterruptedException(ERROR_MESSAGE);
                },
                SERIALIZE));

    sleepForSpansExport();

    // Assert cloud api count increase by 1
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // No 404 error
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT_404, errorCount, 0);
  }

  @Test
  public void testLogEvent() throws Exception {
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);

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
  public void testLogEvent_withException() throws Exception {
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);

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

  @Test
  public void testRecordOperation() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT_404);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);

    operationAnnotator.recordOperation(
        OperationData.builder()
            .setCloudOperation(StubCloudOperation.TEST_OPERATION)
            .setRequestData(PROJECT_REQUEST)
            .setDuration(Duration.ofMillis(1100))
            .build());

    sleepForSpansExport();

    // One cloud api count
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // No error
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT_404, errorCount, 0);

    // This rely on the latency DistributionData defined in {@link MetricHelper} where 1s - 2s are
    // in the same bucket.
    // This would expect the latency falls into the 1s-2s bucket(23rd).
    AggregationData.DistributionData data =
        (AggregationData.DistributionData)
            MetricsHelper.viewManager.getView(LATENCY_VIEW_NAME).getAggregationMap().get(API_COUNT);

    assertEquals(data.getBucketCounts().get(22).longValue(), 1);

    // Verify logger was invoked
    verify(mockLogger, times(1)).debug(anyString(), any(JsonObject.class));
  }

  @Test
  public void testRecordOperation_withException() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT_404);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);

    operationAnnotator.recordOperation(
        OperationData.builder()
            .setCloudOperation(StubCloudOperation.TEST_OPERATION)
            .setRequestData(PROJECT_REQUEST)
            .setDuration(Duration.ofMillis(8100))
            .setExecutionException(RM_EXCEPTION)
            .setHttpStatusCode(404)
            .build());

    sleepForSpansExport();

    // Assert cloud api count increase by 1
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // Assert error count increase by 1
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT_404, errorCount, 1);

    // This rely on the latency DistributionData defined in {@link MetricHelper} where 8s - 16s are
    // in the same bucket.
    // This would expect the latency falls into the 8s-16s bucket(26th).
    AggregationData.DistributionData data =
        (AggregationData.DistributionData)
            MetricsHelper.viewManager.getView(LATENCY_VIEW_NAME).getAggregationMap().get(API_COUNT);

    assertEquals(data.getBucketCounts().get(25).longValue(), 1);

    // Verify logger was invoked with an exception
    verify(mockLogger, times(1)).debug(anyString(), any(JsonObject.class), any(Exception.class));
  }
}
