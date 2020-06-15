package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import org.junit.jupiter.api.*;

@Tag("integration")
public class TabletCowTest {
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();
  private static BigQueryCow bigQueryCow = defaultBigQueryCow();
  private static DatasetInfo reusableDataset;
  private final BigQueryResourceTracker bigQueryResourceTracker =
      new BigQueryResourceTracker(bigQueryCow, REUSABLE_DATASET_ID);

  @BeforeAll
  public static void createReusableDataset() {
    reusableDataset =
        bigQueryCow.create(DatasetInfo.newBuilder(REUSABLE_DATASET_ID).build()).getDatasetInfo();
  }

  @AfterAll
  public static void deleteReusableDataset() {
    bigQueryCow.delete(REUSABLE_DATASET_ID);
  }

  @AfterEach
  public void tearDown() {
    bigQueryResourceTracker.tearDown();
  }

  @Test
  public void reload() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();
    TableCow reloadedTableCow = tableCow.reload();

    assertTableIdEqual(
        tableCow.getTableInfo().getTableId(), reloadedTableCow.getTableInfo().getTableId());
  }

  @Test
  public void update() {
    String description = "des";
    TableCow tableCow = bigQueryResourceTracker.createTableCow();

    TableCow updatedTableCow =
        new TableCow(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            (Table) tableCow.getTableInfo().toBuilder().setDescription(description).build());
    updatedTableCow.update();
    assertEquals(description, updatedTableCow.reload().getTableInfo().getDescription());
  }

  @Test
  public void exists() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();

    assertTrue(tableCow.exists());
  }

  @Test
  public void delete() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();
    tableCow.delete();

    assertNull(bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo());
  }
}
