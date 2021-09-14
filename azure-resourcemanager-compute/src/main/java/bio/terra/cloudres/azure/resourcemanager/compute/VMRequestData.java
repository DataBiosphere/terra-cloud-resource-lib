package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureRequestData;
import com.azure.core.management.Region;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.google.gson.JsonObject;

public class VMRequestData extends BaseRequestData {
    private final Network network;
    private final String subnetName;
    private final PublicIpAddress ip;
    private final Disk disk;

    public VMRequestData(String resourceGroupName, String name, Region region, Network network, String subnetName, PublicIpAddress ip, Disk disk) {
        super(resourceGroupName, name, region);
        this.network = network;
        this.subnetName = subnetName;
        this.ip = ip;
        this.disk = disk;
    }

    @Override
    public JsonObject getRequestData() {
        JsonObject requestData =  super.getRequestData();
        requestData.addProperty("network", network.name());
        requestData.addProperty("subnetName", subnetName);
        requestData.addProperty("ip", ip.ipAddress());
        requestData.addProperty("disk", disk.name());
        return requestData;
    }
}
