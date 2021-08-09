package bio.terra.cloudres.azure.resouce.manager;

import com.azure.core.management.Region;
import com.azure.resourcemanager.*;
import com.azure.resourcemanager.resources.models.Deployment;
import com.azure.resourcemanager.resources.models.DeploymentMode;
import com.azure.resourcemanager.resources.models.ResourceReference;
import org.gradle.internal.impldep.org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AzureResourceManagerClient implements AutoCloseable {
    private final Logger logger = LoggerFactory.getLogger(AzureResourceManagerClient.class);

    AzureResourceConfiguration azureResourceConfiguration;
    BillingProfileModel profileModel;

    public AzureResourceManagerClient(AzureResourceConfiguration azureResourceConfiguration, BillingProfileModel profileModel) {
        this.azureResourceConfiguration = azureResourceConfiguration;
        this.profileModel = profileModel;
    }
    /**
     * Deploy a managed application in a subscription defined by the profileModel.
     *
     * <p>Note that this should be moved into a common lib
     */
    public ManagedApplicationDeployment createManagedApplication(String templatePath) {
        AzureResourceManager client = azureResourceConfiguration.getClient(
                UUID.fromString(profileModel.tenantId),
                UUID.fromString(profileModel.subscriptionId));

        String deploymentName = "tdr" + getShortUUID();
        String rgId =
                "/subscriptions/" + profileModel.subscriptionId + "/resourceGroups/" + deploymentName;
        //TODO
        Map<String, Object> parameters =
                Map.of(
                        "storageAccountNamePrefix",
                        "tdr1",
                        "storageAccountType",
                        "Standard_LRS",
                        "location",
                        Region.US_CENTRAL,
                        "applicationResourceName",
                        deploymentName,
                        "managedResourceGroupId",
                        rgId);

        final String template;
        try (InputStream stream =
                     getClass()
                         .getClassLoader()
                         .getResourceAsStream(templatePath)) {
            template = IOUtils.toString(stream);
        } catch (IOException e) {
            throw new RuntimeException("Problem reading resource", e);
        }

        // TODO, right now this is a manual process but there is a rest endpoint to do programatically
        // accept terms.
        // This should likely be implemented in the Azure SDK (see:
        // https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/marketplaceordering/  \
        // mgmt-v2015_06_01/src/main/java/com/microsoft/azure/management/marketplaceordering/v2015_06_01/ \
        // implementation/MarketplaceAgreementsInner.java)

        try {
            Deployment deployment =
                    client
                        .deployments()
                        .define(deploymentName)
                        .withExistingResourceGroup(profileModel.resourceGroupName)
                        .withTemplate(template)
                        .withParameters(
                            parameters.entrySet().stream()
                                .collect(
                                        Collectors.toMap(Map.Entry::getKey, e -> Map.of("value", e.getValue()))))
                        .withMode(DeploymentMode.INCREMENTAL)
                        .create();

            ResourceReference resourceReference = deployment.outputResources().get(0);

            Map<String, Map<String, String>> outputs =
                    ((Map<String, Map<String, Map<String, String>>>)
                            client.genericResources().getById(resourceReference.id()).properties())
                            .get("outputs");

//            String storageEndpoint = outputs.get("storageEndpoint").get("value");
//
//            String storageAccountName = outputs.get("storageAccountName").get("value");

//            String fileSystemName = outputs.get("fileSystemName").get("value");

//            logger.info(
//                    "Created application group {}, with container {}/{}",
//                    deploymentName,
//                    storageEndpoint,
//                    fileSystemName);

            return new ManagedApplicationDeployment(
                    resourceReference.id(),
                    deploymentName,
                    deploymentName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse template", e);
        }
    }

    public void deleteManagedApplication(ManagedApplicationDeployment applicationDeployment) {
        AzureResourceManager client = azureResourceConfiguration.getClient(
            UUID.fromString(profileModel.tenantId),
            UUID.fromString(profileModel.subscriptionId)
        );
        logger.info("Deleting the managed application deployment");
        client.deployments().deleteById(applicationDeployment.applicationDeploymentId);

        logger.info("Deleting the managed application");
        client.genericResources().deleteById(applicationDeployment.applicationDeploymentId);
    }

    /** Stores information relevant to an application's deployment */
    public class ManagedApplicationDeployment {
        // The Azure resource Id
        public final String applicationDeploymentId;
        // The name given to the deployment
        public final String applicationDeploymentName;
        // The resource group where the application is deployed
        public final String applicationResourceGroup;
        // The endpoint to use to connect to the storage account created when deploying the application
//        public final String storageEndpoint;
//        // The name of the storage account
//        private final String storageAccountName;
//        // The name of the filesystem created when deploying the application
//        private final String fileSystemName;

        public ManagedApplicationDeployment(
                String applicationDeploymentId,
                String applicationDeploymentName,
                String applicationResourceGroup) {
//                String storageEndpoint,
//                String storageAccountName,
//                String fileSystemName
            this.applicationDeploymentId = applicationDeploymentId;
            this.applicationDeploymentName = applicationDeploymentName;
            this.applicationResourceGroup = applicationResourceGroup;
//            this.storageEndpoint = storageEndpoint;
//            this.storageAccountName = storageAccountName;
//            this.fileSystemName = fileSystemName;
        }
    }

    private String getShortUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.allocate(16);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getEncoder().withoutPadding().encodeToString(byteBuffer.array()).replaceAll("/", "_").replaceAll("\\+", "-");
    }

    @Override
    public void close() {
        //TODO
    }


}
