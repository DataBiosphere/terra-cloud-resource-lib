package bio.terra.cloudres.azure.resourcemanager.postgresflex.data;

import bio.terra.cloudres.azure.resourcemanager.postgresflex.PostgresFlexOperation;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.janitor.model.AzureDatabase;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.CloudResourceUid;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;
import java.util.Optional;

@AutoValue
public abstract class CreateDatabaseRequestData extends BasePostgresFlexRequestData {
  /** The name of the database. */
  public abstract String databaseName();

  @Override
  public final CloudOperation cloudOperation() {
    return PostgresFlexOperation.AZURE_CREATE_DATABASE;
  }

  @Override
  public final Optional<CloudResourceUid> resourceUidCreation() {
    return Optional.of(
        new CloudResourceUid()
            .azureDatabase(
                new AzureDatabase()
                    .resourceGroup(
                        new AzureResourceGroup()
                            .tenantId(tenantId())
                            .subscriptionId(subscriptionId())
                            .resourceGroupName(resourceGroupName()))
                    .serverName(serverName())
                    .databaseName(databaseName())));
  }

  public static CreateDatabaseRequestData.Builder builder() {
    return new AutoValue_CreateDatabaseRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract CreateDatabaseRequestData.Builder setDatabaseName(String value);

    public abstract CreateDatabaseRequestData.Builder setServerName(String value);

    public abstract CreateDatabaseRequestData.Builder setTenantId(String value);

    public abstract CreateDatabaseRequestData.Builder setSubscriptionId(String value);

    public abstract CreateDatabaseRequestData.Builder setResourceGroupName(String value);

    public abstract CreateDatabaseRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("databaseName", databaseName());
    return requestData;
  }
}
