package bio.terra.cloudres.google.iam;

import com.google.auto.value.AutoValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convenience class for formatting the project and email of a service account for {@link
 * com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts}.
 *
 * <p>Note that a Service Account name can also be specified with the "unique_id" instead of the
 * email. This class only aims to support the email usage.
 */
@AutoValue
public abstract class ServiceAccountName {
  private static final Pattern NAME_PATTERN =
      Pattern.compile("^projects/([^/]+)/serviceAccounts/([^/]+)$");

  /** The project id of the Google Project the service account is in. */
  public abstract String projectId();

  /** The email of the service account. */
  public abstract String email();

  /**
   * Returns a String for the name of an instance with this parent in the format expected by {@link
   * com.google.api.services.iam.v1.Iam.Projects.ServiceAccounts} functions, i.e.
   * `projects/{PROJECT_ID}/serviceAccounts/{EMAIL_ADDRESS}`
   */
  public String formatName() {
    return String.format("projects/%s/serviceAccounts/%s", projectId(), email());
  }

  public static Builder builder() {
    return new AutoValue_ServiceAccountName.Builder();
  }

  /**
   * Parse the name format to create a {@link ServiceAccountName}.
   *
   * @throws IllegalArgumentException on parse failure.
   * @see #formatName()
   */
  public static ServiceAccountName fromNameFormat(String name) {
    Matcher matcher = NAME_PATTERN.matcher(name);
    if (!matcher.matches()) {
      throw new IllegalArgumentException(
          String.format("Name must conform to %s but got '%s'", NAME_PATTERN.pattern(), name));
    }
    return ServiceAccountName.builder().projectId(matcher.group(1)).email(matcher.group(2)).build();
  }

  /**
   * Returns the service account email based on the project id and the service account id. The
   * service account id is the "username" of the service account email.
   */
  public static String emailFromAccountId(String accountId, String projectId) {
    return String.format("%s@%s.iam.gserviceaccount.com", accountId, projectId);
  }

  /** Builder for {@link ServiceAccountName}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder projectId(String projectId);

    public abstract Builder email(String email);

    public abstract ServiceAccountName build();
  }
}
