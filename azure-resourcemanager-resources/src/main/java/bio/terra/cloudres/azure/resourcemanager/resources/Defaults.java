package bio.terra.cloudres.azure.resourcemanager.resources;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;

public class Defaults {
  private Defaults() {}

  public static HttpLogOptions logOptions() {
    return new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC).setPrettyPrintBody(true);
  }
}
