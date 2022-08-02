package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.*;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.Deployable;
import bio.terra.cloudres.azure.landingzones.deployment.LandingZoneDeployment.DefinitionStages.WithLandingZoneResource;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.batch.BatchManager;
import com.azure.resourcemanager.containerservice.models.AgentPoolMode;
import com.azure.resourcemanager.containerservice.models.ContainerServiceVMSizeTypes;
import com.azure.resourcemanager.postgresql.PostgreSqlManager;
import com.azure.resourcemanager.postgresql.models.InfrastructureEncryption;
import com.azure.resourcemanager.postgresql.models.ServerPropertiesForCreate;
import com.azure.resourcemanager.postgresql.models.ServerVersion;
import com.azure.resourcemanager.relay.RelayManager;
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

  CromwellBaseResourcesFactory() {}

  public CromwellBaseResourcesFactory(
      AzureResourceManager azureResourceManager,
      RelayManager relayManager,
      BatchManager batchManager,
      PostgreSqlManager postgreSqlManager) {
    super(azureResourceManager, relayManager, batchManager, postgreSqlManager);
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
      return new DefinitionV1(azureResourceManager, relayManager, batchManager, postgreSqlManager);
    }
    throw new RuntimeException("Invalid Version");
  }

  class DefinitionV1 extends LandingZoneDefinition {

    protected DefinitionV1(
        AzureResourceManager azureResourceManager,
        RelayManager relayManager,
        BatchManager batchManager,
        PostgreSqlManager postgreSqlManager) {
      super(azureResourceManager, relayManager, batchManager, postgreSqlManager);
    }

    @Override
    public Deployable definition(DefinitionContext definitionContext) {
      WithLandingZoneResource deployment = definitionContext.deployment();
      ResourceGroup resourceGroup = definitionContext.resourceGroup();
      ResourceNameGenerator nameGenerator = definitionContext.resourceNameGenerator();

      var vNet =
          azureResourceManager
              .networks()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_VNET_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup)
              .withAddressSpace("10.0.0.0/28")
              .withSubnet("aks", "10.0.0.0/29")
              .withSubnet("batch", "10.0.0.0/30")
              .withSubnet("postgres", "10.0.0.0/31")
              .withSubnet("compute", "10.0.0.0/32");

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
              .attach()
              .withDnsPrefix(
                  nameGenerator.nextName(ResourceNameGenerator.MAX_AKS_DNS_PREFIX_NAME_LENGTH));

      //   TODO can we make this async/sequential after deployment?
      //      var stopAks = azureResourceManager
      //              .kubernetesClusters()
      //              .stop(resourceGroup.name(), aks.name());

      var batch =
          batchManager
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

      var postgres =
          postgreSqlManager
              .servers()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_SERVER_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup.name())
              .withProperties(
                  new ServerPropertiesForCreate()
                      .withVersion(ServerVersion.ONE_ONE)
                      .withInfrastructureEncryption(InfrastructureEncryption.ENABLED));

      var relay =
          relayManager
              .namespaces()
              .define(nameGenerator.nextName(ResourceNameGenerator.MAX_RELAY_NS_NAME_LENGTH))
              .withRegion(resourceGroup.region())
              .withExistingResourceGroup(resourceGroup.name());

      return deployment
          .withVNetWithPurpose(vNet, "aks", SubnetResourcePurpose.AKS_NODE_POOL_SUBNET)
          .withVNetWithPurpose(vNet, "batch", SubnetResourcePurpose.WORKSPACE_BATCH_SUBNET)
          .withVNetWithPurpose(vNet, "postgres", SubnetResourcePurpose.POSTGRESQL_SUBNET)
          .withVNetWithPurpose(vNet, "compute", SubnetResourcePurpose.WORKSPACE_COMPUTE_SUBNET)
          .withResourceWithPurpose(aks, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(batch, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(storage, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(postgres, ResourcePurpose.SHARED_RESOURCE)
          .withResourceWithPurpose(relay, ResourcePurpose.SHARED_RESOURCE);
    }
  }
}
