package bio.terra.cloudres.azure.resourcemanager.compute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.cloudres.azure.resourcemanager.common.AzureIntegrationUtils;
import bio.terra.cloudres.azure.resourcemanager.common.Defaults;
import bio.terra.cloudres.azure.resourcemanager.compute.data.CreatePublicIpRequestData;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link ComputeManager} using CRL.
 *
 * <p>This test does not attempt to fully cover {@link ComputeManager} functionality, but creates
 * some representative resources to exercise CRL functionality.
 */
@Tag("integration")
// Note: temporarily disabled because we have not yet added Azure test environment setup to vault.
@Disabled
public class ComputeManagerTest {
  private static final ComputeManager computeManager =
      Defaults.crlConfigure(IntegrationUtils.DEFAULT_CLIENT_CONFIG, ComputeManager.configure())
          .authenticate(
              AzureIntegrationUtils.getAdminAzureCredentialsOrDie(),
              AzureIntegrationUtils.getUserAzureProfileOrDie());
  private static final String resourceGroupName = AzureIntegrationUtils.getResuableResourceGroup();
  private static final Region defaultRegion = Region.US_EAST;

  @Test
  public void createListDeletePublicIp() {
    // Create public IP
    final String name = getAzureName("public-ip");
    PublicIpAddress createResponse = createPublicIp(name);

    // Verify get response
    PublicIpAddress getResponse =
        computeManager
            .networkManager()
            .publicIpAddresses()
            .getByResourceGroup(resourceGroupName, name);
    assertEquals(name, getResponse.name());
    assertEquals(createResponse.fqdn(), getResponse.fqdn());
    assertEquals(createResponse.ipAddress(), getResponse.ipAddress());

    // Verify list response
    Stream<PublicIpAddress> listResponse =
        computeManager.networkManager().publicIpAddresses().listByResourceGroup(resourceGroupName)
            .stream();
    // There may be other public IPs from other tests.
    assertThat(
        listResponse.map(PublicIpAddress::name).collect(Collectors.toList()),
        Matchers.hasItem(name));

    // Delete public IP
    computeManager
        .networkManager()
        .publicIpAddresses()
        .deleteByResourceGroup(resourceGroupName, name);

    // Verify get response throws 404
    ManagementException e =
        assertThrows(
            ManagementException.class,
            () ->
                computeManager
                    .networkManager()
                    .publicIpAddresses()
                    .getByResourceGroup(resourceGroupName, name));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  private static PublicIpAddress createPublicIp(String name) {
    return computeManager
        .networkManager()
        .publicIpAddresses()
        .define(name)
        .withRegion(defaultRegion)
        .withExistingResourceGroup(resourceGroupName)
        .withDynamicIP()
        .withTag("crl.integration", "true")
        .create(
            Defaults.buildContext(
                CreatePublicIpRequestData.builder()
                    .setResourceGroupName(resourceGroupName)
                    .setName(name)
                    .setRegion(defaultRegion)
                    .setIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                    .build()));
  }

  private static String getAzureName(String tag) {
    final String id = UUID.randomUUID().toString().substring(0, 6);
    return String.format("crl-integration-%s-%s", tag, id);
  }
}
