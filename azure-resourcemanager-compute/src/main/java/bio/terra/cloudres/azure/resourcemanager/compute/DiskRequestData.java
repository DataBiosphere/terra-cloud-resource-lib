package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureRequestData;
import com.azure.core.management.Region;
import com.google.gson.JsonObject;

public class DiskRequestData extends BaseRequestData {
    private final int size;
    //TODO: type (e.g. SSD)?

    public DiskRequestData(String resourceGroupName, String name, Region region, int size) {
        super(resourceGroupName, name, region);
        this.size = size;
    }

    @Override
    public JsonObject getRequestData() {
        JsonObject requestData = super.getRequestData();
        requestData.addProperty("size", size);
        return requestData;
    }
}
