package bio.terra.cloudres.azure.resourcemanager.kubernetes;

import bio.terra.cloudres.common.CloudOperation;

/** {@link CloudOperation} for using Azure Kubernetes Service API. */
public enum KubernetesOperation implements CloudOperation {
  CREATE_NAMESPACE,
}
