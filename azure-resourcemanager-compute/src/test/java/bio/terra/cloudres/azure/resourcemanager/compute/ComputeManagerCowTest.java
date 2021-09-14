package bio.terra.cloudres.azure.resourcemanager.compute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.cloudres.azure.resourcemanager.resources.AzureIntegrationUtils;
import bio.terra.cloudres.azure.resourcemanager.resources.Defaults;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("integration")
public class ComputeManagerCowTest {
  private static final Logger logger = LoggerFactory.getLogger(ComputeManagerCowTest.class);
  private static final ComputeManagerCow computeManagerCow =
      ComputeManagerCow.create(
          IntegrationUtils.DEFAULT_CLIENT_CONFIG,
          AzureIntegrationUtils.getAdminAzureCredentialsOrDie(),
          AzureIntegrationUtils.getUserAzureProfileOrDie());

  private static final String resourceGroupName = AzureIntegrationUtils.getResuableResourceGroup();

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
            .getByResourceGroup(resourceGroupName, name);
    logger.info("GOT IP " + getResponse.ipAddress());
    assertEquals(name, getResponse.name());
    assertEquals(createResponse.fqdn(), getResponse.fqdn());
    assertEquals(createResponse.ipAddress(), getResponse.ipAddress());

    // Verify list response
    Stream<PublicIpAddress> listResponse =
        computeManagerCow.computeManager().networkManager().publicIpAddresses()
            .listByResourceGroup(resourceGroupName).stream();
    // There may be other public IPs from other tests.
    assertThat(
        listResponse.map(PublicIpAddress::name).collect(Collectors.toList()),
        Matchers.hasItem(name));

    // Delete public IP
    computeManagerCow
        .computeManager()
        .networkManager()
        .publicIpAddresses()
        .deleteByResourceGroup(resourceGroupName, name);

    // Verify get response throws 404
    ManagementException e =
        assertThrows(
            ManagementException.class,
            () ->
                computeManagerCow
                    .computeManager()
                    .networkManager()
                    .publicIpAddresses()
                    .getByResourceGroup(resourceGroupName, name));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  // TODO add more tests building up to VM creation

  private static PublicIpAddress createPublicIp(String name) {
    return computeManagerCow
        .computeManager()
        .networkManager()
        .publicIpAddresses()
        .define(name)
        .withRegion(Region.US_EAST)
        .withExistingResourceGroup(resourceGroupName)
        .withDynamicIP()
        .create(
            Defaults.buildContext(
                new CreatePublicIpRequestData(resourceGroupName, name, Region.US_EAST)));
  }
}
