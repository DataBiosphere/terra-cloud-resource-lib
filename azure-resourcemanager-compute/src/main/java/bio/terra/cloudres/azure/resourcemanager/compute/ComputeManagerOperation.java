package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Azure ComputeManager API. */
public enum ComputeManagerOperation implements CloudOperation {
  AZURE_CREATE_PUBLIC_IP,
  AZURE_CREATE_DISK,
  AZURE_CREATE_NETWORK,
  AZURE_CREATE_VM
}
