package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.billing.v1.CloudBillingClient;
import com.google.cloud.billing.v1.CloudBillingSettings;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import com.google.iam.v1.TestIamPermissionsRequest;
import com.google.iam.v1.TestIamPermissionsResponse;
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
        CloudBillingOperation.GOOGLE_GET_PROJECT_BILLING,
        () -> billing.getProjectBillingInfo(name),
        () -> SerializeBillingUtils.convert(name));
  }

  /** See {@link CloudBillingClient#updateProjectBillingInfo(String, ProjectBillingInfo)} */
  public ProjectBillingInfo updateProjectBillingInfo(
      String name, ProjectBillingInfo projectBillingInfo) {
    return operationAnnotator.executeCowOperation(
        CloudBillingOperation.GOOGLE_UPDATE_PROJECT_BILLING,
        () -> billing.updateProjectBillingInfo(name, projectBillingInfo),
        () -> SerializeBillingUtils.convert(name, projectBillingInfo));
  }

  /** See {@link CloudBillingClient#testIamPermissions(TestIamPermissionsRequest)} */
  public TestIamPermissionsResponse testIamPermissions(TestIamPermissionsRequest request) {
    return operationAnnotator.executeCowOperation(
        CloudBillingOperation.GOOGLE_TEST_IAM_PERMISSIONS,
        () -> billing.testIamPermissions(request),
        () -> SerializeBillingUtils.convert(request));
  }

  @Override
  public void close() {
    billing.close();
  }
}
