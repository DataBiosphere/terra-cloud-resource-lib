package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.util.SerializeHelper;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.iam.v1.TestIamPermissionsRequest;

/** Utils for serializing {@link com.google.cloud.storage} objects. */
public class SerializeBillingUtils extends SerializeHelper {

  private SerializeBillingUtils() {}

  static JsonObject convert(String projectName) {
    JsonObject result = new JsonObject();
    result.addProperty("project_name", projectName);
    return result;
  }

  static JsonObject convert(String projectName, ProjectBillingInfo projectBillingInfo) {
    JsonObject result = convert(projectName);
    Gson gson = createGson();
    result.add("project_billing_info", gson.toJsonTree(projectBillingInfo));
    return result;
  }

  static JsonObject convert(TestIamPermissionsRequest request) {
    JsonObject result = new JsonObject();
    result.addProperty("resource", request.getResource());
    JsonArray permissions = new JsonArray();
    request.getPermissionsList().forEach(permissions::add);
    result.add("permissions", permissions);
    return result;
  }

}
