package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.SerializeUtils.convert;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.TableId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for {@link Dataset}. */
public class DatasetCow {
  private final Logger logger = LoggerFactory.getLogger(DatasetCow.class);

  private final OperationAnnotator operationAnnotator;
  private final Dataset dataset;
  private final ClientConfig clientConfig;

  DatasetCow(ClientConfig clientConfig, Dataset dataset) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.dataset = dataset;
    this.clientConfig = clientConfig;
  }

  public DatasetInfo getDatasetInfo() {
    return dataset;
  }

  /** See {@link Dataset#reload(BigQuery.DatasetOption...)} */
  public DatasetCow reload(BigQuery.DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_RELOAD_DATASET,
            () -> dataset.reload(datasetOptions),
            () -> convert(dataset.getDatasetId().getDataset(), datasetOptions)));
  }

  /** See {@link Dataset#update(BigQuery.DatasetOption...)}. */
  public DatasetCow update(BigQuery.DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_UPDATE_DATASET,
            () -> dataset.update(datasetOptions),
            () -> convert(dataset, datasetOptions)));
  }

  /** See {@link Dataset#delete(BigQuery.DatasetDeleteOption...)} */
  public boolean delete(BigQuery.DatasetDeleteOption... deleteOptions) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_DELETE_DATASET,
        () -> dataset.delete(deleteOptions),
        () -> convert(dataset.getDatasetId().getDataset(), deleteOptions));
  }

  /** See {@link Dataset#get(String, BigQuery.TableOption...)}. */
  public TableCow getTable(String tableId, BigQuery.TableOption... tableOptions) {
    return new TableCow(clientConfig, operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_GET_BIGQUERY_TABLE,
            () -> dataset.get(tableId, tableOptions),
            () -> convert(TableId.of(dataset.getDatasetId().getDataset(), tableId), tableOptions)));
  }
}
