package bio.terra.cloudres.azure.resource.manager;

import bio.terra.cloudres.azure.resouce.manager.AzureResourceConfiguration;
import bio.terra.cloudres.azure.resouce.manager.AzureResourceManagerClient;
import bio.terra.cloudres.azure.resouce.manager.SecretCredentials;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.DeploymentOperation;
import com.azure.resourcemanager.resources.models.TargetResource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@Tag("integration")
public class AzureResourceTest {
    private static final Logger logger = LoggerFactory.getLogger(AzureResourceTest.class);

    // TODO replace me
    private static final String APPLICATION_ID = "app-id";
    private static final String HOME_TENANT_ID = "home-tenant-id";
    private static final String SECRET = "secret";
    private static final String TENANT_ID = "tenant-id";
    private static final String SUBSCRIPTION_ID = "subscription-id";
    private static final String RESOURCE_GROUP = "mrg-name";

    private AzureResourceManagerClient client;

    @BeforeEach
    public void beforeEach() {
        client = new AzureResourceManagerClient(
                new AzureResourceConfiguration(
                        new SecretCredentials(
                                UUID.fromString(APPLICATION_ID),
                                UUID.fromString(HOME_TENANT_ID),
                                SECRET)));
    }

  @Test
  public void deployDsvmTemplate() throws IOException {
      final String template;
      try (InputStream stream = getClass().getClassLoader().getResourceAsStream("azuredeploy.json")) {
          template = IOUtils.toString(stream, StandardCharsets.UTF_8);
      } catch (IOException e) {
          throw new RuntimeException("Problem reading resource", e);
      }

      final String deploymentName = "testdeployment";

      // Deploy VM
      logger.info("Deploying VM...");
      Accepted<Deployment> deployment = client.beginDeployTemplate(
              UUID.fromString(TENANT_ID),
              UUID.fromString(SUBSCRIPTION_ID),
              RESOURCE_GROUP,
              template,
              deploymentName,
              Map.of("adminUsername", "terra", "authenticationType", "password", "adminPasswordOrKey", "terra123?"),
              DeploymentMode.COMPLETE);

      // Wait for deployment to finish
      logger.info("Waiting for deployment to finish...");
      SyncPoller<Void, Deployment> poller = deployment.getSyncPoller();
      PollResponse<Void> finalPollResponse = poller.waitForCompletion(Duration.ofMinutes(30));
      assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, finalPollResponse.getStatus());

      // Verify result
      Deployment finalResult = poller.getFinalResult();
      Set<String> resourceTypesDeployed = finalResult.deploymentOperations()
              .list()
              .stream()
              .flatMap(op -> Optional.ofNullable(op.targetResource()).map(TargetResource::resourceType).stream())
              .collect(Collectors.toSet());
      assertEquals(
              Set.of(
                  "Microsoft.Compute/virtualMachines/Extensions",
                  "Microsoft.Compute/virtualMachines",
                  "Microsoft.Network/networkInterfaces",
                  "Microsoft.Storage/storageAccounts",
                  "Microsoft.Network/publicIpAddresses",
                  "Microsoft.Network/virtualNetworks",
                  "Microsoft.Network/networkSecurityGroups"
              ), resourceTypesDeployed);
      assertEquals(
              Set.of("Succeeded"),
              finalResult.deploymentOperations()
                      .list()
                      .stream()
                      .map(DeploymentOperation::provisioningState)
                      .collect(Collectors.toSet()));

      final String deleteTemplate;
      try (InputStream stream = getClass().getClassLoader().getResourceAsStream("emptydeployment.json")) {
          deleteTemplate = IOUtils.toString(stream, StandardCharsets.UTF_8);
      } catch (IOException e) {
          throw new RuntimeException("Problem reading resource", e);
      }

      // Delete VM
      // Note: this deletes everything in the resource group
      logger.info("Deleting VM...");
      Accepted<Deployment> deleteDeployment = client.beginDeployTemplate(
              UUID.fromString(TENANT_ID),
              UUID.fromString(SUBSCRIPTION_ID),
              RESOURCE_GROUP,
              deleteTemplate,
              deploymentName,
              Map.of(),
              DeploymentMode.COMPLETE);

      // Wait for deletion to finish
      logger.info("Waiting for deletion to finish...");
      SyncPoller<Void, Deployment> deletePoller = deleteDeployment.getSyncPoller();
      PollResponse<Void> finalDeletePollResponse = deletePoller.waitForCompletion(Duration.ofMinutes(30));
      assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, finalDeletePollResponse.getStatus());

      // Finally delete the deployment
      logger.info("Deleting deployment " + deploymentName);
      client.deleteDeployment(
              UUID.fromString(TENANT_ID),
              UUID.fromString(SUBSCRIPTION_ID),
              deletePoller.getFinalResult().id()
      );
  }
}
