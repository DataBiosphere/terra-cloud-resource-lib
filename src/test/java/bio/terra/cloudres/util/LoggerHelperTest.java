package bio.terra.cloudres.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.common.CloudOperation;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.common.collect.ImmutableMap;
import io.opencensus.trace.TraceId;
import java.util.Map;
import java.util.OptionalInt;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;

/** Test for {@link LoggerHelper} */
@Tag("unit")
public class LoggerHelperTest {
  private static final String CLIENT_NAME = "test_client";
  private static final String PROJECT_ID = "project-id";
  private static final String PROJECT_NAME = "myProj";
      "{\"name\":\"myProj\",\"projectId\":\"project-id\",\"labels\":{\"k1\":\"v1\",\"k2\":\"v2\"}}";
  private static final String TRACE_ID = "1234567890123456";
  private static final Map<String, String> PROJECT_LABELS = ImmutableMap.of("k1", "v1", "k2", "v2");
  private static final ProjectInfo PROJECT_INFO =
      ProjectInfo.newBuilder(PROJECT_ID).setName(PROJECT_NAME).setLabels(PROJECT_LABELS).build();
  private static final Map<String, String> JSON_MAP =
      ImmutableMap.of("name1", "value1", "name2", "value2");

  private static final String EXPECTED_LOG_PREFIX =
      "{\"traceId:\":\"TraceId{traceId=31323334353637383930313233343536}\",\"operation:\":\"GOOGLE_CREATE_PROJECT\",\"clientName:\":\"test_client\",";

  @Mock private Logger logger = mock(Logger.class);

  ArgumentCaptor<String> logArgument = ArgumentCaptor.forClass(String.class);

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
    when(logger.isDebugEnabled()).thenReturn(true);

    LoggerHelper.logEvent(
        logger,
        TraceId.fromBytes(TRACE_ID.getBytes()),

        OptionalInt.empty());

    // Expected result in Json format
    verify(logger).debug(logArgument.capture());
    assertEquals(
        EXPECTED_LOG_PREFIX
            + "\"request:\":"
            + PROJECT_INFO_STRING
            + ","
            + "\"response:\":"
        logArgument.getValue());
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
    when(logger.isDebugEnabled()).thenReturn(true);

    LoggerHelper.logEvent(
        logger,
        TraceId.fromBytes(TRACE_ID.getBytes()),

        null,
        OptionalInt.of(404));

    // Expected result in Json format
    verify(logger).debug(logArgument.capture());
    assertEquals(
        EXPECTED_LOG_PREFIX
T_INFO_STRING
            + ",\"response:\":null}",
        logArgument.getValue());
  }

  @Test
  public void testLogEvent_disableDebug() throws Exception {
    when(logger.isDebugEnabled()).thenReturn(false);

    LoggerHelper.logEvent(
        logger,
        TraceId.fromBytes(TRACE_ID.getBytes()),
        CloudOperation.GOOGLE_CREATE_PROJECT,
        CLIENT_NAME,
        null,
        OptionalInt.of(404));
    // Expected result in Json format
    verify(logger, never()).debug(anyString());
  }
}
