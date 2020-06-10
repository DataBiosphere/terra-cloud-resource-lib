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

  static JsonObject convert(TableId tableId, BigQuery.TableDataListOption... tableDataListOption) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId", gson.toJsonTree(tableId));
    jsonObject.add("tableDataListOption", gson.toJsonTree(tableDataListOption));
    return jsonObject;
  }

  static JsonObject convert(
      TableId tableId, Schema schema, BigQuery.TableDataListOption... tableDataListOption) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId", gson.toJsonTree(tableId));
    jsonObject.add("schema", gson.toJsonTree(schema));
    jsonObject.add("tableDataListOption", gson.toJsonTree(tableDataListOption));
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
}
