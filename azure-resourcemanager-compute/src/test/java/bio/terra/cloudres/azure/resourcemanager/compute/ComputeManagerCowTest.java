package bio.terra.cloudres.azure.resourcemanager.compute;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.cloudres.azure.resourcemanager.common.AzureIntegrationUtils;
import bio.terra.cloudres.azure.resourcemanager.common.Defaults;
import bio.terra.cloudres.azure.resourcemanager.compute.data.*;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.models.*;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Test for {@link ComputeManagerCow}. Includes test cases for several resource types, building up
 * to creation of a VM.
 *
 * <p>The tests attempt to clean up after themselves, but also integrate with Janitor for resource
 * clean-up.
 */
@Tag("integration")
// Note: temporarily disabled because we have not yet added Azure test environment setup to vault.
@Disabled
public class ComputeManagerCowTest {
  private static final ComputeManagerCow computeManagerCow =
      ComputeManagerCow.create(
          IntegrationUtils.DEFAULT_CLIENT_CONFIG,
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
        computeManagerCow
            .computeManager()
            .networkManager()
            .publicIpAddresses()
            .getByResourceGroup(resourceGroupName, name);
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

  @Test
  public void createListDeleteDisk() {
    // Create disk
    final String name = getAzureName("disk");
    final int sizeInGB = 100;
    Disk createDiskResponse = createDisk(name, sizeInGB);

    // Verify get response
    Disk getResponse =
        computeManagerCow.computeManager().disks().getByResourceGroup(resourceGroupName, name);

    assertEquals(name, getResponse.name());
    assertEquals(createDiskResponse.sizeInGB(), getResponse.sizeInGB());
    assertEquals(getResponse.isAttachedToVirtualMachine(), false);

    // Verify list response
    Stream<Disk> listResponse =
        computeManagerCow.computeManager().disks().listByResourceGroup(resourceGroupName).stream();

    // There may be other disks from other tests
    assertThat(listResponse.map(Disk::name).collect(Collectors.toList()), Matchers.hasItem(name));

    // Delete disk
    computeManagerCow.computeManager().disks().deleteByResourceGroup(resourceGroupName, name);

    // Verify get response throws 404
    ManagementException e =
        assertThrows(
            ManagementException.class,
            () ->
                computeManagerCow
                    .computeManager()
                    .disks()
                    .getByResourceGroup(resourceGroupName, name));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  @Test
  public void createListDeleteNetwork() {
    // Create network
    final String name = getAzureName("network");
    final String subnetName = getAzureName("subnet");
    final String addressCidr = "192.168.0.0/16";
    final String subnetAddressCidr = "192.168.1.0/24";
    Network createNetworkResponse = createNetwork(name, subnetName, addressCidr, subnetAddressCidr);

    // Verify get response
    Network getResponse =
        computeManagerCow
            .computeManager()
            .networkManager()
            .networks()
            .getByResourceGroup(resourceGroupName, name);

    assertEquals(name, getResponse.name());
    assertEquals(createNetworkResponse.addressSpaces(), getResponse.addressSpaces());
    assertThat(getResponse.addressSpaces(), Matchers.hasItem(addressCidr));
    assertThat(getResponse.subnets().keySet(), Matchers.hasItem(subnetName));
    assertEquals(
        getResponse.subnets().get(subnetName).addressPrefix(),
        createNetworkResponse.subnets().get(subnetName).addressPrefix());
    assertEquals(getResponse.subnets().get(subnetName).addressPrefix(), subnetAddressCidr);

    // Verify list response
    Stream<Network> listResponse =
        computeManagerCow.computeManager().networkManager().networks()
            .listByResourceGroup(resourceGroupName).stream();

    // There may be other networks from other tests
    assertThat(
        listResponse.map(Network::name).collect(Collectors.toList()), Matchers.hasItem(name));

    // Delete network
    computeManagerCow
        .computeManager()
        .networkManager()
        .networks()
        .deleteByResourceGroup(resourceGroupName, name);

    // Verify get response throws 404
    ManagementException e =
        assertThrows(
            ManagementException.class,
            () ->
                computeManagerCow
                    .computeManager()
                    .networkManager()
                    .networks()
                    .getByResourceGroup(resourceGroupName, name));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  @Test
  public void createListDeleteVM() {
    final String vmName = getAzureName("vm");
    final String ipName = getAzureName("vm-public-ip");
    final String diskName = getAzureName("vm-disk");
    final String networkName = getAzureName("vm-network");
    final String subnetName = getAzureName("vm-subnet");
    final String addressCidr = "192.168.0.0/16";
    final String subnetAddressCidr = "192.168.1.0/24";
    final int sizeInGB = 100;

    // Create IP
    PublicIpAddress createIpResponse = createPublicIp(ipName);

    // Create disk
    Disk createDiskResponse = createDisk(diskName, sizeInGB);

    // Create network
    Network createNetworkResponse =
        createNetwork(networkName, subnetName, addressCidr, subnetAddressCidr);

    // Create VM
    VirtualMachine createVMResponse =
        createVM(vmName, createNetworkResponse, subnetName, createIpResponse, createDiskResponse);

    // Verify get response
    VirtualMachine getVMResponse =
        computeManagerCow
            .computeManager()
            .virtualMachines()
            .getByResourceGroup(resourceGroupName, vmName);

    assertEquals(vmName, getVMResponse.name());
    assertEquals(createVMResponse.regionName(), getVMResponse.regionName());
    assertEquals(createVMResponse.resourceGroupName(), getVMResponse.resourceGroupName());

    assertThat(
        getVMResponse.dataDisks().values().stream()
            .map(VirtualMachineDataDisk::name)
            .collect(Collectors.toList()),
        Matchers.hasItem(diskName));

    assertEquals(
        createVMResponse.getPrimaryPublicIPAddress().ipAddress(),
        getVMResponse.getPrimaryPublicIPAddress().ipAddress());

    // Verify list response
    Stream<VirtualMachine> listResponse =
        computeManagerCow.computeManager().virtualMachines().listByResourceGroup(resourceGroupName)
            .stream();

    // There may be other VM from other tests
    assertThat(
        listResponse.map(VirtualMachine::name).collect(Collectors.toList()),
        Matchers.hasItem(vmName));

    // Delete VM
    computeManagerCow
        .computeManager()
        .virtualMachines()
        .deleteByResourceGroup(resourceGroupName, vmName);

    // Verify get response throws 404
    ManagementException e =
        assertThrows(
            ManagementException.class,
            () ->
                computeManagerCow
                    .computeManager()
                    .networkManager()
                    .networks()
                    .getByResourceGroup(resourceGroupName, vmName));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  private static PublicIpAddress createPublicIp(String name) {
    return computeManagerCow
        .computeManager()
        .networkManager()
        .publicIpAddresses()
        .define(name)
        .withRegion(defaultRegion)
        .withExistingResourceGroup(resourceGroupName)
        .withDynamicIP()
        .withTag("crl.integration", "true")
        .create(
            Defaults.buildContext(
                new CreatePublicIpRequestData(resourceGroupName, name, defaultRegion)));
  }

  private static Disk createDisk(String name, int size) {
    return computeManagerCow
        .computeManager()
        .disks()
        .define(name)
        .withRegion(defaultRegion)
        .withExistingResourceGroup(resourceGroupName)
        .withData()
        .withSizeInGB(size)
        .withTag("crl.integration", "true")
        .create(
            Defaults.buildContext(
                new CreateDiskRequestData(resourceGroupName, name, defaultRegion, size)));
  }

  // Most of this is taken from this very helpful snippet
  // https://github.com/Azure-Samples/network-java-manage-virtual-network/blob/master/src/main/java/com/azure/resourcemanager/network/samples/ManageVirtualNetwork.java
  private static Network createNetwork(
      String networkName,
      String subnetName,
      String addressSpaceCidr,
      String subnetAddressSpaceCidr) {
    NetworkSecurityGroup subnetNsg =
        computeManagerCow
            .computeManager()
            .networkManager()
            .networkSecurityGroups()
            .define(subnetName)
            .withRegion(defaultRegion)
            .withExistingResourceGroup(resourceGroupName)
            .withTag("crl.integration", "true")
            .defineRule("AllowHttpInComing")
            .allowInbound()
            .fromAddress("INTERNET")
            .fromAnyPort()
            .toAnyAddress()
            .toPort(80)
            .withProtocol(SecurityRuleProtocol.TCP)
            .attach()
            .defineRule("DenyInternetOutGoing")
            .denyOutbound()
            .fromAnyAddress()
            .fromAnyPort()
            .toAddress("INTERNET")
            .toAnyPort()
            .withAnyProtocol()
            .attach()
            .create(
                Defaults.buildContext(
                    new CreateNetworkSecurityGroupRequestData(
                        resourceGroupName, subnetName, defaultRegion)));

    return computeManagerCow
        .computeManager()
        .networkManager()
        .networks()
        .define(networkName)
        .withRegion(defaultRegion)
        .withExistingResourceGroup(resourceGroupName)
        .withTag("crl.integration", "true")
        .withAddressSpace(addressSpaceCidr)
        .defineSubnet(subnetName)
        .withAddressPrefix(subnetAddressSpaceCidr)
        .withExistingNetworkSecurityGroup(subnetNsg)
        .attach()
        .create(
            Defaults.buildContext(
                new CreateNetworkRequestData(
                    resourceGroupName, networkName, defaultRegion, subnetName)));
  }

  private static VirtualMachine createVM(
      String name, Network network, String subnetName, PublicIpAddress ip, Disk disk) {
    // Note: these are public credentials, taken from:
    // https://github.com/Azure-Samples/network-java-manage-virtual-network/blob/master/src/main/java/com/azure/resourcemanager/network/samples/ManageVirtualNetwork.java
    final String userName = "tirekicker";
    final String sshKey =
        "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";
    return computeManagerCow
        .computeManager()
        .virtualMachines()
        .define(name)
        .withRegion(defaultRegion)
        .withExistingResourceGroup(resourceGroupName)
        .withExistingPrimaryNetwork(network)
        .withSubnet(subnetName)
        .withPrimaryPrivateIPAddressDynamic()
        .withNewPrimaryPublicIPAddress(ip.ipAddress())
        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
        .withRootUsername(userName)
        .withSsh(sshKey)
        .withExistingDataDisk(disk)
        .withTag("crl.integration", "true")
        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
        .create(
            Defaults.buildContext(
                new CreateVirtualMachineRequestData(
                    resourceGroupName, name, defaultRegion, network, subnetName, ip, disk)));
  }

  private static String getAzureName(String tag) {
    final String id = UUID.randomUUID().toString().substring(0, 6);
    return String.format("crl-integration-%s-%s", tag, id);
  }
}
