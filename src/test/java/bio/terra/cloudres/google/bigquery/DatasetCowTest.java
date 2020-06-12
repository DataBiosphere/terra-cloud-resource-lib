package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
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

  // Cleanup the tableId and datesetId list and use them to track resources created in one test
  // method.
  // In this way to can make sure tables/datasets can be cleaned up with best effort.
  private List<String> createdDatasetIds = new ArrayList<>();
  private List<TableId> createdTableIds = new ArrayList<>();

  @AfterEach
  public void tearDown() {
    createdTableIds.forEach(bigQueryCow::delete);
    createdDatasetIds.forEach(bigQueryCow::delete);
  }

  @Test
  public void reload() {
    DatasetCow datasetCow = createDatasetCow(bigQueryCow, createdDatasetIds);

    assertEquals(datasetCow.getDatasetInfo(), datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void update() {
    DatasetCow datasetCow = createDatasetCow(bigQueryCow, createdDatasetIds);

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
    DatasetCow datasetCow = createDatasetCow(bigQueryCow, createdDatasetIds);
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    datasetCow.delete();
    assertNull(datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void createThenGetTable() {
    DatasetCow datasetCow = createDatasetCow(bigQueryCow, createdDatasetIds);
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();

    TableId tableId = TableId.of(datasetId, generatedTableId);

    datasetCow.create(generatedTableId, StandardTableDefinition.newBuilder().build());
    createdTableIds.add(tableId);

    assertTableIdEqual(tableId, datasetCow.getTable(generatedTableId).getTableInfo().getTableId());
  }
}
