package bio.terra.cloudres.azure.landingzones.definition.factories;

import static bio.terra.cloudres.azure.landingzones.definition.factories.CromwellBaseResourcesFactory.Subnet.AKS_SUBNET;
import static bio.terra.cloudres.azure.landingzones.definition.factories.CromwellBaseResourcesFactory.Subnet.BATCH_SUBNET;
import static bio.terra.cloudres.azure.landingzones.definition.factories.CromwellBaseResourcesFactory.Subnet.COMPUTE_SUBNET;
import static bio.terra.cloudres.azure.landingzones.definition.factories.CromwellBaseResourcesFactory.Subnet.POSTGRESQL_SUBNET;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionContext;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionHeader;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinable;
import bio.terra.cloudres.azure.landingzones.definition.LandingZoneDefinition;
import bio.terra.cloudres.azure.landingzones.definition.ResourceNameGenerator;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.Networks;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.postgresql.models.PublicNetworkAccessEnum;
import com.azure.resourcemanager.postgresql.models.Server;
import com.azure.resourcemanager.postgresql.models.ServerPropertiesForDefaultCreate;
import com.azure.resourcemanager.postgresql.models.ServerVersion;
import com.azure.resourcemanager.postgresql.models.Sku;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import java.util.List;

/**
 * An implementation of {@link LandingZoneDefinitionFactory} that deploys resources required for
 * cromwell. Current resources are: - VNet: Subnets required for AKS, Batch, PostgreSQL and
 * Compute/VMs - AKS Account (?) TODO - AKS Nodepool TODO - Batch Account TODO - Storage Account
 * TODO - PostgreSQL server TODO
 */
public class CromwellBaseResourcesFactory extends ArmClientsDefinitionFactory {
  private final String LZ_NAME = "Cromwell Landing Zone Base Resources";
  private final String LZ_DESC =
      "Cromwell Base Resources: VNet, AKS Account & Nodepool, Batch Account,"
          + " Storage Account, PostgreSQL server, Subnets for AKS, Batch, Posgres, and Compute";

  enum Subnet {
    AKS_SUBNET,
    BATCH_SUBNET,
    POSTGRESQL_SUBNET,
    COMPUTE_SUBNET
  }

  CromwellBaseResourcesFactory() {}

  public CromwellBaseResourcesFactory(ArmManagers armManagers) {
    super(armManagers);
  }

  @Override
  public DefinitionHeader header() {
    return new DefinitionHeader(LZ_NAME, LZ_DESC);
  }

  @Override
  public List<DefinitionVersion> availableVersions() {
    return List.of(DefinitionVersion.V1);
  }

  @Override
  public LandingZoneDefinable create(DefinitionVersion version) {
    if (version.equals(DefinitionVersion.V1)) {
      return new DefinitionV1(armManagers);
    }
    throw new RuntimeException("Invalid Version");
  }

  class DefinitionV1 extends LandingZoneDefinition {

    private final ClientLogger logger = new ClientLogger(DefinitionV1.class);

    protected DefinitionV1(ArmManagers armManagers) {
      super(armManagers);
    }

    @Override
    public Deployable definition(DefinitionContext definitionContext) {
      AzureResourceManager azureResourceManager = armManagers.azureResourceManager();
      WithLandingZoneResource deployment = definitionContext.deployment();
      ResourceGroup resourceGroup = definitionContext.resourceGroup();
      ResourceNameGenerator nameGenerator = definitionContext.resourceNameGenerator();

      var vNet =
          azureResourceManager
              .networks()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_VNET_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup)
              .withAddressSpace("10.1.0.0/27")
              .withSubnet(AKS_SUBNET.name(), "10.1.0.0/29")
              .withSubnet(BATCH_SUBNET.name(), "10.1.0.8/29")
              .withSubnet(POSTGRESQL_SUBNET.name(), "10.1.0.16/29")
              .withSubnet(COMPUTE_SUBNET.name(), "10.1.0.24/29");

      var postgres =
          armManagers
              .postgreSqlManager()
              .servers()
              .define(
                  nameGenerator.nextName(ResourceNameGenerator.MAX_POSTGRESQL_SERVER_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup.name())
              .withProperties(
                  new ServerPropertiesForDefaultCreate()
                      .withAdministratorLogin("test_lz_admin")
                      .withAdministratorLoginPassword("AFDgLSVgM4oY!4")
                      .withVersion(ServerVersion.ONE_ONE)
                      .withPublicNetworkAccess(PublicNetworkAccessEnum.DISABLED))
              .withSku(new Sku().withName("GP_Gen5_2"));

      var prerequisites =
          deployment
              .definePrerequisites()
              .withVNetWithPurpose(
                  vNet, AKS_SUBNET.name(), SubnetResourcePurpose.AKS_NODE_POOL_SUBNET)
              .withVNetWithPurpose(
                  vNet, BATCH_SUBNET.name(), SubnetResourcePurpose.WORKSPACE_BATCH_SUBNET)
              .withVNetWithPurpose(
                  vNet, POSTGRESQL_SUBNET.name(), SubnetResourcePurpose.POSTGRESQL_SUBNET)
              .withVNetWithPurpose(
                  vNet, COMPUTE_SUBNET.name(), SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET)
              .withResourceWithPurpose(postgres, ResourcePurpose.SHARED_RESOURCE)
              .deploy();

      Networks listNetworks = azureResourceManager.networks();
      Network vNetwork =
          listNetworks
              .listByResourceGroupAsync(resourceGroup.name())
              .flatMap(
                  network -> {
                    logger.info(
                        "Getting network name and id---- "
                            + network.name()
                            + " created @ "
                            + network.id());
                    return network.refreshAsync();
                  })
              .blockLast();

      assert vNetwork != null;

      Server postgreSqlServer =
          armManagers.postgreSqlManager().servers()
              .listByResourceGroup(resourceGroup.name())
              .stream()
              .findFirst().get();

      var privateEndpoint =
          azureResourceManager
              .privateEndpoints()
              .define(
                  nameGenerator.nextName(ResourceNameGenerator.MAX_PRIVATE_ENDPOINT_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup)
              .withSubnetId(vNetwork.subnets().get(POSTGRESQL_SUBNET.name()).id())
              .definePrivateLinkServiceConnection(
                  nameGenerator.nextName(
                      ResourceNameGenerator.MAX_PRIVATE_LINK_CONNECTION_NAME_LENGTH))
              .withResourceId(postgreSqlServer.id())
              .withSubResource(PrivateLinkSubResourceName.fromString("postgresqlServer"))
              .attach();

      var aks =
          azureResourceManager
              .kubernetesClusters()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_AKS_CLUSTER_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup)
              .withDefaultVersion()
              .withSystemAssignedManagedServiceIdentity()
              .defineAgentPool(
                  nameGenerator.nextName(ResourceNameGenerator.MAX_AKS_AGENT_POOL_NAME_LENGTH))
              .withVirtualMachineSize(ContainerServiceVMSizeTypes.STANDARD_A2_V2)
              .withAgentPoolVirtualMachineCount(1)
              .withAgentPoolMode(
                  AgentPoolMode.SYSTEM) // TODO VM Size? Pool Machine count? AgentPoolMode?
              .withVirtualNetwork(vNetwork.id(), AKS_SUBNET.name())
              .attach()
              .withDnsPrefix(
                  nameGenerator.nextName(ResourceNameGenerator.MAX_AKS_DNS_PREFIX_NAME_LENGTH));

      var batch =
          armManagers
              .batchManager()
              .batchAccounts()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_BATCH_ACCOUNT_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup.name());

      var storage =
          azureResourceManager
              .storageAccounts()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_STORAGE_ACCOUNT_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup);

      var relay =
          armManagers
              .relayManager()
              .namespaces()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_RELAY_NS_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup.name());

      return deployment
          .withResourceWithPurpose(aks, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(batch, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(relay, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(privateEndpoint, ResourcePurpose.SHARED_RESOURCE);
    }
  }
}
