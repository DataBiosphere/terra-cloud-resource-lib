package bio.terra.cloudres.azure.resourcemanager.compute;

import bio.terra.cloudres.azure.resourcemanager.relay.RelayManagerOperation;
import bio.terra.cloudres.azure.resourcemanager.relay.data.CreateRelayRequestData;
import com.azure.core.management.Region;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("unit")
public class RelayManagerRequestDataTest {

  @Test
  public void serializeCreateAzureRelay() {
    CreateRelayRequestData create =
            CreateRelayRequestData.builder()
            .setName("my-relay")
            .setTenantId("my-tenant")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .setRegion(Region.US_EAST)
            .build();

    assertEquals(RelayManagerOperation.AZURE_CREATE_RELAY, create.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\",\"name\":\"my-relay\",\"region\":\"eastus\"}",
        create.serialize().toString());
  }

  @Test
  public void serializeCreateAzureRelayHybridConnection() {
    CreateRelayRequestData create =
            CreateRelayRequestData.builder()
            .setName("my-relay-hc")
            .setTenantId("my-tenant")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .setRegion(Region.US_EAST)
            .build();

    assertEquals(RelayManagerOperation.AZURE_CREATE_RELAY, create.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\",\"name\":\"my-relay-hc\",\"region\":\"eastus\"}",
        create.serialize().toString());
  }
}
