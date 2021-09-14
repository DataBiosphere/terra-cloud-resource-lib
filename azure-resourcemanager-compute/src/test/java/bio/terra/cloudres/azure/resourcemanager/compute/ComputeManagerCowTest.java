package bio.terra.cloudres.azure.resourcemanager.compute;

import static bio.terra.cloudres.azure.resourcemanager.compute.ComputeManagerIntegrationUtils.defaultComputeManagerCow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.cloudres.azure.resourcemanager.resources.Defaults;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.models.*;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.PublicIpAddress;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;

@Tag("integration")
public class ComputeManagerCowTest {
  private static final ComputeManagerCow computeManagerCow = defaultComputeManagerCow();
  private static final Region DEFAULT_REGION = Region.US_EAST;

  // TODO: THESE ARE PUBLIC CREDS, TAKEN FROM
  // From https://github.com/Azure-Samples/network-java-manage-virtual-network/blob/master/src/main/java/com/azure/resourcemanager/network/samples/ManageVirtualNetwork.java
  private static final String userName = "tirekicker";
  private static final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

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
    assertThat(listResponse.map(PublicIpAddress::name).collect(Collectors.toList()),
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

  @Test
  public void createListDeleteDisk() {
    final String name = getAzureName("disk");
    final int sizeInGB = 100;
    Disk createDiskResponse = createDisk(name, sizeInGB);

    // Verify get response
    Disk getResponse =
            computeManagerCow
                    .computeManager()
                    .disks()
                    .getByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), name);

    assertEquals(name, getResponse.name());
    assertEquals(createDiskResponse.sizeInGB(), getResponse.sizeInGB());
    //TODO: this check could eventually be true depending on how we go about constructing create vm request (i.e. disk could be part of that or before)
    assertEquals(getResponse.isAttachedToVirtualMachine(), false);


    // Verify list response
    Stream<Disk> listResponse =
            computeManagerCow.computeManager().disks()
                    .listByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
                    .stream();
    // There may be other disks from other tests.
    assertThat(listResponse.map(Disk::name).collect(Collectors.toList()),
            Matchers.hasItem(name));

    // Delete disk
    computeManagerCow
            .computeManager()
            .disks()
            .deleteByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), name);

    // Verify get response throws 404
    ManagementException e =
            assertThrows(
                    ManagementException.class,
                    () ->
                            computeManagerCow
                                    .computeManager()
                                    .disks()
                                    .getByResourceGroup(
                                            ComputeManagerIntegrationUtils.getReusableResourceGroup(), name));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  @Test
  public void createListDeleteNetwork() {
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
                    .getByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), name);

    assertEquals(name, getResponse.name());
    assertEquals(createNetworkResponse.addressSpaces(), getResponse.addressSpaces());
    assertThat(getResponse.addressSpaces(), Matchers.hasItem(addressCidr));
    assertThat(getResponse.subnets().keySet(), Matchers.hasItem(subnetName));
    assertEquals(getResponse.subnets().get(subnetName).addressPrefix(), createNetworkResponse.subnets().get(subnetName).addressPrefix());
    assertEquals(getResponse.subnets().get(subnetName).addressPrefix(), subnetAddressCidr);

    // Verify list response
    Stream<Network> listResponse =
            computeManagerCow.computeManager()
                    .networkManager()
                    .networks()
                    .listByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
                    .stream();

    // There may be other Network from other tests.
    assertThat(listResponse.map(Network::name).collect(Collectors.toList()), Matchers.hasItem(name));

    // Delete Network
    computeManagerCow
            .computeManager()
            .networkManager()
            .networks()
            .deleteByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), name);

    // Verify get response throws 404
    ManagementException e =
            assertThrows(
                    ManagementException.class,
                    () ->
                            computeManagerCow
                                    .computeManager()
                                    .networkManager()
                                    .networks()
                                    .getByResourceGroup(
                                            ComputeManagerIntegrationUtils.getReusableResourceGroup(), name));
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

    PublicIpAddress createIpResponse = createPublicIp(ipName);

    Disk createDiskResponse = createDisk(diskName, sizeInGB);

    Network createNetworkResponse = createNetwork(networkName, subnetName, addressCidr, subnetAddressCidr);

    VirtualMachine createVMResponse = createVM(vmName, createNetworkResponse, subnetName, createIpResponse, createDiskResponse);

    // Verify get response
    VirtualMachine getVMResponse =
            computeManagerCow
                    .computeManager()
                    .virtualMachines()
                    .getByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), vmName);

    assertEquals(vmName, getVMResponse.name());
    assertEquals(createVMResponse.regionName(), getVMResponse.regionName());
    assertEquals(createVMResponse.resourceGroupName(), getVMResponse.resourceGroupName());

    assertThat(getVMResponse.dataDisks().values().stream().map(VirtualMachineDataDisk::name).collect(Collectors.toList()),
            Matchers.hasItem(diskName));

    assertEquals(createVMResponse.getPrimaryPublicIPAddress().ipAddress(), getVMResponse.getPrimaryPublicIPAddress().ipAddress());

    // Verify list response
    Stream<VirtualMachine> listResponse =
            computeManagerCow.computeManager()
                    .virtualMachines()
                    .listByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
                    .stream();

    // There may be other VM from other tests.
    assertThat(listResponse.map(VirtualMachine::name).collect(Collectors.toList()),
            Matchers.hasItem(vmName));

    // Delete Network
    computeManagerCow
            .computeManager()
            .virtualMachines()
            .deleteByResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup(), vmName);

    // Verify get response throws 404
    ManagementException e =
            assertThrows(
                    ManagementException.class,
                    () ->
                            computeManagerCow
                                    .computeManager()
                                    .networkManager()
                                    .networks()
                                    .getByResourceGroup(
                                            ComputeManagerIntegrationUtils.getReusableResourceGroup(), vmName));
    assertEquals(404, e.getResponse().getStatusCode());
  }

  //TODO test async vm creation if necessary

  private static PublicIpAddress createPublicIp(String name) {
    return computeManagerCow
        .computeManager()
        .networkManager()
        .publicIpAddresses()
        .define(name)
        .withRegion(Region.US_EAST)
        .withExistingResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
        .withDynamicIP()
        .create(
            Defaults.buildContext(
                ComputeManagerOperation.AZURE_CREATE_PUBLIC_IP,
                new PublicIpRequestData(
                    ComputeManagerIntegrationUtils.getReusableResourceGroup(),
                    name,
                    Region.US_EAST)));
  }

  private static Disk createDisk(String name, int size) {
    return computeManagerCow
            .computeManager()
            .disks()
            .define(name)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
            .withData()
            .withSizeInGB(size)
//           .withSku(DiskSkuTypes.ULTRA_SSD_LRS)
//            .withTags() should we label these resources?
            .create(Defaults.buildContext(
                    ComputeManagerOperation.AZURE_CREATE_DISK,
                    new DiskRequestData(
                            ComputeManagerIntegrationUtils.getReusableResourceGroup(),
                            name,
                            Region.US_EAST,
                            size
                    )
            ));
  }

  private static Network createNetwork(String networkName, String subnetName, String addressSpaceCidr, String subnetAddressSpaceCidr) {
    //TODO Its possible tuning is required, in particular the port in the subnet network security group
    //most of this is taken from this very helpful snippet https://github.com/Azure-Samples/network-java-manage-virtual-network/blob/master/src/main/java/com/azure/resourcemanager/network/samples/ManageVirtualNetwork.java
    NetworkSecurityGroup subnetNsg = computeManagerCow
            .computeManager()
            .networkManager()
            .networkSecurityGroups()
            .define(subnetName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
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
            .create();

    return computeManagerCow
            .computeManager()
            .networkManager()
            .networks()
            .define(networkName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
            .withAddressSpace(addressSpaceCidr)
            .defineSubnet(subnetName)
              .withAddressPrefix(subnetAddressSpaceCidr)
              .withExistingNetworkSecurityGroup(subnetNsg)
              .attach()
            .create(Defaults.buildContext(
                    ComputeManagerOperation.AZURE_CREATE_NETWORK,
                    new NetworkRequestData(
                            ComputeManagerIntegrationUtils.getReusableResourceGroup(),
                            networkName,
                            DEFAULT_REGION,
                            subnetName
                    )
            ));
  }

  private static VirtualMachine createVM(String name, Network network, String subnetName, PublicIpAddress ip, Disk disk) {
    VirtualMachine vm = computeManagerCow.computeManager().virtualMachines().define(name)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(ComputeManagerIntegrationUtils.getReusableResourceGroup())
            .withExistingPrimaryNetwork(network)
            .withSubnet(subnetName)
            .withPrimaryPrivateIPAddressDynamic()
            .withNewPrimaryPublicIPAddress(ip.ipAddress())
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername(userName)
            .withSsh(sshKey)
            .withExistingDataDisk(disk)
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .create(Defaults.buildContext(
                    ComputeManagerOperation.AZURE_CREATE_VM,
                    new VMRequestData(
                            ComputeManagerIntegrationUtils.getReusableResourceGroup(),
                            name,
                            DEFAULT_REGION,
                            network,
                            subnetName,
                            ip,
                            disk)
              ));

    return vm;
  }

  private static String getAzureName(String tag) {
    final String id = UUID.randomUUID().toString().substring(0,6);
    return String.format("crl-integration-%s-%s", tag, id);
  }
}
