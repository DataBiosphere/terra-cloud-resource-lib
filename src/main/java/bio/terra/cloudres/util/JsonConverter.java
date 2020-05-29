package bio.terra.cloudres.util;

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

  /**
   * Converts {@link ProjectInfo} to Json formatted String
   *
   * @param projectInfo: the projectInfo to convert
   * @return the formatted Json in String
   */
  public static String convert(ProjectInfo projectInfo) {
    Gson gson = new Gson();
    return gson.toJson(projectInfo, ProjectInfo.class);
  }

  /**
   * Converts map to format
   *
   * @param map the Map to convert.
   * @return the formatted Json in String
   */
  public static String convert(Map<String, String> map) {
    Gson gson = new Gson();
    return gson.toJson(map);
  }

  /**
   * Appends a key-value pair into a {@link JsonObject}
   *
   * @param jsonObject the existing jsonObject
   * @param key the key of new value
   * @param value the value to append
   * @return The new {@link JsonObject} after append
   */
  public static JsonObject appendFormattedString(JsonObject jsonObject, String key, String value) {
    Gson gson = new Gson();
    jsonObject.add(key, gson.fromJson(value, JsonObject.class));
    return jsonObject;
  }
}
