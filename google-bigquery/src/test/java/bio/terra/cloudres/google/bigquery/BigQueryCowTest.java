package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBigQueryDatasetUid;
import bio.terra.janitor.model.GoogleBigQueryTableUid;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.bigquery.model.Binding;
import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetList;
import com.google.api.services.bigquery.model.DatasetList.Datasets;
import com.google.api.services.bigquery.model.DatasetReference;
import com.google.api.services.bigquery.model.GetIamPolicyRequest;
import com.google.api.services.bigquery.model.Policy;
import com.google.api.services.bigquery.model.SetIamPolicyRequest;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableList;
import com.google.api.services.bigquery.model.TableList.Tables;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TestIamPermissionsRequest;
import com.google.api.services.bigquery.model.TestIamPermissionsResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
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
  private static String projectId =
      IntegrationCredentials.getAdminGoogleCredentialsOrDie().getProjectId();
  private static Dataset reusableDataset;

  @BeforeAll
  public static void createReusableDataset() throws Exception {
    DatasetReference datasetReference =
        new DatasetReference().setDatasetId(REUSABLE_DATASET_ID).setProjectId(projectId);
    Dataset datasetToCreate = new Dataset().setDatasetReference(datasetReference);
    reusableDataset = bigQueryCow.datasets().insert(projectId, datasetToCreate).execute();
  }

  @AfterAll
  public static void deleteReusableDataset() throws IOException {
    bigQueryCow.datasets().delete(projectId, REUSABLE_DATASET_ID).setDeleteContents(true).execute();
  }

  @Test
  public void deleteDataset() throws IOException {
    Dataset dataset = createDataset();
    DatasetReference datasetReference = dataset.getDatasetReference();

    Dataset fetchedDataset =
        bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()).execute();
    assertNotNull(fetchedDataset);
    deleteDataset(datasetReference);
    GoogleJsonResponseException response =
        assertThrows(
            GoogleJsonResponseException.class,
            () -> bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()).execute());
    assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusCode());
  }

  @Test
  public void insertDataset() throws IOException {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    Dataset dataset = createDataset();
    DatasetReference datasetReference = dataset.getDatasetReference();

    Dataset fetchedDataset =
        bigQueryCow
            .datasets()
            .get(datasetReference.getProjectId(), datasetReference.getDatasetId())
            .execute();

    assertEquals(datasetReference, fetchedDataset.getDatasetReference());

    assertThat(
        record,
        Matchers.contains(
            new CloudResourceUid()
                .googleBigQueryDatasetUid(
                    new GoogleBigQueryDatasetUid()
                        .projectId(datasetReference.getProjectId())
                        .datasetId(datasetReference.getDatasetId()))));
  }

  @Test
  public void listDataset() throws IOException {
    Dataset dataset1 = createDataset();
    Dataset dataset2 = createDataset();

    // By default, Datasets.List returns 50 results. There may be more than 50 datasets, so increase
    // maxResults to make
    // sure we get all datasets.
    DatasetList fetchedDatasets =
        bigQueryCow.datasets().list(projectId).setMaxResults(1000L).execute();

    // Because this project has been used for testing before, it may have other datasets lying
    // around waiting for cleanup.
    assertTrue(fetchedDatasets.getDatasets().size() >= 2);
    // Each entry of the list response contains information that's hard to match, such as etag
    // values. Instead, here we check the datasetReference (equivalent to a UID).
    List<DatasetReference> fetchedDatasetReferences =
        fetchedDatasets.getDatasets().stream()
            .map(Datasets::getDatasetReference)
            .collect(Collectors.toList());
    assertThat(
        fetchedDatasetReferences,
        hasItems(equalTo(dataset1.getDatasetReference()), equalTo(dataset2.getDatasetReference())));
  }

  @Test
  public void patchDataset() throws IOException {
    Dataset dataset = createDataset();
    DatasetReference datasetReference = dataset.getDatasetReference();

    Dataset fetchedDataset =
        bigQueryCow
            .datasets()
            .get(datasetReference.getProjectId(), datasetReference.getDatasetId())
            .execute();
    assertNull(fetchedDataset.getDescription());

    String description = "new description";
    Dataset datasetToPatch = fetchedDataset.setDescription(description);
    bigQueryCow
        .datasets()
        .patch(projectId, datasetReference.getDatasetId(), datasetToPatch)
        .execute();

    fetchedDataset =
        bigQueryCow
            .datasets()
            .get(datasetReference.getProjectId(), datasetReference.getDatasetId())
            .execute();

    assertEquals(description, fetchedDataset.getDescription());
  }

  @Test
  public void updateDataset() throws IOException {
    Dataset dataset = createDataset();
    DatasetReference datasetReference = dataset.getDatasetReference();

    Dataset fetchedDataset =
        bigQueryCow
            .datasets()
            .get(datasetReference.getProjectId(), datasetReference.getDatasetId())
            .execute();
    assertNull(fetchedDataset.getDescription());

    String description = "new description";
    Dataset datasetToUpdate = fetchedDataset.setDescription(description);
    bigQueryCow
        .datasets()
        .update(projectId, datasetReference.getDatasetId(), datasetToUpdate)
        .execute();

    fetchedDataset =
        bigQueryCow
            .datasets()
            .get(datasetReference.getProjectId(), datasetReference.getDatasetId())
            .execute();

    assertEquals(description, fetchedDataset.getDescription());
  }

  @Test
  public void deleteTable() throws IOException {
    Table table = createTable();
    TableReference tableReference = table.getTableReference();

    Table fetchedTable =
        bigQueryCow
            .tables()
            .get(projectId, REUSABLE_DATASET_ID, tableReference.getTableId())
            .execute();
    assertNotNull(fetchedTable);
    deleteTable(tableReference);
    GoogleJsonResponseException missingResponse =
        assertThrows(
            GoogleJsonResponseException.class,
            () ->
                bigQueryCow
                    .tables()
                    .get(projectId, REUSABLE_DATASET_ID, tableReference.getTableId())
                    .execute());
    assertEquals(HttpStatus.SC_NOT_FOUND, missingResponse.getStatusCode());
  }

  @Test
  public void setAndGetTableIamPolicy() throws IOException {
    Table table = createTable();
    TableReference tableReference = table.getTableReference();

    Policy policy =
        bigQueryCow
            .tables()
            .getIamPolicy(
                projectId,
                REUSABLE_DATASET_ID,
                tableReference.getTableId(),
                new GetIamPolicyRequest())
            .execute();
    assertNotNull(policy);
    // GCP library uses null instead of an empty bindings list when no bindings are present.
    assertNull(policy.getBindings());

    List<Binding> bindingList = new ArrayList<>();
    String member =
        String.format(
            "serviceAccount:%s",
            IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail());
    Binding newBinding =
        new Binding()
            .setRole("roles/bigquery.dataViewer")
            .setMembers(Collections.singletonList(member));
    bindingList.add(newBinding);
    policy.setBindings(bindingList);

    Policy updatedPolicy =
        bigQueryCow
            .tables()
            .setIamPolicy(
                projectId,
                REUSABLE_DATASET_ID,
                tableReference.getTableId(),
                new SetIamPolicyRequest().setPolicy(policy))
            .execute();

    Policy fetchedPolicy =
        bigQueryCow
            .tables()
            .getIamPolicy(
                projectId,
                REUSABLE_DATASET_ID,
                tableReference.getTableId(),
                new GetIamPolicyRequest())
            .execute();

    assertEquals(updatedPolicy, fetchedPolicy);
    assertThat(updatedPolicy.getBindings(), hasItem(newBinding));
  }

  @Test
  public void insertTable() throws IOException {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    Table table = createTable();
    TableReference tableReference = table.getTableReference();

    Table fetchedTable =
        bigQueryCow
            .tables()
            .get(projectId, REUSABLE_DATASET_ID, tableReference.getTableId())
            .execute();

    assertEquals(tableReference, fetchedTable.getTableReference());

    assertThat(
        record,
        Matchers.contains(
            new CloudResourceUid()
                .googleBigQueryTableUid(
                    new GoogleBigQueryTableUid()
                        .projectId(tableReference.getProjectId())
                        .datasetId(tableReference.getDatasetId())
                        .tableId(tableReference.getTableId()))));
  }

  @Test
  public void listTables() throws IOException {
    Table table1 = createTable();
    Table table2 = createTable();

    TableList tableList = bigQueryCow.tables().list(projectId, REUSABLE_DATASET_ID).execute();
    assertNotNull(tableList);
    // We do not delete tables between tests to avoid BQ quota issues, so tables from other tests
    // will be present here.
    assertTrue(tableList.getTables().size() >= 2);
    // Each entry of the list response contains information that's hard to match, such as etag
    // values. Instead, here we check the tableReference (equivalent to a UID).
    List<TableReference> tableReferences =
        tableList.getTables().stream().map(Tables::getTableReference).collect(Collectors.toList());
    assertThat(
        tableReferences,
        hasItems(equalTo(table1.getTableReference()), equalTo(table2.getTableReference())));
  }

  @Test
  public void patchTable() throws IOException {
    Table table = createTable();
    TableReference tableReference = table.getTableReference();

    Table fetchedTable =
        bigQueryCow
            .tables()
            .get(
                tableReference.getProjectId(),
                tableReference.getDatasetId(),
                tableReference.getTableId())
            .execute();
    assertNull(fetchedTable.getDescription());

    String description = "this is a table description";
    fetchedTable.setDescription(description);
    Table patchedTable =
        bigQueryCow
            .tables()
            .patch(
                tableReference.getProjectId(),
                tableReference.getDatasetId(),
                tableReference.getTableId(),
                fetchedTable)
            .execute();

    assertEquals(patchedTable.getDescription(), description);
  }

  @Test
  public void testTableIamPolicy() throws IOException {
    Table table = createTable();
    TableReference tableReference = table.getTableReference();

    List<String> permissionsToCheck = List.of("bigquery.tables.get");
    TestIamPermissionsRequest request =
        new TestIamPermissionsRequest().setPermissions(permissionsToCheck);
    TestIamPermissionsResponse response =
        bigQueryCow
            .tables()
            .testIamPermissions(
                projectId, REUSABLE_DATASET_ID, tableReference.getTableId(), request)
            .execute();
    assertEquals(1, response.getPermissions().size());
    assertEquals("bigquery.tables.get", response.getPermissions().get(0));
  }

  @Test
  public void updateTable() throws IOException {
    Table table = createTable();
    TableReference tableReference = table.getTableReference();

    Table fetchedTable =
        bigQueryCow
            .tables()
            .get(
                tableReference.getProjectId(),
                tableReference.getDatasetId(),
                tableReference.getTableId())
            .execute();
    assertNull(fetchedTable.getDescription());

    String description = "this is a table description";
    fetchedTable.setDescription(description);
    Table updatedTable =
        bigQueryCow
            .tables()
            .update(
                tableReference.getProjectId(),
                tableReference.getDatasetId(),
                tableReference.getTableId(),
                fetchedTable)
            .execute();

    assertEquals(updatedTable.getDescription(), description);
  }

  /** Create a BQ table in the reusable BQ dataset with a random table name. */
  private Table createTable() throws IOException {
    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();
    TableReference tableReference =
        new TableReference()
            .setProjectId(projectId)
            .setDatasetId(REUSABLE_DATASET_ID)
            .setTableId(generatedTableId);
    Table tableToCreate = new Table().setTableReference(tableReference);
    return bigQueryCow.tables().insert(projectId, REUSABLE_DATASET_ID, tableToCreate).execute();
  }

  /** Create a BQ dataset with a random name. */
  private Dataset createDataset() throws IOException {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    DatasetReference datasetReference =
        new DatasetReference().setProjectId(projectId).setDatasetId(datasetId);
    Dataset datasetToCreate = new Dataset().setDatasetReference(datasetReference);
    return bigQueryCow.datasets().insert(projectId, datasetToCreate).execute();
  }

  /** Delete a BQ table from the reusable BQ dataset. */
  private void deleteTable(TableReference tableReference) throws IOException {
    bigQueryCow
        .tables()
        .delete(
            tableReference.getProjectId(),
            tableReference.getDatasetId(),
            tableReference.getTableId())
        .execute();
  }

  /** Delete a BQ dataset. */
  private void deleteDataset(DatasetReference datasetReference) throws IOException {
    bigQueryCow
        .datasets()
        .delete(datasetReference.getProjectId(), datasetReference.getDatasetId())
        .setDeleteContents(true)
        .execute();
  }
}
