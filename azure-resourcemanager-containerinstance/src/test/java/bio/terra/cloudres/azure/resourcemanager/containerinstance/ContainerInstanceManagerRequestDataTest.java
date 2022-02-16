package bio.terra.cloudres.azure.resourcemanager.containerinstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bio.terra.cloudres.azure.resourcemanager.containerinstance.data.CreateContainerInstanceRequestData;
import com.azure.core.management.Region;
import com.azure.resourcemanager.containerinstance.models.Container;
import com.azure.resourcemanager.containerinstance.models.OperatingSystemTypes;
import java.util.Arrays;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class ContainerInstanceManagerRequestDataTest {
  @Test
  public void serializeCreateContainerInstance() {
    CreateContainerInstanceRequestData createContainerRequest =
        CreateContainerInstanceRequestData.builder()
            .setTenantId("my-tenant1")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .setRegion(Region.US_WEST2)
            .setContainerGroupName("myContainerGroup")
            .setContainers(
                Arrays.asList(
                    new Container().withName("container1"), new Container().withName("container2")))
            .setOperatingSystemType(OperatingSystemTypes.LINUX)
            .build();

    assertEquals(
        ContainerInstanceManagerOperation.AZURE_CREATE_CONTAINER_INSTANCE,
        createContainerRequest.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant1\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\",\"containerGroupName\":\"myContainerGroup\",\"region\":\"westus2\",\"containers\":\"[{\\\"name\\\":\\\"container1\\\"},{\\\"name\\\":\\\"container2\\\"}]\",\"operatingSystemTypes\":\"Linux\"}",
        createContainerRequest.serialize().toString());
  }
}
