package bio.terra.cloudres.google.dataproc.testing;

import bio.terra.cloudres.google.cloudresourcemanager.CloudResourceManagerCow;
import bio.terra.cloudres.google.iam.IamCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.v3.model.Binding;
import com.google.api.services.cloudresourcemanager.v3.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.v3.model.Policy;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.cloudresourcemanager.v3.model.SetIamPolicyRequest;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DataprocUtils {

  private static CloudResourceManagerCow defaultManager()
      throws GeneralSecurityException, IOException {
    return CloudResourceManagerCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  private static IamCow defaultIam() throws GeneralSecurityException, IOException {
    return IamCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  /* Create a service account in the project. */
  public static ServiceAccount createServiceAccount(Project project, String serviceAccountName)
      throws IOException, GeneralSecurityException {
    CreateServiceAccountRequest createRequest = new CreateServiceAccountRequest();
    createRequest.setAccountId(serviceAccountName);
    createRequest.setServiceAccount(new ServiceAccount().setDisplayName(serviceAccountName));

    return defaultIam()
        .projects()
        .serviceAccounts()
        .create(String.format("projects/%s", project.getProjectId()), createRequest)
        .execute();
  }

  /**
   * Grant the Dataproc Worker role to the default compute engine service account. This is needed to
   * allow dataproc cluster creation.
   *
   * <p>See
   * https://cloud.google.com/dataproc/docs/concepts/configuring-clusters/service-accounts#dataproc_service_accounts_2.
   */
  public static void grantDataprocWorkerRole(Project project, ServiceAccount serviceAccount) {
    try {
      CloudResourceManagerCow crmCow = defaultManager();

      String projectNumber =
          Arrays.stream(project.getName().split("/")).reduce((first, second) -> second).orElse("0");

      String projectResourceName = String.format("projects/%s", project.getProjectId());

      Policy projectPolicy =
          crmCow.projects().getIamPolicy(projectResourceName, new GetIamPolicyRequest()).execute();

      // Create a new binding with the Dataproc worker role
      Binding workerBinding = new Binding();
      workerBinding.setRole("roles/dataproc.worker");
      workerBinding.setMembers(List.of("serviceAccount:" + serviceAccount.getEmail()));

      // Add the new binding to the existing project policy
      List<Binding> bindings =
          Optional.ofNullable(projectPolicy.getBindings()).orElseGet(ArrayList::new);
      bindings.add(workerBinding);
      projectPolicy.setBindings(bindings);

      // Set the new policy on the project
      SetIamPolicyRequest setIamPolicyRequest = new SetIamPolicyRequest();
      setIamPolicyRequest.setPolicy(projectPolicy);
      defaultManager().projects().setIamPolicy(projectResourceName, setIamPolicyRequest).execute();

      System.out.println("Dataproc worker role added to the service account successfully.");
    } catch (IOException | GeneralSecurityException e) {
      System.err.println("Error granting dataproc worker permissions: " + e.getMessage());
    }
  }
}
