package bio.terra.cloudres.google.serviceusage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.cloudres.google.api.services.common.OperationUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.serviceusage.v1.model.BatchEnableServicesRequest;
import com.google.api.services.serviceusage.v1.model.Operation;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

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

  /** Enable services for a project. */
  public static void enableServices(String projectId, long projectNumber, List<String> services)
      throws Exception {
    List<String> serviceNames =
        services.stream().map(s -> serviceName(projectNumber, s)).collect(Collectors.toList());
    Operation operation =
        getServiceUsageCow()
            .services()
            .batchEnable(
                projectIdToName(projectId),
                new BatchEnableServicesRequest().setServiceIds(services))
            .execute();
    OperationCow<Operation> completedOperation =
        OperationUtils.pollUntilComplete(
            serviceUsageCow.operations().operationCow(operation),
            Duration.ofSeconds(5),
            Duration.ofSeconds(100));
    assertTrue(completedOperation.getOperationAdapter().getDone());
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
