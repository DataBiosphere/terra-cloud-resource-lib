package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.*;

@Tag("integration")
/**
 * Note that Dataset has quota about "5 operations every 10 seconds per dataset". So in some update,
 * delete test, need to create a new dataset every time.
 *
 * @see <a
 *     href="https://cloud.google.com/bigquery/quotas#dataset_limits">cloud.google.com/bigquery/quotas</a>
 */
public class BigQueryCowTest {
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
  public void createDataset() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();

    assertEquals(
        datasetCow.getDatasetInfo(),
        bigQueryCow
            .getDataSet(datasetCow.getDatasetInfo().getDatasetId().getDataset())
            .getDatasetInfo());
  }

  @Test
  public void getDataset() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertEquals(
        datasetId, bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDatasetId().getDataset());
  }

  @Test
  public void updateDataset() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNull(bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDescription());

    String description = "new description";
    bigQueryCow.update(DatasetInfo.newBuilder(datasetId).setDescription("new description").build());

    assertEquals(description, bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDescription());
  }

  @Test
  public void deleteDataset() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    bigQueryCow.delete(datasetId);
    assertNull(bigQueryCow.getDataSet(datasetId).getDatasetInfo());
  }

  @Test
  public void createTable() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();

    assertTableIdEqual(
        tableCow.getTableInfo().getTableId(),
        bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo().getTableId());
  }

  @Test
  public void updateTable() {
    String description = "des";
    TableCow tableCow = bigQueryResourceTracker.createTableCow();
    bigQueryCow.update(tableCow.getTableInfo().toBuilder().setDescription(description).build());

    assertEquals(
        description,
        bigQueryCow
            .update(tableCow.getTableInfo().toBuilder().setDescription(description).build())
            .getTableInfo()
            .getDescription());
  }

  @Test
  public void deleteTable() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();
    TableId tableId = tableCow.getTableInfo().getTableId();
    assertNotNull(bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo());

    bigQueryCow.delete(tableId);
    assertNull(bigQueryCow.getTable(tableId).getTableInfo());
  }

  @Test
  public void getTable() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();
    TableId tableId = tableCow.getTableInfo().getTableId();

    assertTableIdEqual(tableId, bigQueryCow.getTable(tableId).getTableInfo().getTableId());
  }

  @Test
  public void getTableWithDatasetId() {
    TableCow tableCow = bigQueryResourceTracker.createTableCow();
    TableId tableId = tableCow.getTableInfo().getTableId();

    assertTableIdEqual(
        tableId,
        bigQueryCow
            .getTable(reusableDataset.getDatasetId().getDataset(), tableId.getTable())
            .getTableInfo()
            .getTableId());
  }

  @Test
  public void listTables() {
    TableCow tableCow1 = bigQueryResourceTracker.createTableCow();
    TableId tableId1 = tableCow1.getTableInfo().getTableId();
    TableCow tableCow2 = bigQueryResourceTracker.createTableCow();
    TableId tableId2 = tableCow2.getTableInfo().getTableId();

    assertTableIdContainsExactlyInCowPage(
        ImmutableList.of(tableId1, tableId2),
        bigQueryCow.listTables(reusableDataset.getDatasetId()));
    assertTableIdContainsExactlyInCowPage(
        ImmutableList.of(tableId1, tableId2),
        bigQueryCow.listTables(reusableDataset.getDatasetId().getDataset()));
  }
}
