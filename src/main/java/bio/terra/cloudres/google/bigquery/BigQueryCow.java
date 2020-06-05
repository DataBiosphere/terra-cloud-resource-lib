package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQuery.DatasetOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link BigQuery} */
public class BigQueryCow {
  private final Logger logger = LoggerFactory.getLogger(BigQueryCow.class);

  private final OperationAnnotator operationAnnotator;
  private final BigQuery bigQuery;

  public BigQueryCow(ClientConfig clientConfig, BigQueryOptions bigQueryOptions) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bigQuery = bigQueryOptions.getService();
  }

  /** See {@link BigQuery#create(DatasetInfo, DatasetOption...)}. */
  public Dataset createDataset(DatasetInfo datasetInfo, DatasetOption... datasetOptions) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_CREATE_DATASET,
        () -> bigQuery.create(datasetInfo, datasetOptions),
        () -> convert(datasetInfo, datasetOptions));
  }

  /** See {@link BigQuery#update(DatasetInfo, DatasetOption...)}. */
  public Dataset updateDataset(DatasetInfo datasetInfo, DatasetOption... datasetOptions) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_UPDATE_DATASET,
        () -> bigQuery.update(datasetInfo, datasetOptions),
        () -> convert(datasetInfo, datasetOptions));
  }

  /** See {@link BigQuery#delete(String, DatasetDeleteOption...)}. */
  public boolean deleteDataset(String datasetId, DatasetDeleteOption... deleteOptions) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_DATASET,
        () -> bigQuery.delete(datasetId, deleteOptions),
        () -> convert(datasetId, deleteOptions));
  }

  /** See {@link BigQuery#getDataset(String, DatasetOption...)}. */
  public Dataset getDataSet(String datasetId, DatasetOption... datasetOptions) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_GET_DATASET,
        () -> bigQuery.getDataset(datasetId, datasetOptions),
        () -> convert(datasetId, datasetOptions));
  }

  @VisibleForTesting
  static JsonObject convert(DatasetInfo datasetInfo, DatasetOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetId", gson.toJsonTree(datasetInfo));
    jsonObject.add("datasetOptions", gson.toJsonTree(options));
    return jsonObject;
  }

  @VisibleForTesting
  static JsonObject convert(String datasetId, DatasetOption... options) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("datasetId", datasetId);
    jsonObject.add("datasetOptions", new Gson().toJsonTree(options));
    return jsonObject;
  }

  @VisibleForTesting
  static JsonObject convert(String datasetId, DatasetDeleteOption... options) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("datasetId", datasetId);
    jsonObject.addProperty("datasetDeleteOptions", new Gson().toJson(options));
    return jsonObject;
  }
}
