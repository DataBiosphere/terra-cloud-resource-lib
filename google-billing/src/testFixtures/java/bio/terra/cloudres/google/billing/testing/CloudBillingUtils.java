package bio.terra.cloudres.google.billing.testing;

import bio.terra.cloudres.google.billing.CloudBillingClientCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.billing.v1.ProjectBillingInfo;

/** Testing utilities for cloud billings. */
public class CloudBillingUtils {
  private static CloudBillingClientCow billingClientCow;

  public static CloudBillingClientCow getBillingClientCow() throws Exception {
    if (billingClientCow == null) {
      billingClientCow =
          new CloudBillingClientCow(
              IntegrationUtils.DEFAULT_CLIENT_CONFIG,
              IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    }
    return billingClientCow;
  }

  /** Sets project billing account for a project. */
  public static void setProjectBillingInfo(String projectId, String billingAccountName)
      throws Exception {
    ProjectBillingInfo setBilling =
        ProjectBillingInfo.newBuilder().setBillingAccountName(billingAccountName).build();
    getBillingClientCow().updateProjectBillingInfo("projects/" + projectId, setBilling);
  }
}
