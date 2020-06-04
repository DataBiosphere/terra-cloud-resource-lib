package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQuery.DatasetOption;
import org.junit.jupiter.api.*;

@Tag("integration")
/**
 * Note that Dataset has quota about "5 operations every 10 seconds per dataset". So in some update,
 * delete test, need to create a new dataset everytime.
 */
public class BigQueryCowTest {
  private static final ServiceAccountCredentials GOOGLE_CREDENTIALS =
      IntegrationCredentials.getGoogleCredentialsOrDie();

  // Dataset id only allows underscore
  private static final String DATASET_ID = IntegrationUtils.randomName().replace('-', '_');
  private static final String REUSABLE_DATASET_ID = IntegrationUtils.randomName().replace('-', '_');

  private static final DatasetOption DATASET_OPTION_ACCESS =
      DatasetOption.fields(BigQuery.DatasetField.ACCESS);
  private static final DatasetOption DATASET_OPTION_CREATION_TIME =
      DatasetOption.fields(BigQuery.DatasetField.CREATION_TIME);
  private static final DatasetDeleteOption DATASET_OPTION_DELETE_CREATION_TIME =
      DatasetDeleteOption.deleteContents();

  private static BigQueryOptions defaultBigQueryOption() {
    return BigQueryOptions.newBuilder()
        .setCredentials(GOOGLE_CREDENTIALS)
        .setProjectId(GOOGLE_CREDENTIALS.getProjectId())
        .build();
  }

  private static BigQueryCow bigQueryCow =
      new BigQueryCow(IntegrationUtils.DEFAULT_CLIENT_CONFIG, defaultBigQueryOption());

  @BeforeAll
  public static void createReusableDataset() {
    bigQueryCow.createDataset(DatasetInfo.newBuilder(REUSABLE_DATASET_ID).build());
  }

  @AfterAll
  public static void deleteReusableDataset() {
    bigQueryCow.deleteDataset(REUSABLE_DATASET_ID);
  }

  @AfterEach
  public void tearDown() {
    bigQueryCow.deleteDataset(DATASET_ID);
  }

  @Test
  public void getDataset() {
    assertEquals(
        REUSABLE_DATASET_ID,
        bigQueryCow.getDataSet(REUSABLE_DATASET_ID).getDatasetId().getDataset());
  }

  @Test
  public void updateDataset() {
    String datasetId = IntegrationUtils.randomName().replace('-', '_');
    bigQueryCow.createDataset(DatasetInfo.newBuilder(datasetId).build());
    assertNull(bigQueryCow.getDataSet(datasetId).getDescription());

    String description = "new description";
    bigQueryCow.updateDataset(
        DatasetInfo.newBuilder(datasetId).setDescription("new description").build());

    assertEquals(description, bigQueryCow.getDataSet(datasetId).getDescription());

    // cleanup
    bigQueryCow.deleteDataset(datasetId);
  }

  @Test
  public void deleteDataset() {
    String datasetId = IntegrationUtils.randomName().replace('-', '_');
    bigQueryCow.createDataset(DatasetInfo.newBuilder(datasetId).build());

    assertNotNull(bigQueryCow.getDataSet(datasetId));
    bigQueryCow.deleteDataset(datasetId);
    assertNull(bigQueryCow.getDataSet(datasetId));
  }

  @Test
  public void convertDatasetIdWithOptions() {
    assertEquals(
        "{\"datasetId\":\"123\",\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        BigQueryCow.convert("123", DATASET_OPTION_ACCESS, DATASET_OPTION_CREATION_TIME).toString());
  }

  @Test
  public void convertDatasetInfoWithOptions() {
    assertEquals(
        "{\"datasetId\":{\"datasetId\":{\"dataset\":\""
            + DATASET_ID
            + "\"},\"labels\":{\"userMap\":{}}},\"datasetOptions\":[{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,access\"},{\"rpcOption\":\"FIELDS\",\"value\":\"datasetReference,creationTime\"}]}",
        BigQueryCow.convert(
                DatasetInfo.newBuilder(DATASET_ID).build(),
                DATASET_OPTION_ACCESS,
                DATASET_OPTION_CREATION_TIME)
            .toString());
  }

  @Test
  public void convertDatasetInfoWithDeleteOptions() {
    assertEquals(
        "{\"datasetId\":\""
            + DATASET_ID
            + "\",\"datasetDeleteOptions\":\"[{\\\"rpcOption\\\":\\\"DELETE_CONTENTS\\\",\\\"value\\\":true}]\"}",
        BigQueryCow.convert(DATASET_ID, DATASET_OPTION_DELETE_CREATION_TIME).toString());
  }

  @Test
  public void createDataset() {
    Dataset createdDataSet = bigQueryCow.createDataset(DatasetInfo.newBuilder(DATASET_ID).build());

    assertEquals(createdDataSet, bigQueryCow.getDataSet(DATASET_ID));
    assertEquals(DATASET_ID, createdDataSet.getDatasetId().getDataset());
  }
}
