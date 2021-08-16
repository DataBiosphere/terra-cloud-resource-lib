package bio.terra.cloudres.azure.resource.manager;

import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.azure.resouce.manager.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

// @Tag("integration")
public class AzureResourceTest {

  AzureResourceManagerClient client;
  BillingProfileModel profileModel;
  AzureResourceConfiguration resourceConfiguration;
  String testPropsFileName = "test-azure.properties";

  @BeforeEach
  private void setup() throws IOException {
    profileModel = BillingProfileModel.getFromPropsFile(testPropsFileName);
//    SecretCredentials credentials = SecretCredentials.getFromPropsFile(testPropsFileName);
    UsernamePasswordCredentials credentials = UsernamePasswordCredentials.getFromPropsFile(testPropsFileName);
    resourceConfiguration = new AzureResourceConfiguration(credentials);
    client = new AzureResourceManagerClient(resourceConfiguration, profileModel);

  }

  @Test
  public void assertTest() {
    assertTrue(true);
    client.createManagedApplication("azureDeploy.json");
  }
}
