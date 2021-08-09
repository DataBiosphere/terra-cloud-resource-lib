package bio.terra.cloudres.azure.resouce.manager;

import java.io.IOException;
import java.util.Properties;

public class BillingProfileModel {
    public String subscriptionId;
    public String resourceGroupName;
    public String tenantId;

    private BillingProfileModel(String subscriptionId, String resourceGroupName, String tenantId) {
        this.subscriptionId = subscriptionId;
        this.resourceGroupName = resourceGroupName;
        this.tenantId = tenantId;
    }

    public static BillingProfileModel getFromPropsFile(String fileName) throws IOException {
        PropertyReader reader = new PropertyReader(fileName);
        Properties props = reader.getProperties();

        return new BillingProfileModel(props.get("targetSubscriptionId").toString(),
                props.get("targetResourceGroupName").toString(),
                props.get("targetTenantId").toString());
    }
}
