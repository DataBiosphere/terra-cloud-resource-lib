package bio.terra.cloudres.google.billing;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.ServiceUsageCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.serviceusage.v1.model.BatchEnableServicesRequest;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudBillingClientCowTest {
  private static final String BILLING_SERVICE_ID = "cloudbilling.googleapis.com";
  // TODO(PF-67): Find solution for piping configs and secrets.
  private static final String BILLING_ACCOUNT_NAME = "billingAccounts/01A82E-CA8A14-367457";

  private static CloudBillingClientCow defaultBillingCow() throws IOException {
    return new CloudBillingClientCow(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  private static ServiceUsageCow defaultServiceUsage()
      throws GeneralSecurityException, IOException {
    return ServiceUsageCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  @Test
  public void getSetProjectBillingInfo() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    executeEnableBilling(project.getProjectId());

    try (CloudBillingClientCow billingCow = defaultBillingCow()) {
      ProjectBillingInfo initialBilling =
          billingCow.getProjectBillingInfo("projects/" + project.getProjectId());
      assertEquals(project.getProjectId(), initialBilling.getProjectId());
      assertEquals("", initialBilling.getBillingAccountName());

      ProjectBillingInfo setBilling =
          ProjectBillingInfo.newBuilder().setBillingAccountName(BILLING_ACCOUNT_NAME).build();
      ProjectBillingInfo updatedBilling =
          billingCow.updateProjectBillingInfo("projects/" + project.getProjectId(), setBilling);
      assertEquals(project.getProjectId(), updatedBilling.getProjectId());
      assertEquals(BILLING_ACCOUNT_NAME, updatedBilling.getBillingAccountName());
    }

    ProjectUtils.getManagerCow().projects().delete(project.getProjectId());
  }

  /** Enables the cloud billing service on the project. */
  private static void executeEnableBilling(String projectId)
      throws GeneralSecurityException, IOException, InterruptedException {
    ServiceUsageCow serviceUsageCow = defaultServiceUsage();
    OperationCow<?> operation =
        serviceUsageCow
            .operations()
            .operationCow(
                serviceUsageCow
                    .services()
                    .batchEnable(
                        "projects/" + projectId,
                        new BatchEnableServicesRequest()
                            .setServiceIds(ImmutableList.of(BILLING_SERVICE_ID)))
                    .execute());
    operation =
        OperationUtils.pollUntilComplete(operation, Duration.ofSeconds(5), Duration.ofSeconds(60));
    assertTrue(operation.getOperationAdapter().getDone());
  }
}
