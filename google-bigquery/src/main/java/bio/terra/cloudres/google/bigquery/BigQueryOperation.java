package bio.terra.cloudres.google.bigquery;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Google BigQuery API. */
public enum BigQueryOperation implements CloudOperation {
  GOOGLE_CREATE_DATASET,
  GOOGLE_CREATE_BIGQUERY_TABLE,
  GOOGLE_INSERT_BIGQUERY_TABLE,
  GOOGLE_UPDATE_DATASET,
  GOOGLE_UPDATE_BIGQUERY_TABLE,
  GOOGLE_DELETE_DATASET,
  GOOGLE_DELETE_BIGQUERY_TABLE,
  GOOGLE_GET_DATASET,
  GOOGLE_GET_BIGQUERY_TABLE,
  GOOGLE_LIST_BIGQUERY_TABLE,
  GOOGLE_QUERY_BIGQUERY_TABLE,
  GOOGLE_RELOAD_DATASET,
  GOOGLE_RELOAD_BIGQUERY_TABLE;

  @Override
  public String getName() {
    return this.name();
  }
}
