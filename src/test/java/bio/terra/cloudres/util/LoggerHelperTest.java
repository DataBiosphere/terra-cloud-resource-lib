package bio.terra.cloudres.util;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import bio.terra.cloudres.common.CloudOperation;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.OptionalInt;
import org.junit.Before;
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
  private static final Map<String, String> PROJECT_LABELS = ImmutableMap.of("k1", "v1", "k2", "v2");
  private static final ProjectInfo PROJECT_INFO =
      ProjectInfo.newBuilder(PROJECT_ID).setName(PROJECT_NAME).setLabels(PROJECT_LABELS).build();
  private static final Map<String, String> JSON_MAP =
      ImmutableMap.of("name1", "value1", "name2", "value2");

  @Mock private Logger logger = mock(Logger.class);

  @Before
  public void setUp() throws Exception {}

  ArgumentCaptor<String> logArgument = ArgumentCaptor.forClass(String.class);

  /**
   * Expected result in JSON format for project:
   *
   * <pre>{@code
   * { "name": "PROJECT_NAME",
   *   "projectId": "PROJECT_ID",
   *   "labels": "{key1 : value1, key2:value2}"
   *   "operation: ":"GOOGLE_CREATE_PROJECT",
   *   "clientName: ":"test_client",
   *   "isSuccess: ":"true"
   * }
   * }</pre>
   *
   * <p>There is no public constructor for project, so the test creates Project from Json first then
   * convert it back
   */
  @Test
  public void testLogSuccessEvent() throws Exception {
    when(logger.isDebugEnabled()).thenReturn(true);

    // The project
    String expectedResourceJson =
        "{\"name\":\"myProj\",\"projectId\":\"project-id\",\"labels\":{\"k1\":\"v1\",\"k2\":\"v2\"}}";

    String expectedResult =
        "{\"name\":\"myProj\",\"projectId\":\"project-id\",\"labels\":{\"k1\":\"v1\",\"k2\":\"v2\"},\"operation: \":\"GOOGLE_CREATE_PROJECT\",\"clientName: \":\"test_client\",\"isSuccess: \":\"true\"}";

    LoggerHelper.logSuccessEvent(
        logger, CloudOperation.GOOGLE_CREATE_PROJECT, CLIENT_NAME, expectedResourceJson);
    // Expected result in Json format
    verify(logger).debug(logArgument.capture());
    assertEquals(expectedResult, logArgument.getValue());
  }

  @Test
  public void testLogSuccessEvent_disableDebug() throws Exception {
    when(logger.isDebugEnabled()).thenReturn(false);

    LoggerHelper.logSuccessEvent(logger, CloudOperation.GOOGLE_CREATE_PROJECT, CLIENT_NAME, "11");
    // Expected result in Json format
    verify(logger, never()).debug(anyString());
  }

  @Test
  public void testLogFailedEvent() throws Exception {
    LoggerHelper.logFailEvent(
        logger, CloudOperation.GOOGLE_CREATE_PROJECT, CLIENT_NAME, OptionalInt.of(404));
    String expectedResult =
        "{\"operation: \":\"GOOGLE_CREATE_PROJECT\",\"clientName: \":\"test_client\",\"errorCode: \":\"404\",\"isSuccess: \":\"false\"}";
    // Expected result in Json format
    verify(logger).info(logArgument.capture());
    assertEquals(expectedResult, logArgument.getValue());
  }
}
