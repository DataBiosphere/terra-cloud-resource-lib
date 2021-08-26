package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.cloudres.common.CloudOperation;

public enum ResourceManagerOperation implements CloudOperation {
  AZURE_RESOURCE_MANAGER_CREATE_DEPLOYMENT,
  AZURE_RESOURCE_MANAGER_DELETE_DEPLOYMENT
}
