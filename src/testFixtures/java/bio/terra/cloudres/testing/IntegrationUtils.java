package bio.terra.cloudres.testing;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupConfig;
import bio.terra.cloudres.google.cloudresourcemanager.CloudResourceManagerCow;
import bio.terra.cloudres.google.iam.IamCow;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.cloudresourcemanager.v3.model.Binding;
import com.google.api.services.cloudresourcemanager.v3.model.GetIamPolicyRequest;
import com.google.api.services.cloudresourcemanager.v3.model.Policy;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.cloudresourcemanager.v3.model.SetIamPolicyRequest;
import com.google.api.services.iam.v1.model.CreateServiceAccountRequest;
import com.google.api.services.iam.v1.model.ServiceAccount;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Utilities for integration tests. */
public class IntegrationUtils {
  private IntegrationUtils() {}

  public static final String DEFAULT_CLIENT_NAME = "crl-integration-test";

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

  // TODO(CA-874): Consider setting per-integration run environment variable for the cleanup id.
  // TODO(yonghao): Figure a better config pulling solution to replace the hardcoded configs.
  public static final CleanupConfig DEFAULT_CLEANUP_CONFIG =
      CleanupConfig.builder()
          .setTimeToLive(Duration.ofHours(2))
          .setCleanupId("crl-integration")
          .setCredentials(IntegrationCredentials.getJanitorClientGoogleCredentialsOrDie())
          .setJanitorTopicName("crljanitor-tools-pubsub-topic")
          .setJanitorProjectId("terra-kernel-k8s")
          .build();

  public static final ClientConfig DEFAULT_CLIENT_CONFIG =
      ClientConfig.Builder.newBuilder()
          .setClient(DEFAULT_CLIENT_NAME)
          .setCleanupConfig(DEFAULT_CLEANUP_CONFIG)
          .build();

  /** Generates a random name to use for a cloud resource. */
  public static String randomName() {
    return UUID.randomUUID().toString();
  }

  /** Generates a random name to and replace '-' with '_'. */
  public static String randomNameWithUnderscore() {
    return UUID.randomUUID().toString().replace('-', '_');
  }

  /**
   * Sets longer timeout because some operation(e.g. Dns.ManagedZones.Create) may take longer than
   * default timeout. We pass a {@link HttpRequestInitializer} to accept a requestInitializer to
   * allow chaining, since API clients have exactly one initializer and credentials are typically
   * required as well.
   */
  public static HttpRequestInitializer setHttpTimeout(
      final HttpRequestInitializer requestInitializer) {
    return httpRequest -> {
      requestInitializer.initialize(httpRequest);
      httpRequest.setConnectTimeout(5 * 60000); // 5 minutes connect timeout
      httpRequest.setReadTimeout(5 * 60000); // 5 minutes read timeout
    };
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
   * Grant a role to a provided service account.
   *
   * <p>See
   * https://cloud.google.com/dataproc/docs/concepts/configuring-clusters/service-accounts#dataproc_service_accounts_2.
   */
  public static void grantServiceAccountRole(Project project, ServiceAccount serviceAccount, String role) {
    try {
      CloudResourceManagerCow crmCow = defaultManager();

      String projectNumber =
          Arrays.stream(project.getName().split("/")).reduce((first, second) -> second).orElse("0");

      String projectResourceName = String.format("projects/%s", project.getProjectId());

      Policy projectPolicy =
          crmCow.projects().getIamPolicy(projectResourceName, new GetIamPolicyRequest()).execute();

      // Create a new binding with the provided role
      Binding workerBinding = new Binding();
      workerBinding.setRole(role);
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
    } catch (IOException | GeneralSecurityException e) {
      System.err.println("Error granting dataproc worker permissions: " + e.getMessage());
    }
  }
}
