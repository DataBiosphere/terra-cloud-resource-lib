package bio.terra.cloudres.google.serviceusage.testing;

import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.serviceusage.ServiceUsageCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.serviceusage.v1.model.BatchEnableServicesRequest;
import com.google.api.services.serviceusage.v1.model.Operation;
import java.time.Duration;
import java.util.List;

/** Testing utilities for service usage. */
public class ServiceUsageUtils {
  private static ServiceUsageCow serviceUsageCow;

  public static ServiceUsageCow getServiceUsageCow() throws Exception {
    if (serviceUsageCow == null) {
      serviceUsageCow =
          ServiceUsageCow.create(
              IntegrationUtils.DEFAULT_CLIENT_CONFIG,
              IntegrationCredentials.getAdminGoogleCredentialsOrDie());
    }
    return serviceUsageCow;
  }

  /**
   * Enables batch services for a project.
   *
   * @param projectId: The projectId to enable services on.
   * @param services: Services to be enabled. See {@link BatchEnableServicesRequest}
   */
  public static void enableServices(String projectId, List<String> services) throws Exception {
    Operation operation =
        getServiceUsageCow()
            .services()
            .batchEnable(
                projectIdToName(projectId),
                new BatchEnableServicesRequest().setServiceIds(services))
            .execute();
    OperationTestUtils.pollAndAssertSuccess(
        serviceUsageCow.operations().operationCow(operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(100));
  }

  private static String projectIdToName(String projectId) {
    return "projects/" + projectId;
  }

  /**
   * Create a string matching the service name on {@link GoogleApiServiceusageV1Service#getName()},
   * e.g. projects/123/services/serviceusage.googleapis.com.
   */
  private static String serviceName(long projectNumber, String apiId) {
    return String.format("projects/%d/services/%s", projectNumber, apiId);
  }
}
