package bio.terra.cloudres.util;

import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Map;

/**
 * Util class to convert cloud resources to Json format
 *
 * <p>CRL want to log full request aid for debugging. Added this to help us better log things in a
 * good format
 */
public class JsonConverter {
  /** Converts a generic type to json */
  public static <R> String convert(R object) {
    if (object == null) {
      return null;
    }
    Gson gson = new Gson();
    return gson.toJson(object, object.getClass());
  }

  /** Converts {@link Project} to json */
  public static String convert(Project project) {
    Gson gson = new Gson();
    return gson.toJson(project, Project.class);
  }

  /** Converts map to format */
  public static String convert(ProjectInfo projectInfo) {
    Gson gson = new Gson();
    return gson.toJson(projectInfo);
  }

  /** Converts map to format */
  public static String convert(Map<String, String> map) {
    Gson gson = new Gson();
    return gson.toJson(map);
  }

  /** Merges a json format string with a map. */
  public static JsonObject appendFormattedString(JsonObject jsonObject, String key, String value) {
    Gson gson = new Gson();
    jsonObject.add(key, gson.fromJson(value, JsonObject.class));
    return jsonObject;
  }
}
