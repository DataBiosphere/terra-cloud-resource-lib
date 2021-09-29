package bio.terra.cloudres.azure.resourcemanager.compute.data;

import bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerOperation;
import bio.terra.cloudres.common.CloudOperation;
import com.azure.core.management.Region;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;

/** Disk creation request data. */
@AutoValue
public abstract class CreateDiskRequestData extends BaseComputeRequestData {
  /** The size of the disk in GB. */
  public abstract int size();

  @Override
  public final CloudOperation cloudOperation() {
    return ComputeManagerOperation.AZURE_CREATE_DISK;
  }

  public static Builder builder() {
    return new AutoValue_CreateDiskRequestData.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setName(String value);

    public abstract Builder setRegion(Region value);

    public abstract Builder setResourceGroupName(String value);

    public abstract Builder setSize(int value);

    public abstract CreateDiskRequestData build();
  }

  @Override
  public JsonObject serialize() {
    JsonObject requestData = super.serializeCommon();
    requestData.addProperty("size", size());
    return requestData;
  }
}
