package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class DatasetCowTest {
  private BigQueryCow bigQueryCow = BigQueryIntegrationUtils.defaultBigQueryCow();

  @Test
  public void reload() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());

    assertEquals(datasetCow.getDatasetInfo(), datasetCow.reload().getDatasetInfo());

    bigQueryCow.delete(datasetId);
  }

  @Test
  public void update() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());
    assertNull(datasetCow.getDatasetInfo().getDescription());

    String description = "new description";
    DatasetCow updatedDatasetCow =
        new DatasetCow(
            IntegrationUtils.DEFAULT_CLIENT_CONFIG,
            (Dataset)
                datasetCow.getDatasetInfo().toBuilder().setDescription("new description").build());
    updatedDatasetCow.update();

    assertEquals(description, datasetCow.reload().getDatasetInfo().getDescription());

    // cleanup
    bigQueryCow.delete(datasetId);
  }

  @Test
  public void delete() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetCow datasetCow = bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    datasetCow.delete();
    assertNull(datasetCow.reload().getDatasetInfo());
  }
}
