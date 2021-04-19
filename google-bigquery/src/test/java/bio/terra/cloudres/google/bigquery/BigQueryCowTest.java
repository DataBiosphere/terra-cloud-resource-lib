package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.BigQueryIntegrationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleBigQueryDatasetUid;
import bio.terra.janitor.model.GoogleBigQueryTableUid;
import com.google.api.services.bigquery.model.Binding;
import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetList;
import com.google.api.services.bigquery.model.DatasetReference;
import com.google.api.services.bigquery.model.GetIamPolicyRequest;
import com.google.api.services.bigquery.model.Policy;
import com.google.api.services.bigquery.model.SetIamPolicyRequest;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableList;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TestIamPermissionsRequest;
import com.google.api.services.bigquery.model.TestIamPermissionsResponse;
import java.io.IOException;
import java.util.Collections;
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
  private static String projectId =
      IntegrationCredentials.getAdminGoogleCredentialsOrDie().getProjectId();
  private static Dataset reusableDataset;
  private final ResourceTracker resourceTracker =
      new ResourceTracker(bigQueryCow, projectId, REUSABLE_DATASET_ID);

  @BeforeAll
  public static void createReusableDataset() throws Exception {
    DatasetReference datasetReference =
        new DatasetReference().setDatasetId(REUSABLE_DATASET_ID).setProjectId(projectId);
    Dataset datasetToCreate = new Dataset().setDatasetReference(datasetReference);
    reusableDataset = bigQueryCow.datasets().insert(projectId, datasetToCreate).execute();
  }

  @AfterAll
  public static void deleteReusableDataset() throws IOException {
    bigQueryCow.datasets().delete(projectId, REUSABLE_DATASET_ID).execute();
  }

  @AfterEach
  public void tearDown() throws IOException {
    resourceTracker.tearDown();
  }

  @Test
  public void deleteDataset() throws IOException {
    Dataset dataset = resourceTracker.createDataset();
    DatasetReference datasetReference = dataset.getDatasetReference();

    Dataset fetchedDataset =
        bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()).execute();
    assertNotNull(fetchedDataset);
    bigQueryCow.datasets().delete(projectId, datasetReference.getDatasetId()).execute();
    assertThrows(
        IOException.class,
        () -> bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()));
  }

  @Test
  public void deleteDatasetWithTable() throws IOException {
    Dataset dataset = resourceTracker.createDataset();
    DatasetReference datasetReference = dataset.getDatasetReference();

    TableReference tableToCreateReference =
        new TableReference()
            .setProjectId(datasetReference.getProjectId())
            .setDatasetId(datasetReference.getDatasetId())
            .setTableId(IntegrationUtils.randomNameWithUnderscore());
    Table tableToCreate = new Table().setTableReference(tableToCreateReference);
    bigQueryCow
        .tables()
        .insert(datasetReference.getProjectId(), datasetReference.getDatasetId(), tableToCreate)
        .execute();

    Dataset fetchedDataset =
        bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()).execute();
    assertNotNull(fetchedDataset);

    // Attempt a delete call with deleteContents flag set to false. Because this dataset contains a
    // table, this should fail.
    assertThrows(
        IOException.class,
        () ->
            bigQueryCow
                .datasets()
                .delete(projectId, datasetReference.getDatasetId())
                .setDeleteContents(false)
                .execute());
    fetchedDataset =
        bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()).execute();
    assertNotNull(fetchedDataset);

    // Call delete again, this time with deleteContents true. This call should succeed.
    bigQueryCow
        .datasets()
        .delete(projectId, datasetReference.getDatasetId())
        .setDeleteContents(true)
        .execute();
    assertThrows(
        IOException.class,
        () -> bigQueryCow.datasets().get(projectId, datasetReference.getDatasetId()));
  }

  @Test
  public void insertDataset() throws IOException {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    Dataset dataset = resourceTracker.createDataset();
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
    Dataset dataset1 = resourceTracker.createDataset();
    Dataset dataset2 = resourceTracker.createDataset();

    DatasetList fetchedDatasets = bigQueryCow.datasets().list(projectId).execute();
    assertEquals(2, fetchedDatasets.getDatasets().size());
    assertThat(fetchedDatasets.getDatasets(), containsInAnyOrder(dataset1, dataset2));
  }

  @Test
  public void patchDataset() throws IOException {
    Dataset dataset = resourceTracker.createDataset();
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
    Dataset dataset = resourceTracker.createDataset();
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
    Table table = resourceTracker.createTable();
    TableReference tableReference = table.getTableReference();

    Table fetchedTable =
        bigQueryCow
            .tables()
            .get(projectId, REUSABLE_DATASET_ID, tableReference.getTableId())
            .execute();
    assertNotNull(fetchedTable);
    bigQueryCow
        .tables()
        .delete(projectId, REUSABLE_DATASET_ID, tableReference.getTableId())
        .execute();
    assertThrows(
        IOException.class,
        () ->
            bigQueryCow
                .tables()
                .get(projectId, REUSABLE_DATASET_ID, tableReference.getTableId())
                .execute());
  }

  @Test
  public void getTableIamPolicy() throws IOException {
    Table table = resourceTracker.createTable();
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
    assertFalse(policy.getBindings().isEmpty());
  }

  @Test
  public void insertTable() throws IOException {
    List<CloudResourceUid> record = CleanupRecorder.startNewRecordForTesting();
    Table table = resourceTracker.createTable();
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
    Table table1 = resourceTracker.createTable();
    Table table2 = resourceTracker.createTable();

    TableList tableList = bigQueryCow.tables().list(projectId, REUSABLE_DATASET_ID).execute();
    assertNotNull(tableList);
    assertEquals(2, tableList.getTables().size());
    assertThat(tableList.getTables(), containsInAnyOrder(table1, table2));
  }

  @Test
  public void patchTable() throws IOException {
    Table table = resourceTracker.createTable();
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
  public void setTableIamPolicy() throws IOException {
    Table table = resourceTracker.createTable();
    TableReference tableReference = table.getTableReference();

    Policy originalPolicy =
        bigQueryCow
            .tables()
            .getIamPolicy(
                projectId,
                REUSABLE_DATASET_ID,
                tableReference.getTableId(),
                new GetIamPolicyRequest())
            .execute();

    Policy modifiedPolicy = originalPolicy.clone();
    List<Binding> bindingList = modifiedPolicy.getBindings();
    String member =
        String.format(
            "user:%s", IntegrationCredentials.getUserGoogleCredentialsOrDie().getClientEmail());
    Binding newBinding =
        new Binding()
            .setRole("roles/bigquery.dataViewer")
            .setMembers(Collections.singletonList(member));
    bindingList.add(newBinding);
    modifiedPolicy.setBindings(bindingList);

    Policy updatedPolicy =
        bigQueryCow
            .tables()
            .setIamPolicy(
                projectId,
                REUSABLE_DATASET_ID,
                tableReference.getTableId(),
                new SetIamPolicyRequest().setPolicy(originalPolicy))
            .execute();

    assertEquals(originalPolicy.getBindings().size() + 1, updatedPolicy.getBindings().size());
    assertThat(updatedPolicy.getBindings(), hasItem(newBinding));

    // New binding list should be exactly the same as the old list, except for our one addition.
    List<Binding> newBindingList = updatedPolicy.getBindings();
    newBindingList.remove(newBinding);
    assertThat(newBindingList, containsInAnyOrder(originalPolicy.getBindings()));
  }

  @Test
  public void testTableIamPolicy() throws IOException {
    Table table = resourceTracker.createTable();
    TableReference tableReference = table.getTableReference();

    // Table creator should have get permission on the table, but not random other permissions.
    List<String> permissionsToCheck = List.of("bigquery.tables.get", "lifesciences.operations.get");
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
    Table table = resourceTracker.createTable();
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
}
