package bio.terra.cloudres.util;

import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.gson.Gson;

/** Util class to convert cloud resources to Json format */
public class JsonConverter {
  public static String convertGoogleProjectInfoToJson(ProjectInfo projectinfo) {
    Gson gson = new Gson();
    return gson.toJson(projectinfo);
  }

  public static String convertGoogleProjectToJson(Project project) {
    Gson gson = new Gson();
    return gson.toJson(project, Project.class);
  }
}
