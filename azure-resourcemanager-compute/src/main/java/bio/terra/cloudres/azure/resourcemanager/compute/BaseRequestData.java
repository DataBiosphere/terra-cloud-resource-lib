package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

public abstract class BaseRequestData extends AzureRequestData {
    protected final String name;
    protected final Region region;
    protected final String resourceGroupName;

    protected BaseRequestData(String resourceGroupName, String name, Region region) {
        this.name = name;
        this.region = region;
        this.resourceGroupName = resourceGroupName;
    }

    @Override
    public JsonObject getRequestData() {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("resourceGroupName", resourceGroupName);
        requestData.addProperty("name", name);
        requestData.addProperty("region", region.name());
        return requestData;
    }
}
