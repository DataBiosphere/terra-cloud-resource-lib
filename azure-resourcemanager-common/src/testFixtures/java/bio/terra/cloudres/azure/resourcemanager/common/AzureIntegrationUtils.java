package bio.terra.cloudres.azure.resourcemanager.common;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.ClientSecretCredentialBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AzureIntegrationUtils {
  /** Path to Azure properties file. */
  private static final String AZURE_PROPERTIES_PATH = "integration_azure_env.properties";

  /** Property prefix for properties in {@link #AZURE_PROPERTIES_PATH}. */
  private static final String AZURE_PROPERTY_PREFIX = "integration.azure";

  /**
   * Gets an Azure TokenCredential object for an Azure admin account. This account has the roles
   * needed to operate the CRL APIs in the integration test project, e.g. create and delete
   * resources.
   *
   * @return TokenCredential
   */
  public static TokenCredential getAdminAzureCredentialsOrDie() {
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(AZURE_PROPERTIES_PATH)) {
      Properties properties = new Properties();
      properties.load(in);

      final String clientId = properties.getProperty(AZURE_PROPERTY_PREFIX + ".admin.clientId");
      if (clientId == null) {
        throw new RuntimeException(
            "Unable to read Azure admin client id from " + AZURE_PROPERTIES_PATH);
      }
      final String clientSecret =
          properties.getProperty(AZURE_PROPERTY_PREFIX + ".admin.clientSecret");
      if (clientSecret == null) {
        throw new RuntimeException(
            "Unable to read Azure admin application secret from " + AZURE_PROPERTIES_PATH);
      }
      final String tenantId = properties.getProperty(AZURE_PROPERTY_PREFIX + ".admin.tenantId");
      if (tenantId == null) {
        throw new RuntimeException(
            "Unable to read Azure admin tenant id from " + AZURE_PROPERTIES_PATH);
      }
      return new ClientSecretCredentialBuilder()
          .clientId(clientId)
          .clientSecret(clientSecret)
          .tenantId(tenantId)
          .build();

    } catch (IOException e) {
      throw new RuntimeException(
          "Unable to load Azure properties file from " + AZURE_PROPERTIES_PATH, e);
    }
  }

  /**
   * Gets an AzureProfile object for a non-admin client.
   *
   * @return AzureProfile
   */
  public static AzureProfile getUserAzureProfileOrDie() {
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(AZURE_PROPERTIES_PATH)) {
      Properties properties = new Properties();
      properties.load(in);

      final String tenantId = properties.getProperty(AZURE_PROPERTY_PREFIX + ".user.tenantId");
      if (tenantId == null) {
        throw new RuntimeException(
            "Unable to read Azure user tenant id from " + AZURE_PROPERTIES_PATH);
      }
      final String subscriptionId =
          properties.getProperty(AZURE_PROPERTY_PREFIX + ".user.subscriptionId");
      if (subscriptionId == null) {
        throw new RuntimeException(
            "Unable to read Azure user subscription id from " + AZURE_PROPERTIES_PATH);
      }
      return new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to load Azure properties file from " + AZURE_PROPERTIES_PATH, e);
    }
  }

  /**
   * Gets a resource group in which to create resources.
   *
   * @return resource group name.
   */
  public static String getResuableResourceGroup() {
    try (InputStream in =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(AZURE_PROPERTIES_PATH)) {
      Properties properties = new Properties();
      properties.load(in);

      final String resourceGroupName =
          properties.getProperty(AZURE_PROPERTY_PREFIX, ".resourceGroupName");
      if (resourceGroupName == null) {
        throw new RuntimeException(
            "Unable to read Azure resource group from " + AZURE_PROPERTIES_PATH);
      }
      return resourceGroupName;
    } catch (Exception e) {
      throw new RuntimeException(
          "Unable to load Azure properties file from " + AZURE_PROPERTIES_PATH, e);
    }
  }
}
