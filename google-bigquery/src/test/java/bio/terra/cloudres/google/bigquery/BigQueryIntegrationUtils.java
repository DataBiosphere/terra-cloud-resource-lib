package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.TableId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

  static void assertTableIdEqual(TableId expect, TableId actual) {
    assertEquals(expect.getDataset(), actual.getDataset());
    assertEquals(expect.getTable(), actual.getTable());
  }

  static void assertTableIdContainsExactlyInCowPage(List<TableId> expect, Page<TableCow> actual) {
    List<TableId> actualTableIds = new ArrayList<>();
    actual
        .iterateAll()
        .forEach(tableCow -> actualTableIds.add(tableCow.getTableInfo().getTableId()));
    assertEquals(expect.size(), actualTableIds.size());
    assertTrue(
        expect.stream()
            .map(TableId::getTable)
            .collect(Collectors.toList())
            .containsAll(
                actualTableIds.stream().map(TableId::getTable).collect(Collectors.toList())));
    assertTrue(
        expect.stream()
            .map(TableId::getDataset)
            .collect(Collectors.toList())
            .containsAll(
                actualTableIds.stream().map(TableId::getDataset).collect(Collectors.toList())));
  }
}
