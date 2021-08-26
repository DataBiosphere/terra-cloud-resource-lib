package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.cloudres.azure.resourcemanager.common.ApplicationSecretCredentials;
import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Cloud Object Wrapper(COW) for Azure Resource Manager client library: {@link ResourceManager}
 *
 * <p>TODO: integrate with Janitor
 */
public class ResourceManagerCow {
  private final Logger logger = LoggerFactory.getLogger(ResourceManagerCow.class);

  private final ClientConfig clientConfig;
  private final ResourceManager client;
  private final OperationAnnotator operationAnnotator;

  private ResourceManagerCow(ClientConfig clientConfig, ResourceManager client) {
    this.clientConfig = clientConfig;
    this.client = client;
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
  }

  /**
   * Creates a ResourceManagerCow.
   *
   * @param clientConfig CRL configuration
   * @param profile Azure profile
   * @param credentials Azure application credentials
   * @return ResourceManagerCow
   */
  public static ResourceManagerCow create(
      ClientConfig clientConfig, AzureProfile profile, ApplicationSecretCredentials credentials) {
    return new ResourceManagerCow(
        clientConfig,
        ResourceManager.authenticate(credentials.getTokenCredential(), profile)
            .withSubscription(profile.getSubscriptionId()));
  }

  /**
   * Deploys an ARM template in the given resource group. This method blocks until the deployment is
   * complete.
   *
   * @param resourceGroup name of the resource group in which to deploy
   * @param deploymentName name of the deployment
   * @param template template JSON
   * @param parameters deployment parameters
   * @param deploymentMode deployment mode -- COMPLETE or INCREMENTAL
   * @return Deployment completed Deployment object
   * @throws IOException if there was an error parsing the template JSON
   */
  public Deployment deployTemplate(
      String resourceGroup,
      String template,
      String deploymentName,
      Map<String, String> parameters,
      DeploymentMode deploymentMode)
      throws IOException {
    return operationAnnotator.executeCheckedCowOperation(
        ResourceManagerOperation.AZURE_RESOURCE_MANAGER_CREATE_DEPLOYMENT,
        () ->
            client
                .deployments()
                .define(deploymentName)
                .withExistingResourceGroup(resourceGroup)
                .withTemplate(template)
                .withParameters(
                    parameters.entrySet().stream()
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey, e -> Map.of("value", e.getValue()))))
                .withMode(deploymentMode)
                .create(),
        () -> serializeDeployment(resourceGroup, deploymentName));
  }

  /**
   * Deploys an ARM template in the given resource group. This method returns immediately after the
   * deployment is started. The caller should poll the returned Deployment object for completion or
   * error.
   *
   * @param resourceGroup name of the resource group in which to deploy
   * @param deploymentName name of the deployment
   * @param template template JSON
   * @param parameters deployment parameters
   * @param deploymentMode deployment mode -- COMPLETE or INCREMENTAL
   * @return Deployment long running operation which can be polled for checking status
   * @throws IOException if there was an error parsing the template JSON
   */
  public Accepted<Deployment> beginDeployTemplate(
      String resourceGroup,
      String deploymentName,
      String template,
      Map<String, String> parameters,
      DeploymentMode deploymentMode)
      throws IOException {
    return operationAnnotator.executeCheckedCowOperation(
        ResourceManagerOperation.AZURE_RESOURCE_MANAGER_CREATE_DEPLOYMENT,
        () ->
            client
                .deployments()
                .define(deploymentName)
                .withExistingResourceGroup(resourceGroup)
                .withTemplate(template)
                .withParameters(
                    parameters.entrySet().stream()
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey, e -> Map.of("value", e.getValue()))))
                .withMode(deploymentMode)
                .beginCreate(),
        () -> serializeDeployment(resourceGroup, deploymentName));
  }

  /**
   * Deletes a given deployment. Note: this does not delete any resources under the deployment, just
   * the deployment object itself.
   *
   * @param resourceGroup name of the resource group in which to delete the deployment
   * @param deploymentName name of the deployment to delete
   */
  public void deleteDeployment(String resourceGroup, String deploymentName) {
    operationAnnotator.executeCowOperation(
        ResourceManagerOperation.AZURE_RESOURCE_MANAGER_DELETE_DEPLOYMENT,
        () -> {
          client.deployments().deleteByResourceGroup(resourceGroup, deploymentName);
          return null;
        },
        () -> serializeDeployment(resourceGroup, deploymentName));
  }

  @VisibleForTesting
  static JsonObject serializeDeployment(String resourceGroup, String deploymentName) {
    JsonObject result = new JsonObject();
    result.addProperty("resourceGroup", resourceGroup);
    result.addProperty("deploymentName", deploymentName);
    return result;
  }
}
