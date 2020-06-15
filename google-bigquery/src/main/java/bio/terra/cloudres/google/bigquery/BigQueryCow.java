package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.SerializeUtils.convert;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQuery.DatasetOption;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link BigQuery} */
public class BigQueryCow {
  private final Logger logger = LoggerFactory.getLogger(BigQueryCow.class);

  private final OperationAnnotator operationAnnotator;
  private final BigQuery bigQuery;
  private final ClientConfig clientConfig;

  public BigQueryCow(ClientConfig clientConfig, BigQueryOptions bigQueryOptions) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bigQuery = bigQueryOptions.getService();
    this.clientConfig = clientConfig;
  }

  /** See {@link BigQuery#create(DatasetInfo, DatasetOption...)}. */
  public DatasetCow createDataset(DatasetInfo datasetInfo, DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_CREATE_DATASET,
            () -> bigQuery.create(datasetInfo, datasetOptions),
            () -> convert(datasetInfo, datasetOptions)));
  }

  /** See {@link BigQuery#update(DatasetInfo, DatasetOption...)}. */
  public DatasetCow updateDataset(DatasetInfo datasetInfo, DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_UPDATE_DATASET,
            () -> bigQuery.update(datasetInfo, datasetOptions),
            () -> convert(datasetInfo, datasetOptions)));
  }

  /** See {@link BigQuery#delete(String, DatasetDeleteOption...)}. */
  public boolean deleteDataset(String datasetId, DatasetDeleteOption... deleteOptions) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_DATASET,
        () -> bigQuery.delete(datasetId, deleteOptions),
        () -> convert(datasetId, deleteOptions));
  }

  /** See {@link BigQuery#getDataset(String, DatasetOption...)}. */
  public DatasetCow getDataSet(String datasetId, DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_GET_DATASET,
            () -> bigQuery.getDataset(datasetId, datasetOptions),
            () -> convert(datasetId, datasetOptions)));
  }
}
