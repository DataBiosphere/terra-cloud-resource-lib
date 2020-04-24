package bio.terra.cloudres.util;

import java.io.Serializable;

public class Project implements Serializable {
  private String projectName;
  private String cromwellAuthBucketUrl;

  public String getProjectName() { return this.projectName; }
}
