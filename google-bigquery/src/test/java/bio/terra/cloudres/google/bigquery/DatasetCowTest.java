package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.assertTableIdEqual;
import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.defaultBigQueryCow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import bio.terra.cloudres.testing.MockJanitorService;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBigQueryTableUid;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class DatasetCowTest {
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomNameWithUnderscore();
  private BigQueryCow bigQueryCow = defaultBigQueryCow();
  private final ResourceTracker resourceTracker =
      new ResourceTracker(bigQueryCow, REUSABLE_DATASET_ID);
  private MockJanitorService mockJanitorService;

  @BeforeEach
  public void setUp() {
    mockJanitorService = new MockJanitorService();
    mockJanitorService.setup();
  }

  @AfterEach
  public void tearDown() {
    resourceTracker.tearDown();
    mockJanitorService.stop();
  }

  @Test
  public void reload() throws Exception {
    DatasetCow datasetCow = resourceTracker.createDatasetCow();

    assertEquals(datasetCow.getDatasetInfo(), datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void update() {
    DatasetCow datasetCow = resourceTracker.createDatasetCow();

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
    DatasetCow datasetCow = resourceTracker.createDatasetCow();
    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    datasetCow.delete();
    assertNull(datasetCow.reload().getDatasetInfo());
  }

  @Test
  public void createThenGetTable() throws Exception {
    DatasetCow datasetCow = resourceTracker.createDatasetCow();

    String datasetId = datasetCow.getDatasetInfo().getDatasetId().getDataset();
    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();
    TableId tableId = TableId.of(datasetId, generatedTableId);
    datasetCow.create(generatedTableId, StandardTableDefinition.newBuilder().build());

    assertTableIdEqual(tableId, datasetCow.getTable(generatedTableId).getTableInfo().getTableId());
    assertThat(
        mockJanitorService.getRecordedResources(),
        Matchers.contains(
            new CloudResourceUid()
                .googleBigQueryTableUid(
                    new GoogleBigQueryTableUid()
                        .projectId(datasetCow.getDatasetInfo().getDatasetId().getProject())
                        .datasetId(tableId.getDataset())
                        .tableId(tableId.getTable()))));

    bigQueryCow.delete(tableId);
  }
}
