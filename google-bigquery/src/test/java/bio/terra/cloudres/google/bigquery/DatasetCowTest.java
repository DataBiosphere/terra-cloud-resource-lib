package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
import org.junit.jupiter.api.*;

@Tag("integration")
public class DatasetCowTest {
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();
  private BigQueryCow bigQueryCow = defaultBigQueryCow();
  private final BigQueryResourceTracker bigQueryResourceTracker =
      new BigQueryResourceTracker(bigQueryCow, REUSABLE_DATASET_ID);

  @AfterEach
  public void tearDown() {
    bigQueryResourceTracker.tearDown();
  }

  @Test
  public void reload() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();

    assertEquals(datasetCow.getDatasetInfo(), datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void update() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();

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
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    datasetCow.delete();
    assertNull(datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void createThenGetTable() {
    DatasetCow datasetCow = bigQueryResourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();
    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();
    TableId tableId = TableId.of(datasetId, generatedTableId);
    datasetCow.create(generatedTableId, StandardTableDefinition.newBuilder().build());

    assertTableIdEqual(tableId, datasetCow.getTable(generatedTableId).getTableInfo().getTableId());

    bigQueryCow.delete(tableId);
  }
}
