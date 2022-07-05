package bio.terra.cloudres.azure.landingzones.management;

import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;
import bio.terra.cloudres.azure.landingzones.deployment.DeployedVNet;
import bio.terra.cloudres.azure.landingzones.deployment.ResourcePurpose;
import bio.terra.cloudres.azure.landingzones.deployment.SubnetResourcePurpose;

import java.util.List;

public interface ResourcesReader {
    List<DeployedResource> listSharedResources();

    List<DeployedResource> listResourcesByPurpose(ResourcePurpose purpose);

    List<DeployedVNet> listVNetWithSubnetPurpose(SubnetResourcePurpose purpose);
}
