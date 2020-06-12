package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

@Tag("integration")
public class TabletCowTest {
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();

  private static BigQueryCow bigQueryCow = defaultBigQueryCow();

  private static DatasetInfo reusableDataset;

  // Cleanup the createdTableIds list and use this to track tables created in one test method.
  // In this way to can make sure tables can always be cleaned up even test failed in the middle.
  private List<TableId> createdTableIds = new ArrayList<>();

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
    createdTableIds.forEach(bigQueryCow::delete);
  }

  @Test
  public void reload() {
    TableCow tableCow =
        createTableCow(bigQueryCow, reusableDataset.getDatasetId().getDataset(), createdTableIds);
    TableCow reloadedTableCow = tableCow.reload();

    assertTableIdEqual(
        tableCow.getTableInfo().getTableId(), reloadedTableCow.getTableInfo().getTableId());
  }

  @Test
  public void update() {
    String description = "des";
    TableCow tableCow =
        createTableCow(bigQueryCow, reusableDataset.getDatasetId().getDataset(), createdTableIds);

    TableCow updatedTableCow =
        new TableCow(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            (Table) tableCow.getTableInfo().toBuilder().setDescription(description).build());
    updatedTableCow.update();
    assertEquals(description, updatedTableCow.reload().getTableInfo().getDescription());
  }

  @Test
  public void exists() {
    TableCow tableCow =
        createTableCow(bigQueryCow, reusableDataset.getDatasetId().getDataset(), createdTableIds);

    assertTrue(tableCow.exists());
  }

  @Test
  public void delete() {
    TableCow tableCow =
        createTableCow(bigQueryCow, reusableDataset.getDatasetId().getDataset(), createdTableIds);
    tableCow.delete();

    assertNull(bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo());
  }
}
