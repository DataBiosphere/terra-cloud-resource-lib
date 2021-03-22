package bio.terra.cloudres.google.billing.testing;

import bio.terra.cloudres.google.billing.CloudBillingClientCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.cloud.billing.v1.ProjectBillingInfo;

/** Testing utilities for cloud billings. */
public class CloudBillingUtils {
  private static CloudBillingClientCow billingClientCow;

  /** The name of the billing account to use for tests. The CRL test service account is expected to have permissions to set up billing for this account on projects. */
  // TODO(PF-67): Find solution for piping configs and secrets.
  public static final String BILLING_ACCOUNT_NAME = "billingAccounts/01A82E-CA8A14-367457";

  public static CloudBillingClientCow getBillingClientCow() throws Exception {
    if (billingClientCow == null) {
      billingClientCow =
          new CloudBillingClientCow(
              IntegrationUtils.DEFAULT_CLIENT_CONFIG,
              IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    }
    return billingClientCow;
  }

  /** Sets the projects billing account with the default billing used for test. */
  public static void setDefaultProjectBilling(String projectId) throws Exception {
    ProjectBillingInfo setBilling =
            ProjectBillingInfo.newBuilder().setBillingAccountName(BILLING_ACCOUNT_NAME).build();
    getBillingClientCow().updateProjectBillingInfo("projects/" + projectId, setBilling);
    // Sleep for 1s to make sure billing is ready.
    Thread.sleep(1000);
  }
}
