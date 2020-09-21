package bio.terra.cloudres.google.compute;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.ServiceUsageCow;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Operation;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudComputeCowTest {
  private static final String COMPUTE_SERVICE_ID = "compute.googleapis.com";

  // TODO(PF-67): Find solution for piping configs and secrets.
  private static final String BILLING_ACCOUNT_NAME = "billingAccounts/01A82E-CA8A14-367457";

  private ServiceUsageCow serviceUsageCow;

  private static CloudComputeCow defaultCompute() throws GeneralSecurityException, IOException {
    return CloudComputeCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  @Test
  public void createAndGetNetwork() throws Exception {
    Project project = createPreparedProject();

    CloudComputeCow cloudComputeCow = defaultCompute();

    String projectId = project.getProjectId();
    String netWorkName = randomNetworkName();
    Network network = new Network().setName(netWorkName).setAutoCreateSubnetworks(false);
    Operation operation = cloudComputeCow.networks().insert(projectId, network).execute();
    OperationCow<Operation> completedOperation =
        OperationUtils.pollUntilComplete(
            cloudComputeCow.globalOperations().operationCow(projectId, operation),
            Duration.ofSeconds(5),
            Duration.ofSeconds(100));
    assertTrue(completedOperation.getOperationAdapter().getDone());
    assertNull(completedOperation.getOperationAdapter().getError());

    Network createdNetwork = cloudComputeCow.networks().get(projectId, netWorkName).execute();

    assertEquals(netWorkName, createdNetwork.getName());
    assertFalse(createdNetwork.getAutoCreateSubnetworks());
  }

  @Test
  public void networkInsertSerialize() throws Exception {
    Network network = new Network().setName("network-name");
    CloudComputeCow.Networks.Insert insert =
        defaultCompute().networks().insert("project-id", network);

    assertEquals(
        "{\"project\":\"project-id\",\"network\":{\"name\":\"network-name\"}}",
        insert.serialize().toString());
  }

  @Test
  public void networkGetSerialize() throws Exception {
    CloudComputeCow.Networks.Get get =
        defaultCompute().networks().get("project-id", "network-name");

    assertEquals(
        "{\"project\":\"project-id\",\"network_name\":\"network-name\"}",
        get.serialize().toString());
  }

  /** Create Project then set billing account, enable compute compute service */
  private static Project createPreparedProject() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    CloudBillingUtils.setProjectBillingInfo(project.getProjectId(), BILLING_ACCOUNT_NAME);
    ServiceUsageUtils.enableServices(
        project.getProjectId(), ImmutableList.of(COMPUTE_SERVICE_ID));
    return project;
  }

  public static String randomNetworkName() {
    // Network name ids must start with a letter and be no more than 30 characters long.
    return "n" + IntegrationUtils.randomName().substring(0, 29);
  }
}
