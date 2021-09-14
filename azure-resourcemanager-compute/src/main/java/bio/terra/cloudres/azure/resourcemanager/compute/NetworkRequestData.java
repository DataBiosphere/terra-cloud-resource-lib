package bio.terra.cloudres.azure.resourcemanager.compute;

import com.azure.core.management.Region;
import com.google.gson.JsonObject;

public class NetworkRequestData extends BaseRequestData {
    private final String subnetName;

    public NetworkRequestData(String resourceGroupName, String name, Region region, String subnetName) {
        super(resourceGroupName, name, region);
        this.subnetName = subnetName;
    }

    @Override
    public JsonObject getRequestData() {
        JsonObject requestData =  super.getRequestData();
        requestData.addProperty("subnetName", subnetName);
        return requestData;
    }
}
