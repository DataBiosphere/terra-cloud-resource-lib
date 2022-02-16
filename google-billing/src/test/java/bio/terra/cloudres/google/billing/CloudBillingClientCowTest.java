package bio.terra.cloudres.google.billing;

import static bio.terra.cloudres.google.billing.testing.CloudBillingUtils.BILLING_ACCOUNT_NAME;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.ServiceUsageCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.cloud.billing.v1.ProjectBillingInfo;
import com.google.iam.v1.TestIamPermissionsRequest;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudBillingClientCowTest {
  private static final String BILLING_SERVICE_ID = "cloudbilling.googleapis.com";

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

  @Test
  public void testIamPermissions() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    try (CloudBillingClientCow billingCow = defaultBillingCow()) {
      ProjectBillingInfo initialBilling =
          billingCow.getProjectBillingInfo("projects/" + project.getProjectId());
      var permissions = List.of("billing.resourceAssociations.create");
      var request =
          TestIamPermissionsRequest.newBuilder()
              .setResource(initialBilling.getBillingAccountName())
              .addAllPermissions(permissions)
              .build();
      var response = billingCow.testIamPermissions(request);
      assertEquals(permissions, response.getPermissionsList());
    }

    ProjectUtils.getManagerCow().projects().delete(project.getProjectId());
  }

  @Test
  public void serializeProjectName() {
    assertEquals(
        "{\"project_name\":\"projects/my-project\"}",
        CloudBillingClientCow.serializeProjectName("projects/my-project").toString());
  }

  @Test
  public void serialize() {
    assertEquals(
        "{\"project_name\":\"projects/my-project\","
            + "\"project_billing_info\":{\"name_\":\"\",\"projectId_\":\"my-project\","
            + "\"billingAccountName_\":\"billingAccounts/01A82E-CA8A14-367457\",\"billingEnabled_\":false,"
            + "\"memoizedIsInitialized\":1,\"unknownFields\":{\"fields\":{},\"fieldsDescending\":{}},"
            + "\"memoizedSize\":-1,\"memoizedHashCode\":0}}",
        CloudBillingClientCow.serialize(
                "projects/my-project",
                ProjectBillingInfo.newBuilder()
                    .setProjectId("my-project")
                    .setBillingAccountName(BILLING_ACCOUNT_NAME)
                    .build())
            .toString());
  }

  @Test
  public void serializeTestIamPermissions() {
    assertEquals(
        "{\"resource\":\"billingAccounts/01A82E-CA8A14-367457\","
            + "\"permissions\":[\"billing.resourceAssociations.create\"]}",
        CloudBillingClientCow.serializeTestIamPermissions(
                TestIamPermissionsRequest.newBuilder()
                    .setResource("billingAccounts/01A82E-CA8A14-367457")
                    .addPermissions("billing.resourceAssociations.create")
                    .build())
            .toString());
  }
}
