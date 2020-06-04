package bio.terra.cloudres.google.bigquery;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
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
  private static final DatasetInfo.Builder REUSABLE_DATASET_INFO_BUILDER =
      DatasetInfo.newBuilder(REUSABLE_DATASET_ID);
  private static final DatasetInfo.Builder DATASET_INFO_BUILDER =
      DatasetInfo.newBuilder(DATASET_ID);

  private static BigQueryOptions defaultStorageOptions() {
    return BigQueryOptions.newBuilder()
        .setCredentials(GOOGLE_CREDENTIALS)
        .setProjectId(GOOGLE_CREDENTIALS.getProjectId())
        .build();
  }

  private static BigQueryCow bigQueryCow =
      new BigQueryCow(IntegrationUtils.DEFAULT_CLIENT_CONFIG, defaultStorageOptions());

  private static Dataset reusableDataset;

  @BeforeAll
  public static void createReusableDataset() {
    bigQueryCow.createDataset(REUSABLE_DATASET_INFO_BUILDER.build());
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
                DATASET_INFO_BUILDER.build(), DATASET_OPTION_ACCESS, DATASET_OPTION_CREATION_TIME)
            .toString());
  }

  @Test
  public void createDataset() {
    Dataset createdDataSet = bigQueryCow.createDataset(DATASET_INFO_BUILDER.build());

    assertEquals(createdDataSet, bigQueryCow.getDataSet(DATASET_ID));
    assertEquals(DATASET_ID, createdDataSet.getDatasetId().getDataset());
  }

  @Test
  public void getDataset() {
    assertEquals(
        REUSABLE_DATASET_ID,
        bigQueryCow.getDataSet(REUSABLE_DATASET_ID).getDatasetId().getDataset());
  }

  @Test
  public void updateDataset() {
    assertNull(bigQueryCow.getDataSet(REUSABLE_DATASET_ID).getDescription());
    String description = "new description";
    bigQueryCow.updateDataset(
        REUSABLE_DATASET_INFO_BUILDER.setDescription("new description").build());
    assertEquals(description, bigQueryCow.getDataSet(REUSABLE_DATASET_ID).getDescription());
  }

  @Test
  public void deleteDataset() {
    bigQueryCow.createDataset(DATASET_INFO_BUILDER.build());
    assertNotNull(bigQueryCow.getDataSet(DATASET_ID));
    bigQueryCow.deleteDataset(DATASET_ID);
    assertNull(bigQueryCow.getDataSet(DATASET_ID));
  }
}
