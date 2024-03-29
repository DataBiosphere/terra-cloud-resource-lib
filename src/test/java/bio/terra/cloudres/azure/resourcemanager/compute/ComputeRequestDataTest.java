package bio.terra.cloudres.azure.resourcemanager.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bio.terra.cloudres.azure.resourcemanager.compute.data.BaseComputeRequestData;
import bio.terra.cloudres.azure.resourcemanager.compute.data.CreateDiskRequestData;
import bio.terra.cloudres.azure.resourcemanager.compute.data.CreateVirtualMachineRequestData;
import bio.terra.janitor.model.AzureDisk;
import bio.terra.janitor.model.AzureResourceGroup;
import bio.terra.janitor.model.AzureVirtualMachine;
import bio.terra.janitor.model.CloudResourceUid;
import com.azure.core.management.Region;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class ComputeRequestDataTest {

  @Test
  public void serializeCreateDisk() {
    CreateDiskRequestData createDisk =
        CreateDiskRequestData.builder()
            .setName("my-disk")
            .setTenantId("my-tenant")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .setRegion(Region.US_EAST)
            .setSize(500)
            .build();

    assertEquals(ComputeManagerOperation.AZURE_CREATE_DISK, createDisk.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\",\"name\":\"my-disk\",\"region\":\"eastus\",\"size\":500}",
        createDisk.serialize().toString());
    assertEquals(
        Optional.of(
            new CloudResourceUid()
                .azureDisk(
                    new AzureDisk()
                        .resourceGroup(azureResourceGroup(createDisk))
                        .diskName("my-disk"))),
        createDisk.resourceUidCreation());
  }

  @Test
  public void serializeCreateVirtualMachine() {
    CreateVirtualMachineRequestData createVirtualMachine =
        CreateVirtualMachineRequestData.builder()
            .setTenantId("my-tenant")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .setRegion(Region.US_EAST)
            .setName("my-vm")
            .setPublicIpAddress(mockPublicIpAddress())
            .setDisk(mockDisk())
            .setNetwork(mockNetwork())
            .setSubnetName("my-subnet")
            .setImage("my-image")
            .build();

    assertEquals(ComputeManagerOperation.AZURE_CREATE_VM, createVirtualMachine.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\","
            + "\"name\":\"my-vm\",\"region\":\"eastus\","
            + "\"network\":\"my-network\",\"subnetName\":\"my-subnet\",\"ip\":null,"
            + "\"disk\":\"my-disk\",\"image\":\"my-image\"}",
        createVirtualMachine.serialize().toString());
    assertEquals(
        Optional.of(
            new CloudResourceUid()
                .azureVirtualMachine(
                    new AzureVirtualMachine()
                        .resourceGroup(azureResourceGroup(createVirtualMachine))
                        .vmName("my-vm"))),
        createVirtualMachine.resourceUidCreation());
  }

  @Test
  public void serializeCreateVirtualMachineWithoutPublicIp() {
    CreateVirtualMachineRequestData createVirtualMachine =
        CreateVirtualMachineRequestData.builder()
            .setTenantId("my-tenant")
            .setSubscriptionId("my-sub")
            .setResourceGroupName("my-rg")
            .setRegion(Region.US_EAST)
            .setName("my-vm")
            .setDisk(mockDisk())
            .setNetwork(mockNetwork())
            .setSubnetName("my-subnet")
            .setImage("my-image")
            .build();

    assertEquals(ComputeManagerOperation.AZURE_CREATE_VM, createVirtualMachine.cloudOperation());
    assertEquals(
        "{\"tenantId\":\"my-tenant\",\"subscriptionId\":\"my-sub\",\"resourceGroupName\":\"my-rg\","
            + "\"name\":\"my-vm\",\"region\":\"eastus\","
            + "\"network\":\"my-network\",\"subnetName\":\"my-subnet\","
            + "\"disk\":\"my-disk\",\"image\":\"my-image\"}",
        createVirtualMachine.serialize().toString());
    assertEquals(
        Optional.of(
            new CloudResourceUid()
                .azureVirtualMachine(
                    new AzureVirtualMachine()
                        .resourceGroup(azureResourceGroup(createVirtualMachine))
                        .vmName("my-vm"))),
        createVirtualMachine.resourceUidCreation());
  }

  private static PublicIpAddress mockPublicIpAddress() {
    PublicIpAddress mock = mock(PublicIpAddress.class);
    when(mock.name()).thenReturn("my-ip");
    return mock;
  }

  private static Disk mockDisk() {
    Disk mock = mock(Disk.class);
    when(mock.name()).thenReturn("my-disk");
    return mock;
  }

  private static Network mockNetwork() {
    Network mock = mock(Network.class);
    when(mock.name()).thenReturn("my-network");
    return mock;
  }

  private static NetworkSecurityGroup mockNetworkSecurityGroup() {
    NetworkSecurityGroup mock = mock(NetworkSecurityGroup.class);
    when(mock.name()).thenReturn("my-nsg");
    return mock;
  }

  private AzureResourceGroup azureResourceGroup(BaseComputeRequestData requestData) {
    return new AzureResourceGroup()
        .tenantId(requestData.tenantId())
        .subscriptionId(requestData.subscriptionId())
        .resourceGroupName(requestData.resourceGroupName());
  }
}
