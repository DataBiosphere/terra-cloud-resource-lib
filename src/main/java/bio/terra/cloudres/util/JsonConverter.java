package bio.terra.cloudres.util;

import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.gson.Gson;

/**
 * Util class to convert cloud resources to Json format
 *
 * <p> CRL want to log full request aid for debugging. Added this to help us better log things in a good format
 */
public class JsonConverter {
  /** Converts {@link ProjectInfo} to json */
  public static String convert(ProjectInfo projectinfo) {
    Gson gson = new Gson();
    return gson.toJson(projectinfo);
  }

  public static String convert(Project project) {
    Gson gson = new Gson();
    return gson.toJson(project, Project.class);
  }
}
