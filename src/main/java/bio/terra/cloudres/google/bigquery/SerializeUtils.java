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

  static JsonObject convert(String datasetId, BigQuery.DatasetOption... options) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("datasetId", datasetId);
    jsonObject.add("datasetOptions", new Gson().toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(String datasetId, BigQuery.DatasetDeleteOption... options) {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("datasetId", datasetId);
    jsonObject.addProperty("datasetDeleteOptions", new Gson().toJson(options));
    return jsonObject;
  }

  static JsonObject convert(TableInfo tableInfo, BigQuery.TableOption... options) {
    JsonObject jsonObject = new JsonObject();
    Gson gson = new Gson();
    jsonObject.add("tableInfo",  gson.toJsonTree(tableInfo));
    jsonObject.add("TableDeleteOptions", gson.toJsonTree(options));
    return jsonObject;
  }

  static JsonObject convert(TableId tablaId) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tablaId",  gson.toJsonTree(tablaId));
    return jsonObject;
  }

  static JsonObject convert(TableId tablaId, BigQuery.TableOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tablaId",  gson.toJsonTree(tablaId));
    jsonObject.addProperty("tableOptions", gson.toJson(options));
    return jsonObject;
  }


  static JsonObject convert(String datasetId, BigQuery.TableListOption... tableListOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("datasetId",  datasetId);
    jsonObject.addProperty("tableListOptions", gson.toJson(tableListOptions));
    return jsonObject;
  }

  static JsonObject convert(DatasetId datasetId, BigQuery.TableListOption... tableListOptions) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetId",  gson.toJsonTree(datasetId));
    jsonObject.addProperty("tableListOptions", gson.toJson(tableListOptions));
    return jsonObject;
  }

  static JsonObject convert(TableId tableId, BigQuery.TableDataListOption... tableDataListOption) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId",  gson.toJsonTree(tableId));
    jsonObject.addProperty("tableDataListOption", gson.toJson(tableDataListOption));
    return jsonObject;
  }

  static JsonObject convert(TableId tableId, Schema schema, BigQuery.TableDataListOption... tableDataListOption) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("tableId",  gson.toJsonTree(tableId));
    jsonObject.addProperty("schema", gson.toJson(schema));
    jsonObject.addProperty("tableDataListOption", gson.toJson(tableDataListOption));
    return jsonObject;
  }
}
