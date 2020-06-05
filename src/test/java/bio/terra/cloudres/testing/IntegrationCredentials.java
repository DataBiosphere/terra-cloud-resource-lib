package bio.terra.cloudres.testing;

import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.FileInputStream;

/** Provides cloud credentials to use in integration tests. */
public class IntegrationCredentials {
  /** Path to the admin service account credentials file. */
  private static final String GOOGLE_SERVICE_ACCOUNT_ADMIN_PATH =
      "src/test/resources/integration_service_account_admin.json";

  /** Path to the admin service account credentials file. */
  private static final String GOOGLE_SERVICE_ACCOUNT_USER_PATH =
          "src/test/resources/integration_service_account_user.json";

  public static ServiceAccountCredentials getAdminGoogleCredentialsOrDie() {
    try {
      return ServiceAccountCredentials.fromStream(new FileInputStream(GOOGLE_SERVICE_ACCOUNT_ADMIN_PATH));
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to load GoogleCredentials from " + GOOGLE_SERVICE_ACCOUNT_ADMIN_PATH + "\n", e);
    }
  }

  public static ServiceAccountCredentials getRegularUserGoogleCredentialsOrDie() {
    try {
      return ServiceAccountCredentials.fromStream(new FileInputStream(GOOGLE_SERVICE_ACCOUNT_USER_PATH));
    } catch (Exception e) {
      throw new RuntimeException(
              "Unable to load GoogleCredentials from " + GOOGLE_SERVICE_ACCOUNT_USER_PATH + "\n", e);
    }
  }
}
