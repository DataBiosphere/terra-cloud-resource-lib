package bio.terra.cloudres.google.bigquery;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Utils for serializing {@link com.google.cloud.bigquery} objects. */
public class SerializeUtils {
  static JsonObject convert(DatasetInfo datasetInfo, BigQuery.DatasetOption... options) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add("datasetId", gson.toJsonTree(datasetInfo));
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
}
