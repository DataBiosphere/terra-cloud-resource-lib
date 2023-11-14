package bio.terra.cloudres.azure.resourcemanager.common;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.cleanup.CleanupRecorder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.Context;
import com.azure.core.util.tracing.Tracer;
import com.azure.resourcemanager.resources.fluentcore.arm.AzureConfigurable;
import io.opentelemetry.api.trace.Span;
import java.util.Map;

/** Provides defaults for working with the Azure Resource Manager API. */
public class Defaults {
  private Defaults() {}

  static final String CLOUD_RESOURCE_REQUEST_DATA_KEY = "crlRequestData";

  /**
   * Builds a standard {@link Context} object for passing {@link ResourceManagerRequestData} to
   * Azure Resource Manager APIs. This should be used to enrich structured logging, and track
   * resource creations for clean-up.
   *
   * <p>Example usage:
   *
   * <pre>
   *     computeManager
   *         .networkManager()
   *         .publicIpAddresses()
   *         .define(name)
   *         .withRegion(region)
   *         .withExistingResourceGroup(resourceGroupName)
   *         .withDynamicIP()
   *         .create(
   *             Defaults.buildContext(
   *                 CreatePublicIpRequestData.builder()
   *                     .setTenantId(tenantId)
   *                     .setSubscriptionId(subscriptionId)
   *                     .setResourceGroupName(resourceGroupName)
   *                     .setName(name)
   *                     .setRegion(region)
   *                     .setIpAllocationMethod(IpAllocationMethod.DYNAMIC)
   *                     .build()));
   * </pre>
   */
  public static Context buildContext(ResourceManagerRequestData requestData) {
    return Context.of(
        Map.of(
            CLOUD_RESOURCE_REQUEST_DATA_KEY,
            requestData,
            // azure has built-in handling of tracing, nice!
            Tracer.PARENT_TRACE_CONTEXT_KEY,
            Span.current()));
  }

  /**
   * Configures a client for CRL usage.
   *
   * <p>Example usage:
   *
   * <pre>
   *    crlConfigure(clientConfig, ComputeManager.configure())
   *        .authenticate(tokenCredential, azureProfile);
   * </pre>
   *
   * @param clientConfig client configuration object to manage CRL behavior.
   * @param configurable Azure client to configure.
   * @return a configured Azure client.
   */
  public static <T extends AzureConfigurable<T>> T crlConfigure(
      ClientConfig clientConfig, AzureConfigurable<T> configurable) {
    return configurable.withLogOptions(
        new HttpLogOptions()
            .setRequestLogger(new AzureResourceCleanupRecorder(clientConfig))
            .setResponseLogger(new AzureResponseLogger(clientConfig))
            // Since we are providing our own loggers this value isn't actually used; however it
            // does need to be set to a value other than NONE for the loggers to fire.
            .setLogLevel(HttpLogDetailLevel.BASIC));
  }

  /**
   * Manually records a resource for cleanup. This is useful for cleaning up resources created
   * outside of Azure Resource Manager API calls.
   *
   * @param requestData the request data object
   * @param clientConfig the client config
   */
  public static void recordCleanup(
      ResourceManagerRequestData requestData, ClientConfig clientConfig) {
    requestData
        .resourceUidCreation()
        .ifPresent(
            uid ->
                CleanupRecorder.record(
                    uid, requestData.resourceCreationMetadata().orElse(null), clientConfig));
  }
}
