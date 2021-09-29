package bio.terra.cloudres.azure.resourcemanager.common;

import bio.terra.cloudres.common.CloudOperation;

/**
 * {@link CloudOperation} for using Azure ResourceManager API.
 *
 * <p>This is used as a generic fallback; in practice, a more specific value should be used.
 */
public enum ResourceManagerOperation implements CloudOperation {
  AZURE_RESOURCE_MANAGER_UNKNOWN_OPERATION
}
