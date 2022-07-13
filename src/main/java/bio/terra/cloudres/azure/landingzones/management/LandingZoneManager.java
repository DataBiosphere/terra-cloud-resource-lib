package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionContext;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import bio.terra.cloudres.azure.landingzones.definition.ResourceNameGenerator;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionFactory;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProvider;
import bio.terra.cloudres.azure.landingzones.definition.factories.LandingZoneDefinitionProviderImpl;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployments;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeploymentsImpl;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * High level component to deploy and list deployment definitions, and listing resources by purpose.
 */
public class LandingZoneManager {
  private static final ClientLogger logger = new ClientLogger(LandingZoneManager.class);
  private final LandingZoneDefinitionProvider landingZoneDefinitionProvider;
  private final LandingZoneDeployments landingZoneDeployments;
  private final AzureResourceManager resourceManager;
  private final ResourceGroup resourceGroup;
  private final ResourcesReader resourcesReader;

  private LandingZoneManager(
      LandingZoneDefinitionProvider landingZoneDefinitionProvider,
      LandingZoneDeployments landingZoneDeployments,
      AzureResourceManager resourceManager,
      ResourceGroup resourceGroup,
      ResourcesReader resourcesReader) {
    this.landingZoneDefinitionProvider = landingZoneDefinitionProvider;
    this.landingZoneDeployments = landingZoneDeployments;
    this.resourceManager = resourceManager;
    this.resourceGroup = resourceGroup;
    this.resourcesReader = resourcesReader;
  }

  public static LandingZoneManager createLandingZoneManager(
      TokenCredential credential, AzureProfile profile, String resourceGroupName) {

    Objects.requireNonNull(credential, "credential can't be null");
    Objects.requireNonNull(profile, "profile can't be null");
    if (StringUtils.isBlank(resourceGroupName)) {
      throw logger.logExceptionAsError(
          new IllegalArgumentException("Resource group name can't be blank or null"));
    }

    ArmManagers armManagers = createArmManagers(credential, profile);
    ResourceGroup resourceGroup =
        armManagers.azureResourceManager().resourceGroups().getByName(resourceGroupName);

    return new LandingZoneManager(
        new LandingZoneDefinitionProviderImpl(armManagers),
        new LandingZoneDeploymentsImpl(),
        armManagers.azureResourceManager(),
        resourceGroup,
        new ResourcesReaderImpl(armManagers.azureResourceManager(), resourceGroup));
  }

  private static ArmManagers createArmManagers(TokenCredential credential, AzureProfile profile) {
    AzureResourceManager azureResourceManager =
        AzureResourceManager.authenticate(credential, profile)
            .withSubscription(profile.getSubscriptionId());
    RelayManager relayManager = RelayManager.authenticate(credential, profile);

    return new ArmManagers(azureResourceManager, relayManager);
  }

  public List<FactoryInfo> listDefinitionFactories() {
    return landingZoneDefinitionProvider.factories().stream().toList();
  }

  public List<DeployedResource> deployLandingZone(
      String landingZoneId,
      Class<? extends LandingZoneDefinitionFactory> factory,
      DefinitionVersion version,
      Map<String, String> parameters) {

    return deployLandingZoneAsync(landingZoneId, factory, version, parameters)
        .collectList()
        .block();
  }

  public Flux<DeployedResource> deployLandingZoneAsync(
      String landingZoneId,
      Class<? extends LandingZoneDefinitionFactory> factory,
      DefinitionVersion version,
      Map<String, String> parameters) {

    Objects.requireNonNull(factory, "Factory information can't be null");
    Objects.requireNonNull(version, "Factory version can't be null");
    if (StringUtils.isBlank(landingZoneId)) {
      throw logger.logExceptionAsError(
          new IllegalArgumentException("Landing Zone ID can't be null or blank"));
    }

    return landingZoneDefinitionProvider
        .createDefinitionFactory(factory)
        .create(version)
        .definition(createNewDefinitionContext(landingZoneId, parameters))
        .deployAsync();
  }

  private DefinitionContext createNewDefinitionContext(
      String landingZoneId, Map<String, String> parameters) {
    return new DefinitionContext(
        landingZoneId,
        landingZoneDeployments.define(landingZoneId),
        resourceGroup,
        new ResourceNameGenerator(landingZoneId),
        parameters);
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
