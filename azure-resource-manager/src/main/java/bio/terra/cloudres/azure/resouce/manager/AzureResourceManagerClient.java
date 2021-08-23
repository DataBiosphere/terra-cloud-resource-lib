package bio.terra.cloudres.azure.resouce.manager;

import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Client for working with Azure Resource Manager (ARM).
 */
public class AzureResourceManagerClient {
    private final Logger logger = LoggerFactory.getLogger(AzureResourceManagerClient.class);

    private final AzureResourceConfiguration azureResourceConfiguration;

    public AzureResourceManagerClient(AzureResourceConfiguration azureResourceConfiguration) {
        this.azureResourceConfiguration = azureResourceConfiguration;
    }

    /**
     * Deploys an ARM template in a user's tenant. This method blocks until the deployment
     * is complete.
     *
     * @param tenantId ID of the user's tenant
     * @param subscriptionId ID of the subscription that will be charged for the resources created
     *     with this client
     * @param resourceGroup name of the resource group in which to deploy
     * @param template template JSON
     * @param deploymentName name of the deployment
     * @param parameters deployment parameters
     * @param deploymentMode deployment mode -- COMPLETE or INCREMENTAL
     * @return Deployment completed Deployment object
     * @throws IOException if there was an error parsing the template JSON
     */
    public Deployment deployTemplate(
            UUID tenantId,
            UUID subscriptionId,
            String resourceGroup,
            String template,
            String deploymentName,
            Map<String, String> parameters,
            DeploymentMode deploymentMode
    ) throws IOException {
        AzureResourceManager client = azureResourceConfiguration.getClient(tenantId, subscriptionId);

        return client.deployments()
                .define(deploymentName)
                .withExistingResourceGroup(resourceGroup)
                .withTemplate(template)
                .withParameters(parameters.entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, e -> Map.of("value", e.getValue()))))
                .withMode(deploymentMode)
                .create();
    }

    /**
     * Deploys an ARM template in a user's tenant. This method returns after the deployment
     * is started. The caller should poll the returned Deployment object for completion or error.
     *
     * @param tenantId ID of the user's tenant
     * @param subscriptionId ID of the subscription that will be charged for the resources created
     *     with this client
     * @param resourceGroup name of the resource group in which to deploy
     * @param template template JSON
     * @param deploymentName name of the deployment
     * @param parameters deployment parameters
     * @param deploymentMode deployment mode -- COMPLETE or INCREMENTAL
     * @return Deployment long running operation which can be polled for checking status
     * @throws IOException if there was an error parsing the template JSON
     */
    public Accepted<Deployment> beginDeployTemplate(
            UUID tenantId,
            UUID subscriptionId,
            String resourceGroup,
            String template,
            String deploymentName,
            Map<String, String> parameters,
            DeploymentMode deploymentMode
    ) throws IOException {
        AzureResourceManager client = azureResourceConfiguration.getClient(tenantId, subscriptionId);

        return client.deployments()
                .define(deploymentName)
                .withExistingResourceGroup(resourceGroup)
                .withTemplate(template)
                .withParameters(parameters.entrySet().stream()
                        .collect(
                                Collectors.toMap(Map.Entry::getKey, e -> Map.of("value", e.getValue()))))
                .withMode(deploymentMode)
                .beginCreate();
    }

    /**
     * Deletes a given deployment. Note: this does not delete any resources under the deployment,
     * just the deployment object itself.
     * @param tenantId ID of the user's tenant
     * @param subscriptionId ID of the user's subscription
     * @param deploymentId ID of the deployment to delete
     */
    public void deleteDeployment(
            UUID tenantId,
            UUID subscriptionId,
            String deploymentId) {
        AzureResourceManager client = azureResourceConfiguration.getClient(tenantId, subscriptionId);

        client.deployments().deleteById(deploymentId);
    }
}
