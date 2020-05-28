package bio.terra.cloudres.util;

import com.google.cloud.resourcemanager.Project;
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
  /** Converts {@link Project} to json */
  public static String convert(Project project) {
    Gson gson = new Gson();
    return gson.toJson(project, Project.class);
  }

  /** Converts map to format */
  public static String convert(Map<String, String> map) {
    Gson gson = new Gson();
    return gson.toJson(map);
  }

  /** Merges a json format string with a map. */
  public static String merge(String jsonString, Map<String, String> map) {
    Gson gson = new Gson();
    JsonObject jsonObject = gson.fromJson(jsonString, JsonObject.class);
    map.forEach((k, v) -> jsonObject.add(k, gson.toJsonTree(v)));
    return jsonObject.toString();
  }
}
