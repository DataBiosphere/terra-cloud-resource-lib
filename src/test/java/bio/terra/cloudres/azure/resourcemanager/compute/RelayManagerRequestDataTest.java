package bio.terra.cloudres.azure.resourcemanager.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.azure.resourcemanager.relay.RelayManagerOperation;
import bio.terra.cloudres.azure.resourcemanager.relay.data.CreateRelayHybridConnectionRequestData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class RelayManagerRequestDataTest {

  @Test
  public void serializeCreateAzureRelayHybridConnection() {
    CreateRelayHybridConnectionRequestData create =
        CreateRelayHybridConnectionRequestData.builder()
            .setName("my-relay-hc")
            .setTenantId("my-tenant")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .build();

    assertEquals(
        RelayManagerOperation.AZURE_CREATE_RELAY_HYBRID_CONNECTION, create.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\",\"name\":\"my-relay-hc\"}",
        create.serialize().toString());
  }
}
