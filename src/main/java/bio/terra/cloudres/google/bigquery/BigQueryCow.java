package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.SerializeUtils.convert;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.CowPageImpl;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.api.gax.paging.Page;
import com.google.cloud.bigquery.*;
import com.google.cloud.bigquery.BigQuery.DatasetDeleteOption;
import com.google.cloud.bigquery.BigQuery.DatasetOption;
import com.google.cloud.bigquery.BigQuery.TableOption;
import com.google.cloud.bigquery.BigQuery.TableListOption;
import com.google.cloud.bigquery.BigQuery.TableDataListOption;
import com.google.common.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link BigQuery} */
public class BigQueryCow {
  private final Logger logger = LoggerFactory.getLogger(BigQueryCow.class);

  private final OperationAnnotator operationAnnotator;
  private final BigQuery bigQuery;
  private final ClientConfig clientConfig;

  public final Function<Table, TableCow> tableToTableCow
          = new Function<Table, TableCow>() {
    @Override
    public TableCow apply(Table table) {
      return new TableCow(clientConfig, table);
    }
  };

  public BigQueryCow(ClientConfig clientConfig, BigQueryOptions bigQueryOptions) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.bigQuery = bigQueryOptions.getService();
    this.clientConfig = clientConfig;
  }

  /** See {@link BigQuery#create(DatasetInfo, DatasetOption...)}. */
  public DatasetCow create(DatasetInfo datasetInfo, DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_CREATE_DATASET,
            () -> bigQuery.create(datasetInfo, datasetOptions),
            () -> convert(datasetInfo, datasetOptions)));
  }

  /** See {@link BigQuery#update(DatasetInfo, DatasetOption...)}. */
  public DatasetCow update(DatasetInfo datasetInfo, DatasetOption... datasetOptions) {
    return new DatasetCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_UPDATE_DATASET,
            () -> bigQuery.update(datasetInfo, datasetOptions),
            () -> convert(datasetInfo, datasetOptions)));
  }

  /** See {@link BigQuery#delete(String, DatasetDeleteOption...)}. */
  public boolean delete(String datasetId, DatasetDeleteOption... deleteOptions) {
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

  /** See {@link BigQuery#create(TableInfo, TableOption...)}. */
  public TableCow create(TableInfo tableInfo, TableOption... tableOptions) {
    return new TableCow(
            clientConfig,
            operationAnnotator.executeCowOperation(
                    CloudOperation.GOOGLE_CREATE_BIGQUERY_TABLE,
                    () -> bigQuery.create(tableInfo, tableOptions),
                    () -> convert(tableInfo, tableOptions)));
  }

  /** See {@link BigQuery#update(TableInfo, TableOption...)}. */
  public TableCow update(TableInfo tableInfo, TableOption... tableOptions) {
    return new TableCow(
            clientConfig,
            operationAnnotator.executeCowOperation(
                    CloudOperation.GOOGLE_UPDATE_BIGQUERY_TABLE,
                    () -> bigQuery.update(tableInfo, tableOptions),
                    () -> convert(tableInfo, tableOptions)));
  }

  /** See {@link BigQuery#delete(TableId)}. */
  public boolean delete(TableId tableId) {
    return operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_DELETE_BIGQUERY_TABLE,
            () -> bigQuery.delete(tableId),
            () -> convert(tableId));
  }

  /** See {@link BigQuery#getTable(TableId, TableOption...)}. */
  public TableCow getTable(TableId tableId, TableOption... tableOptions) {
    return new TableCow(
            clientConfig,
            operationAnnotator.executeCowOperation(
                    CloudOperation.GOOGLE_GET_BIGQUERY_TABLE,
                    () -> bigQuery.getTable(tableId, tableOptions),
                    () -> convert(tableId, tableOptions)));
  }

  /** See {@link BigQuery#getTable(String, String, TableOption...)}. */
  public TableCow getTable(String datasetId, String tableId, TableOption... tableOptions) {
    return getTable(TableId.of(datasetId, tableId), tableOptions);
  }

  /** See {@link BigQuery#listTables(DatasetId, TableListOption...)}. */
  public Page<TableCow> listTables(DatasetId datasetId, TableListOption... tableListOptions) {
    return new TableCowPageImpl(clientConfig, operationAnnotator.executeCowOperation(
                    CloudOperation.GOOGLE_LIST_BIGQUERY_TABLE,
                    () -> bigQuery.listTables(datasetId, tableListOptions),
                    () -> convert(datasetId, tableListOptions)));
  }

  /** See {@link BigQuery#listTables(String, TableListOption...)}. */
  public Page<TableCow> listTables(String datasetId, TableListOption... tableListOptions) {
    return listTables(DatasetId.of(datasetId), tableListOptions);
  }

  /** See {@link BigQuery#listTableData(TableId, TableDataListOption...)}. */
  public TableResult listTableData(TableId tableId, TableDataListOption... tableDataListOptions) {
    return operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_LIST_TABLE_BIGQUERY_DATA,
            () -> bigQuery.listTableData(tableId, tableDataListOptions),
            () -> convert(tableId, tableDataListOptions));
  }

  /** See {@link BigQuery#listTableData(String, String, TableDataListOption...)}. */
  public TableResult listTableData(String datasetId, String tableId, TableDataListOption... tableDataListOptions) {
    return listTableData(TableId.of(datasetId, tableId), tableDataListOptions);
  }

  /** See {@link BigQuery#listTableData(TableId, Schema, TableDataListOption...)}. */
  public TableResult listTableData(TableId tableId, Schema schema, TableDataListOption... tableDataListOptions) {
    return operationAnnotator.executeCowOperation(
            CloudOperation.GOOGLE_LIST_TABLE_BIGQUERY_DATA,
            () -> bigQuery.listTableData(tableId, schema, tableDataListOptions),
            () -> convert(tableId, schema, tableDataListOptions));
  }

  /** See {@link BigQuery#listTableData(String, String, Schema, TableDataListOption...)}. */
  public TableResult listTableData(String datasetId, String tableId, Schema schema, TableDataListOption... tableDataListOptions) {
    return listTableData(TableId.of(datasetId, tableId), schema, tableDataListOptions);
  }

  public static class TableCowPageImpl extends CowPageImpl<Table, TableCow> {

    public TableCowPageImpl(ClientConfig clientConfig, Page<Table> originalPage) {
      super(clientConfig, originalPage);
    }

    @Override
    protected Function<Table, TableCow> getTransformFunction() {
      return table -> new TableCow(getClientConfig(), table);
    }

    @Override
    public Page<TableCow> getNextPage() {
      return new TableCowPageImpl(getClientConfig(), getOriginalPage());
    }
  }
}
