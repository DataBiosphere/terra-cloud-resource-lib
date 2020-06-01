package bio.terra.cloudres.common;

import static bio.terra.cloudres.testing.MetricsTestUtil.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.resourcemanager.ResourceManagerException;
import io.opencensus.stats.AggregationData;
import io.opencensus.trace.TraceId;
import java.util.Optional;
import java.util.function.Supplier;
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
  private static final String TRACE_ID = "1234567890123456";
  private static final String ERROR_MESSAGE = "error!";
  private static final String FORMATTED_EXCEPTION =
      "\"exception:\":{\"message\":\"error!\",\"errorCode\":\"404\"},";
  private static final String EXPECTED_LOG_PREFIX =
      "{\"traceId:\":\"TraceId{traceId=31323334353637383930313233343536}\",\"operation:\":\"GOOGLE_CREATE_PROJECT\",\"clientName:\":\"TestClient\",";

  /** use the {@link ResourceManagerException} as an example of a BaseHttpServiceException. */
  private static final ResourceManagerException RM_EXCEPTION =
      new ResourceManagerException(404, ERROR_MESSAGE);

  private static final Supplier FAILED_COW_EXECUTE_SUPPLIER =
      () -> {
        throw RM_EXCEPTION;
      };

  private static final Supplier SUCCESS_COW_EXECUTE_SUPPLIER =
      () -> {
        try {
          Thread.sleep(4100);
          return null;
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      };

  ArgumentCaptor<String> logArgument = ArgumentCaptor.forClass(String.class);

  private Logger logger = LoggerFactory.getLogger(OperationAnnotatorTest.class);

  @Mock private Logger mockLogger = mock(Logger.class);

  private ClientConfig clientConfig = ClientConfig.Builder.newBuilder().setClient(CLIENT).build();
  private OperationAnnotator operationAnnotator = new OperationAnnotator(clientConfig, logger);
  private TestCowOperation cowOperation = new TestCowOperation(SUCCESS_COW_EXECUTE_SUPPLIER);

  @Test
  public void testExecuteGoogleCloudCall_success() throws Exception {
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    operationAnnotator.executeCowOperation(cowOperation);

    sleepForSpansExport();

    // One cloud api count
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // No error
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT, errorCount, 0);

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
    long errorCount = getCurrentCount(ERROR_VIEW_NAME, ERROR_COUNT);
    long apiCount = getCurrentCount(API_VIEW_NAME, API_COUNT);

    cowOperation = new TestCowOperation(FAILED_COW_EXECUTE_SUPPLIER);

    Assert.assertThrows(
        ResourceManagerException.class, () -> operationAnnotator.executeCowOperation(cowOperation));

    sleepForSpansExport();

    // Assert cloud api count increase by 1
    assertCountIncremented(API_VIEW_NAME, API_COUNT, apiCount, 1);

    // Assert error count increase by 1
    assertCountIncremented(ERROR_VIEW_NAME, ERROR_COUNT, errorCount, 1);
  }

  /**
   * Expected log in JSON format with no error code
   *
   * <pre>{@code
   * { "traceId:":"TraceId{traceId=31323334353637383930313233343536}",
   *   "operation:":"GOOGLE_CREATE_PROJECT",
   *   "clientName:":"test_client",
   *   "request:":{
   *      "requestName":"request1"
   *   },
   *   "response:":{
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
        CloudOperation.GOOGLE_CREATE_PROJECT,
        PROJECT_INFO_STRING,
        Optional.empty());

    // Expected result in Json format
    verify(mockLogger).debug(logArgument.capture());
    assertEquals(
        EXPECTED_LOG_PREFIX + "\"request:\":" + PROJECT_INFO_STRING + "}", logArgument.getValue());
  }

  /**
   * Expected log in JSON format with empty response:
   *
   * <pre>{@code
   * { "traceId:":"TraceId{traceId=31323334353637383930313233343536}",
   *   "operation:":"GOOGLE_CREATE_PROJECT",
   *   "clientName:":"test_client",
   *   "errorCode:":"404",
   *   "request:":{
   *      "requestName":"request1"
   *   },
   *   "response:":null
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
        CloudOperation.GOOGLE_CREATE_PROJECT,
        PROJECT_INFO_STRING,
        Optional.of(RM_EXCEPTION));

    // Expected result in Json format
    verify(mockLogger).debug(logArgument.capture());
    assertEquals(
        EXPECTED_LOG_PREFIX + FORMATTED_EXCEPTION + "\"request:\":" + PROJECT_INFO_STRING + "}",
        logArgument.getValue());
  }

  @Test
  public void testLogEvent_disableDebug() throws Exception {
    operationAnnotator = new OperationAnnotator(clientConfig, mockLogger);
    when(mockLogger.isDebugEnabled()).thenReturn(false);

    operationAnnotator.logEvent(
        TraceId.fromBytes(TRACE_ID.getBytes()),
        CloudOperation.GOOGLE_CREATE_PROJECT,
        PROJECT_INFO_STRING,
        Optional.of(RM_EXCEPTION));
    // Expected result in Json format
    verify(mockLogger, never()).debug(anyString());
  }

  private static class TestCowOperation<R> implements CowOperation<R> {
    private Supplier<R> executeFunction;

    TestCowOperation(Supplier<R> executeFunction) {
      this.executeFunction = executeFunction;
    }

    @Override
    public CloudOperation getCloudOperation() {
      return CloudOperation.GOOGLE_CREATE_PROJECT;
    }

    @Override
    public R execute() {
      return executeFunction.get();
    }

    @Override
    public String serializeRequest() {
      return PROJECT_INFO_STRING;
    }
  }
}
