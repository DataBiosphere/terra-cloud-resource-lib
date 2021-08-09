package bio.terra.cloudres.azure.resource.manager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.azure.resouce.manager.AzureResourceConfiguration;
import bio.terra.cloudres.azure.resouce.manager.AzureResourceManagerClient;
import bio.terra.cloudres.azure.resouce.manager.BillingProfileModel;
import bio.terra.cloudres.azure.resouce.manager.Credentials;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

// @Tag("integration")
public class AzureResourceTest {

  AzureResourceManagerClient client;
  BillingProfileModel profileModel;
  AzureResourceConfiguration resourceConfiguration;
  String testPropsFileName = "test-azure.properties";
  String templatePath =

  @BeforeEach
  private void setup() throws IOException {
    profileModel = BillingProfileModel.getFromPropsFile(testPropsFileName);
    Credentials credentials = Credentials.getFromPropsFile(testPropsFileName);
    resourceConfiguration = new AzureResourceConfiguration(credentials);
    client = new AzureResourceManagerClient(resourceConfiguration, profileModel);
  }

  @Test
  public void assertTest() {
    assertTrue(true);
    client.createManagedApplication("azureDeploy.json");
  }
}
