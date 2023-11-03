package bio.terra.cloudres.azure.resourcemanager.postgresflex;

import bio.terra.cloudres.azure.resourcemanager.common.AzureResourceCleanupRecorder;
import bio.terra.cloudres.azure.resourcemanager.common.AzureResponseLogger;
import bio.terra.cloudres.common.ClientConfig;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.resourcemanager.postgresqlflexibleserver.PostgreSqlManager;

public class Defaults {
  private Defaults() {}

  /**
   * Configures a client for CRL usage.
   *
   * <p>Example usage:
   *
   * <pre>
   *    crlConfigure(clientConfig, PostgreSqlManager.configure())
   *        .authenticate(tokenCredential, azureProfile);
   * </pre>
   *
   * @param clientConfig client configuration object to manage CRL behavior.
   * @param configurable Azure client to configure.
   * @return a configured Azure client.
   */
  public static PostgreSqlManager.Configurable crlConfigure(
      ClientConfig clientConfig, PostgreSqlManager.Configurable configurable) {
    return configurable.withLogOptions(
        new HttpLogOptions()
            .setRequestLogger(new AzureResourceCleanupRecorder(clientConfig))
            .setResponseLogger(new AzureResponseLogger(clientConfig))
            // Since we are providing our own loggers this value isn't actually used; however it
            // does need to be set to a value other than NONE for the loggers to fire.
            .setLogLevel(HttpLogDetailLevel.BASIC));
  }
}
