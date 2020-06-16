package bio.terra.cloudres.google.bigquery;

import com.google.cloud.bigquery.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Utils for serializing {@link com.google.cloud.bigquery} objects. */
public class SerializeUtils {
  static JsonObject convert(DatasetInfo datasetInfo, BigQuery.DatasetOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetInfo", gson.toJsonTree(datasetInfo));
    jsonObject.add("datasetOptions", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(DatasetId datasetId, BigQuery.DatasetOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetId", gson.toJsonTree(datasetId));
    jsonObject.add("datasetOptions", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(DatasetId datasetId, BigQuery.DatasetDeleteOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetId", gson.toJsonTree(datasetId));
    jsonObject.add("datasetDeleteOptions", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(TableInfo tableInfo, BigQuery.TableOption... options) {
    JsonObject jsonObject = new JsonObject();
    Gson gson = new Gson();
    jsonObject.add("tableInfo", gson.toJsonTree(tableInfo));
    jsonObject.add("tableOptions", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(TableId tableId) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId", gson.toJsonTree(tableId));
    return jsonObject;
  }

  static JsonObject convert(TableId tableId, BigQuery.TableOption... tableOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId", gson.toJsonTree(tableId));
    jsonObject.add("tableOptions", gson.toJsonTree(tableOptions));
    return jsonObject;
  }

  static JsonObject convert(DatasetId datasetId, BigQuery.TableListOption... tableListOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetId", gson.toJsonTree(datasetId));
    jsonObject.add("tableListOptions", gson.toJsonTree(tableListOptions));
    return jsonObject;
  }

  static JsonObject convert(
      TableId tableId, TableDefinition tableDefinition, BigQuery.TableOption... tableOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId", gson.toJsonTree(tableId));
    jsonObject.add("tableDefinition", gson.toJsonTree(tableDefinition));
    jsonObject.add("tableOptions", gson.toJsonTree(tableOptions));
    return jsonObject;
  }

  static JsonObject convert(
      QueryJobConfiguration queryJobConfiguration, BigQuery.JobOption... jobOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("queryJobConfiguration", gson.toJsonTree(queryJobConfiguration));
    jsonObject.add("jobOptions", gson.toJsonTree(jobOptions));
    return jsonObject;
  }

  static JsonObject convert(
      QueryJobConfiguration queryJobConfiguration, JobId jobId, BigQuery.JobOption... jobOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("queryJobConfiguration", gson.toJsonTree(queryJobConfiguration));
    jsonObject.add("jobId", gson.toJsonTree(jobId));
    jsonObject.add("jobOptions", gson.toJsonTree(jobOptions));
    return jsonObject;
  }

  static JsonObject convert(InsertAllRequest insertAllRequest) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("insertAllRequest", new Gson().toJsonTree(insertAllRequest));
    return jsonObject;
  }
}
