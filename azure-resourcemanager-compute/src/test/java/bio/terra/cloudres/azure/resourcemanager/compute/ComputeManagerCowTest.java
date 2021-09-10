package bio.terra.cloudres.azure.resourcemanager.compute;

import static bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerIntegrationUtils.defaultComputeManagerCow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

@Tag("integration")
public class ComputeManagerCowTest {
  private static final ComputeManagerCow computeManagerCow = defaultComputeManagerCow();

  @Test
  public void createListDeletePublicIp() {
    // Create public IP
    final String name = "crl-integration-public-ip";
    PublicIpAddress createResponse = createPublicIp(name);

    // Verify get response
    PublicIpAddress getResponse =
        computeManagerCow
            .computeManager()
            .networkManager()
            .publicIpAddresses()
            .getByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), name);
    assertEquals(name, getResponse.name());
    assertEquals(createResponse.fqdn(), getResponse.fqdn());
    assertEquals(createResponse.ipAddress(), getResponse.ipAddress());

    // Verify list response
    Stream<PublicIpAddress> listResponse =
        computeManagerCow.computeManager().networkManager().publicIpAddresses()
            .listByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
            .stream();
    // There may be other public IPs from other tests.
    assertThat(
        listResponse.map(PublicIpAddress::name).collect(Collectors.toList()),
        Matchers.hasItem(name));

    // Delete public IP
    computeManagerCow
        .computeManager()
        .networkManager()
        .publicIpAddresses()
        .deleteByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), name);

    // Verify get response throws 404
    ManagementException e =
        assertThrows(
            ManagementException.class,
            () ->
                computeManagerCow
                    .computeManager()
                    .networkManager()
                    .publicIpAddresses()
                    .getByResourceGroup(
                        ComputeManagerIntegrationUtils.getReusableResourceGroup(), name));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  private static PublicIpAddress createPublicIp(String name) {
    return computeManagerCow
        .computeManager()
        .networkManager()
        .publicIpAddresses()
        .define(name)
        .withRegion(Region.US_EAST)
        .withExistingResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
        .withDynamicIP()
        .create(); // TODO add Context
  }
}
