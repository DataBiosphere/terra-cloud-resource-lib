package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.billing.v1.CloudBillingClient;
import com.google.cloud.billing.v1.CloudBillingSettings;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Cloud Object Wrapper (COW) for {@link CloudBillingClient}.
 *
 * <p>Because this contains a {@link CloudBillingClient}, it needs to be closed to cleanup helper
 * threads.
 */
public class CloudBillingClientCow implements AutoCloseable {
  private final Logger logger = LoggerFactory.getLogger(CloudBillingClientCow.class);

  private final OperationAnnotator operationAnnotator;
  private final CloudBillingClient billing;

  public CloudBillingClientCow(ClientConfig clientConfig, GoogleCredentials credentials)
      throws IOException {
    this(
        clientConfig,
        CloudBillingSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .build());
  }

  public CloudBillingClientCow(ClientConfig clientConfig, CloudBillingSettings settings)
      throws IOException {
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.billing = CloudBillingClient.create(settings);
  }

  /** See {@link CloudBillingClient#getProjectBillingInfo(String)}. */
  public ProjectBillingInfo getProjectBillingInfo(String name) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_GET_PROJECT_BILLING,
        () -> billing.getProjectBillingInfo(name),
        () -> serializeProjectName(name));
  }

  /** See {@link CloudBillingClient#updateProjectBillingInfo(String, ProjectBillingInfo)} */
  public ProjectBillingInfo updateProjectBillingInfo(
      String name, ProjectBillingInfo projectBillingInfo) {
    return operationAnnotator.executeCowOperation(
        CloudOperation.GOOGLE_UPDATE_PROJECT_BILLING,
        () -> billing.updateProjectBillingInfo(name, projectBillingInfo),
        () -> serialize(name, projectBillingInfo));
  }

  @VisibleForTesting
  static JsonObject serializeProjectName(String name) {
    JsonObject result = new JsonObject();
    result.addProperty("project_name", name);
    return result;
  }

  @VisibleForTesting
  static JsonObject serialize(String projectName, ProjectBillingInfo projectBillingInfo) {
    JsonObject result = serializeProjectName(projectName);
    result.add("project_billing_info", new Gson().toJsonTree(projectBillingInfo));
    return result;
  }

  @Override
  public void close() {
    billing.close();
  }
}
