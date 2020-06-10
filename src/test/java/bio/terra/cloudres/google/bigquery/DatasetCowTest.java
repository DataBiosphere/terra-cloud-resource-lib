package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.assertTableIdEqual;
import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.defaultBigQueryCow;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.*;

@Tag("integration")
public class DatasetCowTest {
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();
  private BigQueryCow bigQueryCow = defaultBigQueryCow();

  private List<String> createdDatasetIds;
  private List<TableId> createdTableIds;

  @BeforeEach
  public void setUp() {
    // Cleanup the createdTableIds list and use this to track tables created in one test method.
    // In this way to can make sure tables can always be cleaned up even test failed in the middle.
    createdTableIds = new ArrayList<>();
    createdDatasetIds = new ArrayList<>();
  }

  @AfterEach
  public void tearDown() {
    createdTableIds.forEach(bigQueryCow::delete);
    createdDatasetIds.forEach(bigQueryCow::delete);
  }

  @Test
  public void reload() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());
    createdDatasetIds.add(datasetId);

    assertEquals(datasetCow.getDatasetInfo(), datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void update() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());
    createdDatasetIds.add(datasetId);
    assertNull(datasetCow.getDatasetInfo().getDescription());

    String description = "new description";
    DatasetCow updatedDatasetCow =
        new DatasetCow(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            (Dataset)
                datasetCow.getDatasetInfo().toBuilder().setDescription("new description").build());
    updatedDatasetCow.update();

    assertEquals(description, datasetCow.reload().getDatasetInfo().getDescription());
  }

  @Test
  public void delete() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());
    createdDatasetIds.add(datasetId);

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    datasetCow.delete();
    assertNull(datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void createThenGetTable() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());
    createdDatasetIds.add(datasetId);
    TableId tableId = TableId.of(datasetId, generatedTableId);

    datasetCow.create(generatedTableId, StandardTableDefinition.newBuilder().build());
    createdTableIds.add(tableId);

    assertTableIdEqual(tableId, datasetCow.getTable(generatedTableId).getTableInfo().getTableId());
  }
}
