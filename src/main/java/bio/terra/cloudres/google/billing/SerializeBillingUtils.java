package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.util.SerializeHelper;
import com.google.cloud.billing.v1.ProjectBillingInfo;
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
    JsonObject billingInfo = new JsonObject();
    billingInfo.addProperty("name_", projectName);
    billingInfo.addProperty("projectId_", projectBillingInfo.getProjectId());
    billingInfo.addProperty("billingAccountName_", projectBillingInfo.getBillingAccountName());
    billingInfo.addProperty("billingEnabled_", projectBillingInfo.getBillingEnabled());
    result.add("project_billing_info", billingInfo);
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
