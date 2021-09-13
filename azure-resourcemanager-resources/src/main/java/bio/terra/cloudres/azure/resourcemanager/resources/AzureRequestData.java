package bio.terra.cloudres.azure.resourcemanager.resources;

import com.google.gson.JsonObject;

public abstract class AzureRequestData {
  public abstract JsonObject getRequestData();
}
