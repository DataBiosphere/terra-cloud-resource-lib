package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProvider;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProviderImpl;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployments;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeploymentsImpl;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

public class LandingZoneManager {
    private final LandingZoneDefinitionProvider landingZoneDefinitionProvider;
    private final LandingZoneDeployments landingZoneDeployments;
    private final AzureResourceManager resourceManager;
    private final ResourceGroup resourceGroup;
    private final ResourcesReader resourcesReader;

    private final ClientLogger logger = new ClientLogger(LandingZoneManager.class);

    public static LandingZoneManager createLandingZoneManager(AzureResourceManager azureResourceManager,
                                                              ResourceGroup resourceGroup) {

        Objects.requireNonNull(azureResourceManager, "Resource Manager can't be null");
        Objects.requireNonNull(resourceGroup, "Resource Group can't be null");

        return new LandingZoneManager(new LandingZoneDefinitionProviderImpl(),
                new LandingZoneDeploymentsImpl(), azureResourceManager, resourceGroup,
                new ResourcesReaderImpl(azureResourceManager, resourceGroup));
    }

    private LandingZoneManager(LandingZoneDefinitionProvider landingZoneDefinitionProvider,
                               LandingZoneDeployments landingZoneDeployments,
                               AzureResourceManager resourceManager,
                               ResourceGroup resourceGroup, ResourcesReader resourcesReader) {
        this.landingZoneDefinitionProvider = landingZoneDefinitionProvider;
        this.landingZoneDeployments = landingZoneDeployments;
        this.resourceManager = resourceManager;
        this.resourceGroup = resourceGroup;
        this.resourcesReader = resourcesReader;
    }

    public List<FactoryInfo> listDefinitionFactories() {
        return landingZoneDefinitionProvider.factories().stream().toList();
    }

    public List<DeployedResource> deployLandingZone(String landingZoneId,
                                                    Class<? extends LandingZoneDefinitionFactory> factory,
                                                    DefinitionVersion version) {

        Objects.requireNonNull(factory, "Factory information can't be null");
        Objects.requireNonNull(version, "Factory version can't be null");
        if (StringUtils.isBlank(landingZoneId)){
            throw logger.logExceptionAsError( new IllegalArgumentException("Landing Zone ID can't be null or blank"));
        }

        return landingZoneDefinitionProvider
                .createDefinitionFactory(factory)
                .create(version)
                .definition(landingZoneDeployments.define(landingZoneId), resourceManager, resourceGroup)
                .deploy();
    }

    public ResourcesReader reader() {
        return resourcesReader;
    }

    public LandingZoneDeployments deployments() {
        return landingZoneDeployments;
    }

    public LandingZoneDefinitionProvider provider() {
        return landingZoneDefinitionProvider;
    }
}
