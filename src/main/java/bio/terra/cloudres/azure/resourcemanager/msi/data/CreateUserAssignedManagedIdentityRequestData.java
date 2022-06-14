package bio.terra.cloudres.azure.resourcemanager.msi.data;

import bio.terra.cloudres.azure.resourcemanager.msi.MsiManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureManagedIdentity;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

@AutoValue
public abstract class CreateUserAssignedManagedIdentityRequestData extends BaseMsiRequestData {

  @Override
  public final CloudOperation cloudOperation() {
    return MsiManagerOperation.CREATE_USER_ASSIGNED_MANAGED_IDENTITY;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureManagedIdentity(
                new AzureManagedIdentity()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .identityName(name())));
  }

  public static CreateUserAssignedManagedIdentityRequestData.Builder builder() {
    return new AutoValue_CreateUserAssignedManagedIdentityRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateUserAssignedManagedIdentityRequestData.Builder setName(String value);

    public abstract CreateUserAssignedManagedIdentityRequestData.Builder setRegion(Region value);

    public abstract CreateUserAssignedManagedIdentityRequestData.Builder setTenantId(String value);

    public abstract CreateUserAssignedManagedIdentityRequestData.Builder setSubscriptionId(
        String value);

    public abstract CreateUserAssignedManagedIdentityRequestData.Builder setResourceGroupName(
        String value);

    public abstract CreateUserAssignedManagedIdentityRequestData build();
  }

  @Override
  public JsonObject serialize() {
    return serializeCommon();
  }
}
