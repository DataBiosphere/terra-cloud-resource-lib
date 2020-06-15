package bio.terra.cloudres.testing;

import com.google.auth.oauth2.ServiceAccountCredentials;

/** Provides cloud credentials to use in integration tests. */
public class IntegrationCredentials {
  /**
   * Path to the admin service account credentials file.
   *
   * <p>he admin service account has the roles needed to operate the CRL APIs in the integration
   * test project, e.g. create and delete resources
   */
  private static final String GOOGLE_SERVICE_ACCOUNT_ADMIN_PATH =
      "integration_service_account_admin.json";

  /**
   * Path to the regular user service account credentials file.
   *
   * <p>The user service account doesn't have any permissions, but should be used as a test
   * non-admin user to reference
   */
  private static final String GOOGLE_SERVICE_ACCOUNT_USER_PATH =
      "integration_service_account_user.json";

  public static ServiceAccountCredentials getAdminGoogleCredentialsOrDie() {
    return getGoogleCredentialsOrDie(GOOGLE_SERVICE_ACCOUNT_ADMIN_PATH);
  }

  public static ServiceAccountCredentials getUserGoogleCredentialsOrDie() {
    return getGoogleCredentialsOrDie(GOOGLE_SERVICE_ACCOUNT_USER_PATH);
  }

  private static ServiceAccountCredentials getGoogleCredentialsOrDie(String serviceAccountPath) {
    try {
      return ServiceAccountCredentials.fromStream(
          Thread.currentThread().getContextClassLoader().getResourceAsStream(serviceAccountPath));
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to load GoogleCredentials from " + serviceAccountPath + "\n", e);
    }
  }
}
