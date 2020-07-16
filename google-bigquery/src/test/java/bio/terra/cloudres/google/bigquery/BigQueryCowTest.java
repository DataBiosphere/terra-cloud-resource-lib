package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.cloudres.resources.CloudResourceUid;
import bio.terra.cloudres.resources.GoogleBigQueryDatasetUid;
import bio.terra.cloudres.resources.GoogleBigQueryTableUid;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Iterator;
import java.util.List;
import org.hamcrest.Matchers;
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
  private final ResourceTracker resourceTracker =
      new ResourceTracker(bigQueryCow, REUSABLE_DATASET_ID);

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
    resourceTracker.tearDown();
  }

  @Test
  public void createDataset() {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    DatasetCow datasetCow = resourceTracker.createDatasetCow();

    assertEquals(
        datasetCow.getDatasetInfo(),
        bigQueryCow
            .getDataSet(datasetCow.getDatasetInfo().getDatasetId().getDataset())
            .getDatasetInfo());
    DatasetId datasetId = datasetCow.getDatasetInfo().getDatasetId();
    assertThat(
        record,
        Matchers.contains(
            new CloudResourceUid()
                .googleBigQueryDatasetUid(
                    new GoogleBigQueryDatasetUid()
                        .projectId(datasetId.getProject())
                        .datasetId(datasetId.getDataset()))));
  }

  @Test
  public void getDataset() {
    DatasetCow datasetCow = resourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertEquals(
        datasetId, bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDatasetId().getDataset());
  }

  @Test
  public void updateDataset() {
    DatasetCow datasetCow = resourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNull(bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDescription());

    String description = "new description";
    bigQueryCow.update(DatasetInfo.newBuilder(datasetId).setDescription("new description").build());

    assertEquals(description, bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDescription());
  }

  @Test
  public void deleteDataset() {
    DatasetCow datasetCow = resourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    bigQueryCow.delete(datasetId);
    assertNull(bigQueryCow.getDataSet(datasetId).getDatasetInfo());
  }

  @Test
  public void createTable() {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    TableCow tableCow = resourceTracker.createTableCow();

    TableId tableId = tableCow.getTableInfo().getTableId();
    assertTableIdEqual(
        tableId,
        bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo().getTableId());
    assertThat(
        record,
        Matchers.contains(
            new CloudResourceUid()
                .googleBigQueryTableUid(
                    new GoogleBigQueryTableUid()
                        .projectId(tableId.getProject())
                        .datasetId(tableId.getDataset())
                        .tableId(tableId.getTable()))));
  }

  @Test
  public void updateTable() {
    String description = "des";
    TableCow tableCow = resourceTracker.createTableCow();
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
    TableCow tableCow = resourceTracker.createTableCow();
    TableId tableId = tableCow.getTableInfo().getTableId();
    assertNotNull(bigQueryCow.getTable(tableCow.getTableInfo().getTableId()).getTableInfo());

    bigQueryCow.delete(tableId);
    assertNull(bigQueryCow.getTable(tableId).getTableInfo());
  }

  @Test
  public void getTable() {
    TableCow tableCow = resourceTracker.createTableCow();
    TableId tableId = tableCow.getTableInfo().getTableId();

    assertTableIdEqual(tableId, bigQueryCow.getTable(tableId).getTableInfo().getTableId());
  }

  @Test
  public void getTableWithDatasetId() {
    TableCow tableCow = resourceTracker.createTableCow();
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
    TableCow tableCow1 = resourceTracker.createTableCow();
    TableId tableId1 = tableCow1.getTableInfo().getTableId();
    TableCow tableCow2 = resourceTracker.createTableCow();
    TableId tableId2 = tableCow2.getTableInfo().getTableId();

    assertTableIdContainsExactlyInCowPage(
        ImmutableList.of(tableId1, tableId2),
        bigQueryCow.listTables(reusableDataset.getDatasetId()));
    assertTableIdContainsExactlyInCowPage(
        ImmutableList.of(tableId1, tableId2),
        bigQueryCow.listTables(reusableDataset.getDatasetId().getDataset()));
  }

  @Test
  public void insertThenQuery() throws Exception {
    String fieldName = "field1";
    String fieldValue = "value1";

    // The random UUID tableId is not the standard table id format, to make query work, manually
    // creates shorter name.
    // TableCow tableCow = resourceTracker.createTableCow("table1");
    TableCow tableCow = resourceTracker.createTableCow();
    TableId tableId = tableCow.getTableInfo().getTableId();

    TableDefinition tableDefinition =
        StandardTableDefinition.of(Schema.of(Field.of(fieldName, LegacySQLTypeName.STRING)));
    bigQueryCow.update(tableCow.getTableInfo().toBuilder().setDefinition(tableDefinition).build());

    // Insert
    assertTrue(
        bigQueryCow
            .insertAll(
                InsertAllRequest.of(
                    tableId,
                    InsertAllRequest.RowToInsert.of(ImmutableMap.of(fieldName, fieldValue))))
            .getInsertErrors()
            .isEmpty());

    // Query
    Iterator<FieldValueList> fieldValueLists =
        bigQueryCow
            .query(
                QueryJobConfiguration.newBuilder(
                        "SELECT "
                            + fieldName
                            + " FROM `"
                            + tableId.getDataset()
                            + "`.`"
                            + tableId.getTable()
                            + "`")
                    .build())
            .getValues()
            .iterator();
    assertEquals(fieldValue, fieldValueLists.next().get(fieldName).getStringValue());
    assertFalse(fieldValueLists.hasNext());
  }
}
