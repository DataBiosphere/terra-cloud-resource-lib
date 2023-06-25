package bio.terra.cloudres.google.dataproc;

import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience class for formatting the project/region/clusterName of a Dataproc cluster for {@link
 * com.google.api.services.dataproc.Dataproc.Projects.Regions.Clusters}
 */
@AutoValue
public abstract class ClusterName {
  private static final Pattern NAME_PATTERN =
      Pattern.compile("^projects/([^/]+)/locations/([^/]+)/clusters/([^/]+)$");

  /** The Google project id that contains this cluster. */
  public abstract String projectId();
  /** The region where the cluster is, e.g. "us-west1". */
  public abstract String region();
  /** The user specified id for the cluster. */
  public abstract String name();

  /**
   * Returns a String for the name of a cluster with this parent in the format expected by {@link
   * com.google.api.services.dataproc.Dataproc.Projects.Regions.Clusters} functions.
   */
  public String formatName() {
    return String.format("projects/%s/regions/%s/clusters/%s", projectId(), region(), name());
  }

  /**
   * Parse the name format to create an {@link ClusterName}.
   *
   * @throws IllegalArgumentException on parse failure.
   * @see #formatName()
   */
  public static ClusterName fromNameFormat(String name) {
    Matcher matcher = NAME_PATTERN.matcher(name);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format("Name must conform to %s but got '%s'", NAME_PATTERN.pattern(), name));
    }
    return ClusterName.builder()
        .projectId(matcher.group(1))
        .region(matcher.group(2))
        .name(matcher.group(3))
        .build();
  }

  public static Builder builder() {
    return new AutoValue_ClusterName.Builder();
  }

  /** Adds properties to the JsonObject for the fields on this. */
  void addProperties(JsonObject jsonObject) {
    jsonObject.addProperty("projectId", projectId());
    jsonObject.addProperty("region", region());
    jsonObject.addProperty("clusterName", name());
  }

  /** Builder for {@link ClusterName}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder projectId(String projectId);

    public abstract Builder region(String location);

    public abstract Builder name(String clusterName);

    public abstract ClusterName build();
  }
}
