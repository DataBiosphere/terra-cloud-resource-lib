package bio.terra.cloudres.azure.resourcemanager.resources;

import bio.terra.janitor.model.CloudResourceUid;
import com.google.gson.JsonObject;
import java.util.Optional;

public abstract class AzureRequestData {
  private final Optional<CloudResourceUid> cloudResourceUid;

  protected AzureRequestData(Optional<CloudResourceUid> cloudResourceUid) {
    this.cloudResourceUid = cloudResourceUid;
  }

  public final Optional<CloudResourceUid> getCloudResourceUid() {
    return cloudResourceUid;
  }

  public abstract JsonObject getRequestData();
}
