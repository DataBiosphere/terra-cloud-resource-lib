package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * A Cloud Object Wrapper(COW) for Google API Client Library: {@link ResourceManager}
 *
 * <p>Eventually there might be multiple COW classes for each resource type, e.g. ProjectCow.
 */
public class BigQueryCow {
  private final Logger logger = LoggerFactory.getLogger(BigQueryCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final BigQuery bigQuery;

  public BigQueryCow(
      ClientConfig clientConfig, BigQueryOptions bigQueryOptions) {
    this.clientConfig = clientConfig;
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bigQuery = bigQueryOptions.getService();
  }

  /**
   * Gets {@link Dataset}
   *
   * @param datasetId The DatasetId to get
   * @param options The array of {@link DatasetOption}
   * @return the project being created
   */
  public Dataset getDataSet(String datasetId, DatasetOption... options) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_GET_DATASET,
        () -> bigQuery.getDataset(datasetId, options),
        () -> convert(datasetId, options));
  }

  @VisibleForTesting
   static JsonObject convert(String datasetId, DatasetOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("datasetId", datasetId);
    jsonObject.addProperty("options", Arrays.toString(options));
    return jsonObject;
  }
}
