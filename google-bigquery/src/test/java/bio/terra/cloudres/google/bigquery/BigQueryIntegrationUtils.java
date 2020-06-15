package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQueryOptions;

/** Utilities for integration tests of the BigQuery package. */
public class BigQueryIntegrationUtils {
  static BigQueryCow defaultBigQueryCow() {
    ServiceAccountCredentials googleCredentials =
        IntegrationCredentials.getAdminGoogleCredentialsOrDie();
    return new BigQueryCow(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        BigQueryOptions.newBuilder()
            .setCredentials(googleCredentials)
            .setProjectId(googleCredentials.getProjectId())
            .build());
  }
}
