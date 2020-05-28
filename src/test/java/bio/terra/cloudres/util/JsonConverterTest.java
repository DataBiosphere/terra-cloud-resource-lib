package bio.terra.cloudres.util;

import static bio.terra.cloudres.util.JsonConverter.convert;
import static org.junit.Assert.assertEquals;

import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import java.util.Map;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Test for {@link JsonConverter} */
@Tag("unit")
public class JsonConverterTest {
  private static final String PROJECT_ID = "project-id";
  private static final String PROJECT_NAME = "myProj";
  private static final Map<String, String> PROJECT_LABELS = ImmutableMap.of("k1", "v1", "k2", "v2");
  private static final ProjectInfo PROJECT_INFO =
      ProjectInfo.newBuilder(PROJECT_ID).setName(PROJECT_NAME).setLabels(PROJECT_LABELS).build();
  private static final Map<String, String> JSON_MAP = ImmutableMap.of("name1", "value1", "name2" , "value2");

  /**
   * Expected result in JSON format for projectInfo:
   *
   * <pre>{@code
   * { "name": "PROJECT_NAME",
   *   "projectId": "PROJECT_ID",
   *   "labels": "{key1 : value1, key2: value2}"
   * }
   * }</pre>
   */
  @Test
  public void testConvertMap() throws Exception {
    // Expected result in Json format
    assertEquals(
        "{\"name1\":\"value1\",\"name2\":\"value2\"}",
        JsonConverter.convert(JSON_MAP));
  }

  /**
   * Expected result in JSON format for projectInfo:
   *
   * <pre>{@code
   * { "name": "PROJECT_NAME",
   *   "projectId": "PROJECT_ID",
   *   "labels": "{key1 : value1, key2: value2}"
   * }
   * }</pre>
   */
  @Test
  public void testConvertGoogleProjectInfo() throws Exception {
    // Expected result in Json format
    assertEquals(
            "{\"name\":\"myProj\",\"projectId\":\"project-id\",\"labels\":{\"k1\":\"v1\",\"k2\":\"v2\"}}",
            JsonConverter.convert(PROJECT_INFO));
  }

  /**
   * Expected result in JSON format for project:
   *
   * <pre>{@code
   * { "name": "PROJECT_NAME",
   *   "projectId": "PROJECT_ID",
   *   "labels": "{key1 : value1, key2:value2}"
   * }
   * }</pre>
   *
   * <p>There is no public constructor for project, so the test creates Project from Json first then
   * convert it back
   */
  @Test
  public void testConvertGoogleProject() throws Exception {
    Gson gson = new Gson();
    String expectedJson =
        "{\"name\":\"myProj\",\"projectId\":\"project-id\",\"labels\":{\"k1\":\"v1\",\"k2\":\"v2\"}}";

    Project project = gson.fromJson(expectedJson, Project.class);
    // Verify the converted project is correct
    assertEquals(PROJECT_ID, project.getProjectId());
    assertEquals(PROJECT_NAME, project.getName());
    assertEquals(PROJECT_LABELS, project.getLabels());

    // Verify project cast to json
    assertEquals(expectedJson, convert(project));
  }
}
