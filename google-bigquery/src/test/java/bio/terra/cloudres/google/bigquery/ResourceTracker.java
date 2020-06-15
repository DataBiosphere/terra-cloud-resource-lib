package bio.terra.cloudres.google.bigquery;

import static com.google.common.base.Preconditions.checkNotNull;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** Helper class to track BigQuery resources created in tests and do best effort clean up. */
public class ResourceTracker {
  private final BigQueryCow bigQueryCow;
  /* The dataset id used to create tables. */
  private final String datasetId;
  private final List<TableId> createdTableIds = new ArrayList<>();
  private final List<String> createdDatasetIds = new ArrayList<>();

  public ResourceTracker(BigQueryCow bigQueryCow, @Nullable String datasetId) {
    this.bigQueryCow = bigQueryCow;
    this.datasetId = datasetId;
  }

  public TableCow createTableCow() {
    checkNotNull(datasetId);
    String generatedTableId = IntegrationUtils.randomNameWithUnderscore();
    TableId tableId = TableId.of(datasetId, generatedTableId);
    createdTableIds.add(tableId);

    return bigQueryCow.create(
        TableInfo.newBuilder(tableId, StandardTableDefinition.newBuilder().build()).build());
  }

  /** Create a DatasetCow also register that for cleanup. */
  public DatasetCow createDatasetCow() {
    String datasetId = IntegrationUtils.randomNameWithUnderscore();
    createdDatasetIds.add(datasetId);

    return bigQueryCow.create(DatasetInfo.newBuilder(datasetId).build());
  }

  public void tearDown() {
    createdTableIds.forEach(bigQueryCow::delete);
    createdDatasetIds.forEach(bigQueryCow::delete);
  }
}
