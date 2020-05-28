package bio.terra.cloudres.common;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.FileInputStream;

/** Provides cloud credentials to use in integration tests. */
public class IntegrationCredentials {
  /** Path to the service account credentials file. */
  private static final String GOOGLE_SERVICE_ACCOUNT_PATH =
      "src/test/resources/integration_service_account.json";

  public static GoogleCredentials getGoogleCredentialsOrDie() {
    try {
      return ServiceAccountCredentials.fromStream(new FileInputStream(GOOGLE_SERVICE_ACCOUNT_PATH))
          .createScoped("https://www.googleapis.com/auth/cloud-platform");
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to load GoogleCredentials from " + GOOGLE_SERVICE_ACCOUNT_PATH + "\n", e);
    }
  }
}
