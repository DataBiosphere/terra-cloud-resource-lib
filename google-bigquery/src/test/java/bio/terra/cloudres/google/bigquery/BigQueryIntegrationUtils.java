package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import java.io.IOException;
import java.security.GeneralSecurityException;

/** Utilities for integration tests of the BigQuery package. */
public class BigQueryIntegrationUtils {
  static BigQueryCow defaultBigQueryCow() {
    ServiceAccountCredentials googleCredentials =
        IntegrationCredentials.getAdminGoogleCredentialsOrDie();
    try {
      return BigQueryCow.create(IntegrationUtils.DEFAULT_CLIENT_CONFIG, googleCredentials);
    } catch (GeneralSecurityException | IOException e) {
      throw new RuntimeException("Failure creating BigQueryCow", e);
    }
  }


}
