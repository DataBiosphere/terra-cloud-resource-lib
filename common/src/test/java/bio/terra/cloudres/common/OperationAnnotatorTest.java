package bio.terra.cloudres.common;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.testing.StubCloudOperation;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.ResourceManagerException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.opencensus.stats.AggregationData;
import io.opencensus.trace.TraceId;
import java.util.Optional;
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
  private static final String PROJECT_INFO_STRING =
      "{\"name\":\"myProj\",\"projectId\":\"project-id\",\"labels\":{\"k1\":\"v1\",\"k2\":\"v2\"}}";
  private static final JsonObject PROJECT_INFO_JSON_OBJECT =
      new Gson().fromJson(PROJECT_INFO_STRING, JsonObject.class);
  private static final String TRACE_ID = "1234567890123456";
  private static final String ERROR_MESSAGE = "error!";
  private static final String FORMATTED_EXCEPTION =
      "\"exception\":{\"message\":\"error!\",\"errorCode\":\"404\"},";
  private static final String EXPECTED_LOG_PREFIX =
      "{\"traceId\":\"TraceId{traceId=31323334353637383930313233343536}\",\"operation\":\"GOOGLE_CREATE_PROJECT\",\"clientName\":\"TestClient\",";

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
        return PROJECT_INFO_JSON_OBJECT;
      };

  ArgumentCaptor<String> logArgument = ArgumentCaptor.forClass(String.class);

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
    Assert.assertThrows(
        InterruptedException.class,
        () ->
            operationAnnotator.executeCheckedCowOperation(
                StubCloudOperation.TEST_OPERATION,
                () -> {
                  throw new InterruptedException(ERROR_MESSAGE);
                },
                SERIALIZE));
  }

  /**
   * Expected log in JSON format with no error code
   *
   * <pre>{@code
   * { "traceId":"TraceId{traceId=31323334353637383930313233343536}",
   *   "operation":"GOOGLE_CREATE_PROJECT",
   *   "clientName":"test_client",
   *   "request":{
   *      "requestName":"request1"
   *   },
   *   "response":{
   *       "name":"myProj",
   *       "projectId":"project-id",
   *       "labels":{
   *          "k1":"v1",
   *          "k2":"v2"
   *       }
   *    }
   * }
   * }</pre>
   *
   * <p>There is no public constructor for project, so the test creates Project from Json first then
   * convert it back
   */
  @Test
  public void testLogEvent_nullError() throws Exception {
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);
    when(mockLogger.isDebugEnabled()).thenReturn(true);

    operationAnnotator.logEvent(
        TraceId.fromBytes(TRACE_ID.getBytes()),
        StubCloudOperation.TEST_OPERATION,
        PROJECT_INFO_JSON_OBJECT,
        Optional.empty());

    // Expected result in Json format
    verify(mockLogger).debug(logArgument.capture());
    assertEquals(
        EXPECTED_LOG_PREFIX + "\"request\":" + PROJECT_INFO_STRING + "}", logArgument.getValue());
  }

  /**
   * Expected log in JSON format with empty response:
   *
   * <pre>{@code
   * { "traceId":"TraceId{traceId=31323334353637383930313233343536}",
   *   "operation":"GOOGLE_CREATE_PROJECT",
   *   "clientName":"test_client",
   *   "errorCode":"404",
   *   "request":{
   *      "requestName":"request1"
   *   },
   *   "response":null
   * }
   * }</pre>
   *
   * <p>There is no public constructor for project, so the test creates Project from Json first then
   * convert it back
   */
  @Test
  public void testLogEvent_nullResponse() throws Exception {
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);
    when(mockLogger.isDebugEnabled()).thenReturn(true);

    operationAnnotator.logEvent(
        TraceId.fromBytes(TRACE_ID.getBytes()),
        StubCloudOperation.TEST_OPERATION,
        PROJECT_INFO_JSON_OBJECT,
        Optional.of(RM_EXCEPTION));

    // Expected result in Json format
    verify(mockLogger).debug(logArgument.capture());
    assertEquals(
        EXPECTED_LOG_PREFIX + FORMATTED_EXCEPTION + "\"request\":" + PROJECT_INFO_STRING + "}",
        logArgument.getValue());
  }

  @Test
  public void testLogEvent_disableDebug() throws Exception {
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);
    when(mockLogger.isDebugEnabled()).thenReturn(false);

    operationAnnotator.logEvent(
        TraceId.fromBytes(TRACE_ID.getBytes()),
        StubCloudOperation.TEST_OPERATION,
        PROJECT_INFO_JSON_OBJECT,
        Optional.of(RM_EXCEPTION));

    // no expected result in this case
    verify(mockLogger, never()).debug(anyString());
  }
}
