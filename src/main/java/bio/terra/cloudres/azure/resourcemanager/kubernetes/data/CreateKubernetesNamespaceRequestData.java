package bio.terra.cloudres.azure.resourcemanager.kubernetes.data;

import bio.terra.cloudres.azure.resourcemanager.kubernetes.KubernetesOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureKubernetesNamespace;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

@AutoValue
public abstract class CreateKubernetesNamespaceRequestData extends BaseKubernetesRequestData {
  /** The name of the namespace. */
  public abstract String namespaceName();

  @Override
  public final CloudOperation cloudOperation() {
    return KubernetesOperation.CREATE_NAMESPACE;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureKubernetesNamespace(
                new AzureKubernetesNamespace()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .clusterName(clusterName())
                    .namespaceName(namespaceName())));
  }

  public static Builder builder() {
    return new AutoValue_CreateKubernetesNamespaceRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setNamespaceName(String value);

    public abstract Builder setClusterName(String value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract CreateKubernetesNamespaceRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("namespaceName", namespaceName());
    return requestData;
  }
}
