package bio.terra.cloudres.azure.landingzones;

import bio.terra.cloudres.azure.landingzones.deployment.DeployedResource;

import java.util.List;

public class TestUtils {
  public static String findFirstStorageAccountId(List<DeployedResource> resources) {
    return findFirstResourceIdByResourceType(resources, "Microsoft.Storage/storageAccounts");
  }

  public static String findFirstVNetId(List<DeployedResource> resources) {
    return findFirstResourceIdByResourceType(resources, "Microsoft.Network/virtualNetworks");
  }

  public static String findFirstResourceIdByResourceType(
      List<DeployedResource> resources, String type) {
    return resources.stream()
        .filter(r -> r.resourceType().equals(type))
        .findFirst()
        .orElse(null)
        .resourceId();
  }
}
