package bio.terra.cloudres.azure.landingzones.deployment;

import java.util.Map;

public record DeployedResource(String resourceId, String resourceType, Map<String, String> tags, String region) {
}
