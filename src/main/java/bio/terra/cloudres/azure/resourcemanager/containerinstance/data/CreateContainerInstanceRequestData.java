package bio.terra.cloudres.azure.resourcemanager.containerinstance.data;

import bio.terra.cloudres.azure.resourcemanager.containerinstance.ContainerInstanceManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureContainerInstance;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.azure.resourcemanager.containerinstance.models.Container;
import com.azure.resourcemanager.containerinstance.models.OperatingSystemTypes;
import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Optional;

@AutoValue
public abstract class CreateContainerInstanceRequestData extends BaseContainerInstanceRequestData {
  /** The containers within the container group. */
  public abstract List<Container> containers();
  /** The operating system type required by the containers in the container group. */
  public abstract OperatingSystemTypes operatingSystemType();

  @Override
  public final CloudOperation cloudOperation() {
    return ContainerInstanceManagerOperation.AZURE_CREATE_CONTAINER_INSTANCE;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureContainerInstance(
                new AzureContainerInstance()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .containerGroupName(containerGroupName())));
  }

  public static Builder builder() {
    return new AutoValue_CreateContainerInstanceRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setContainerGroupName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setTenantId(String value);

    public abstract Builder setSubscriptionId(String value);

    public abstract Builder setResourceGroupName(String value);

    public abstract Builder setContainers(List<Container> containers);

    public abstract Builder setOperatingSystemType(OperatingSystemTypes operatingSystemType);

    public abstract CreateContainerInstanceRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.FINAL).create();
    requestData.addProperty("containers", gson.toJson(containers()));
    requestData.addProperty("operatingSystemTypes", operatingSystemType().toString());
    return requestData;
  }
}
