package bio.terra.cloudres.google.bigquery;

import static bio.terra.cloudres.google.bigquery.SerializeUtils.convert;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for {@link Table}. */
public class TableCow {
  private final Logger logger = LoggerFactory.getLogger(TableCow.class);

  private final OperationAnnotator operationAnnotator;
  private final Table table;
  private final ClientConfig clientConfig;

  TableCow(ClientConfig clientConfig, Table table) {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.table = table;
    this.clientConfig = clientConfig;
  }

  public TableInfo getTableInfo() {
    return table;
  }

  /** See {@link Table#reload(BigQuery.TableOption...)} */
  public TableCow reload(BigQuery.TableOption... tableOptions) {
    return new TableCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            BigQueryOperation.GOOGLE_RELOAD_BIGQUERY_TABLE,
            () -> table.reload(tableOptions),
            () -> convert(table.getTableId(), tableOptions)));
  }

  /** See {@link Table#update(BigQuery.TableOption...)}. */
  public TableCow update(BigQuery.TableOption... TableOptions) {
    return new TableCow(
        clientConfig,
        operationAnnotator.executeCowOperation(
            BigQueryOperation.GOOGLE_UPDATE_BIGQUERY_TABLE,
            () -> table.update(TableOptions),
            () -> convert(table, TableOptions)));
  }

  /** See {@link Table#exists()} */
  public boolean exists() {
    return operationAnnotator.executeCowOperation(
        BigQueryOperation.GOOGLE_DELETE_BIGQUERY_TABLE,
        table::exists,
        () -> convert(table.getTableId()));
  }

  /** See {@link Table#delete()} */
  public boolean delete() {
    return operationAnnotator.executeCowOperation(
        BigQueryOperation.GOOGLE_DELETE_BIGQUERY_TABLE,
        table::delete,
        () -> convert(table.getTableId()));
  }
}
