package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.*;
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
  private BigQueryCow bigQueryCow = BigQueryIntegrationUtils.defaultBigQueryCow();

  @Test
  public void createDataset() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetInfo createdDataSet =
        bigQueryCow.createDataset(DatasetInfo.newBuilder(datasetId).build()).getDatasetInfo();

    assertEquals(createdDataSet, bigQueryCow.getDataSet(datasetId).getDatasetInfo());
    assertEquals(datasetId, createdDataSet.getDatasetId().getDataset());
    bigQueryCow.deleteDataset(datasetId);
  }

  @Test
  public void getDataset() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    bigQueryCow.createDataset(DatasetInfo.newBuilder(datasetId).build());

    assertEquals(
        datasetId, bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDatasetId().getDataset());

    bigQueryCow.deleteDataset(datasetId);
  }

  @Test
  public void updateDataset() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    bigQueryCow.createDataset(DatasetInfo.newBuilder(datasetId).build());
    assertNull(bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDescription());

    String description = "new description";
    bigQueryCow.updateDataset(
        DatasetInfo.newBuilder(datasetId).setDescription("new description").build());

    assertEquals(description, bigQueryCow.getDataSet(datasetId).getDatasetInfo().getDescription());

    // cleanup
    bigQueryCow.deleteDataset(datasetId);
  }

  @Test
  public void deleteDataset() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    bigQueryCow.createDataset(DatasetInfo.newBuilder(datasetId).build());

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    bigQueryCow.deleteDataset(datasetId);
    assertNull(bigQueryCow.getDataSet(datasetId).getDatasetInfo());
  }
}
