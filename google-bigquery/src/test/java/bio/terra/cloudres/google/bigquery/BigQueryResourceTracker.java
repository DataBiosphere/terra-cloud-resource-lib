package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Helper class to track BigQuery resources created in tests, this used to clean up resources with
 * best effort.
 */
public class BigQueryResourceTracker {
  private final BigQueryCow bigQueryCow;
  private final String datasetId; // must be a created dataset comment assumption.
  private final List<TableId> createdTableIds;
  private final List<String> createdDatasetIds;

  public BigQueryResourceTracker(BigQueryCow bigQueryCow, @Nullable String datasetId) {
    this.bigQueryCow = bigQueryCow;
    this.datasetId = datasetId;
    createdTableIds = new ArrayList<>();
    createdDatasetIds = new ArrayList<>();
  }

  public TableCow createTableCow() {
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
