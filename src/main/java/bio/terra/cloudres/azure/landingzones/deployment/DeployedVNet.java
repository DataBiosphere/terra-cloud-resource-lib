package bio.terra.cloudres.azure.landingzones.deployment;

import java.util.Map;

public record DeployedVNet (String Id, Map<SubnetResourcePurpose, DeployedSubnet> subnetIdPurposeMap, String region) {
}
