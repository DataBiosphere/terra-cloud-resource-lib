package bio.terra.cloudres.azure.resourcemanager.msi.data;

import bio.terra.cloudres.azure.resourcemanager.common.Defaults;
import bio.terra.cloudres.azure.resourcemanager.common.ResourceManagerRequestData;
import com.azure.core.management.Region;
import com.azure.resourcemanager.msi.MsiManager;
import com.google.gson.JsonObject;

/**
 * Extends {@link ResourceManagerRequestData} to add common fields for working with the MSI
 * (Managed Service Identity) Manager API.
 */
public abstract class BaseMsiRequestData implements ResourceManagerRequestData {
    /** The name of the resource. */
    public abstract String name();

    /** The region of the resource. */
    public abstract Region region();

    /** The tenant of the resource. */
    public abstract String tenantId();

    /** The subscription of the resource. */
    public abstract String subscriptionId();

    /** The resource group of the resource. */
    public abstract String resourceGroupName();

    /**
     * Serializes this object to JSON. Not overriding {@link ResourceManagerRequestData#serialize()}
     * to ensure subclasses implement their own serialize method.
     */
    protected JsonObject serializeCommon() {
        JsonObject requestData = new JsonObject();
        requestData.addProperty("name", name());
        requestData.addProperty("region", region().name());
        requestData.addProperty("tenantId", tenantId());
        requestData.addProperty("subscriptionId", subscriptionId());
        requestData.addProperty("resourceGroupName", resourceGroupName());
        return requestData;
    }
}
