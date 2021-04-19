package bio.terra.cloudres.google.bigquery;

import static com.google.common.base.Preconditions.checkNotNull;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.DatasetReference;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** Helper class to track BigQuery resources created in tests and do best effort clean up. */
public class ResourceTracker {
  private final BigQueryCow bigQueryCow;
  private final String projectId;
  /* The dataset id used to create tables, Must be non-null if {@link ResourceTracker#createTable} is used. */
  private final String datasetId;
  private final List<String> createdTableIds = new ArrayList<>();
  private final List<String> createdDatasetIds = new ArrayList<>();

  public ResourceTracker(BigQueryCow bigQueryCow, String projectId, @Nullable String datasetId) {
    this.bigQueryCow = bigQueryCow;
    this.projectId = projectId;
    this.datasetId = datasetId;
  }

  /** Create a BQ table and record it for later cleanup. */
  public Table createTable() throws IOException {
    checkNotNull(datasetId);
    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();
    createdTableIds.add(generatedTableId);
    TableReference tableReference =
        new TableReference()
            .setProjectId(projectId)
            .setDatasetId(datasetId)
            .setTableId(generatedTableId);
    Table tableToCreate = new Table().setTableReference(tableReference);
    return bigQueryCow.tables().insert(projectId, datasetId, tableToCreate).execute();
  }

  /** Create a BQ dataset and record it for cleanup. */
  public Dataset createDataset() throws IOException {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    createdDatasetIds.add(datasetId);
    DatasetReference datasetReference =
        new DatasetReference().setProjectId(projectId).setDatasetId(datasetId);
    Dataset datasetToCreate = new Dataset().setDatasetReference(datasetReference);
    return bigQueryCow.datasets().insert(projectId, datasetToCreate).execute();
  }

  /** Delete a BQ table and remove its entry for cleanup. */
  public void deleteTable(TableReference tableReference) throws IOException {
    bigQueryCow
        .tables()
        .delete(
            tableReference.getProjectId(),
            tableReference.getDatasetId(),
            tableReference.getTableId())
        .execute();
    createdTableIds.remove(tableReference.getTableId());
  }

  /** Delete a BQ dataset and remove its entry for cleanup. */
  public void deleteDataset(DatasetReference datasetReference) throws IOException {
    bigQueryCow
        .datasets()
        .delete(datasetReference.getProjectId(), datasetReference.getDatasetId())
        .setDeleteContents(true)
        .execute();
    createdDatasetIds.remove(datasetReference.getDatasetId());
  }

  public void tearDown() throws IOException {
    for (String tableId : createdTableIds) {
      bigQueryCow.tables().delete(projectId, datasetId, tableId).execute();
    }

    for (String createdDatasetId : createdDatasetIds) {
      bigQueryCow.datasets().delete(projectId, createdDatasetId).setDeleteContents(true).execute();
    }
  }
}
