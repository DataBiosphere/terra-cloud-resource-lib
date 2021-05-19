package bio.terra.cloudres.google.serviceusage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.serviceusage.v1.model.BatchEnableServicesRequest;
import com.google.api.services.serviceusage.v1.model.GoogleApiServiceusageV1Service;
import com.google.api.services.serviceusage.v1.model.ListServicesResponse;
import com.google.api.services.serviceusage.v1.model.Operation;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class ServiceUsageCowTest {
  private static final String STORAGE_SERVICE_ID = "storage-api.googleapis.com";
  private static final String ENABLED_FILTER = "state:ENABLED";

  private static ServiceUsageCow defaultServiceUsage()
      throws GeneralSecurityException, IOException {
    return ServiceUsageCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
  }

  @Test
  public void listAndEnableServices() throws Exception {
    ServiceUsageCow serviceUsage = defaultServiceUsage();
    Project project = ProjectUtils.executeCreateProject();
    String projectName = projectIdToName(project.getProjectId());
    String storageServiceName = serviceName(project, STORAGE_SERVICE_ID);

    ListServicesResponse response1 =
        serviceUsage.services().list(projectName).setFilter(ENABLED_FILTER).execute();
    assertNull(response1.getServices());

    Operation operation =
        serviceUsage
            .services()
            .batchEnable(
                projectName,
                new BatchEnableServicesRequest()
                    .setServiceIds(ImmutableList.of(STORAGE_SERVICE_ID)))
            .execute();
    OperationTestUtils.pollAndAssertSuccess(
        serviceUsage.operations().operationCow(operation),
        Duration.ofSeconds(5),
        Duration.ofSeconds(60));

    ListServicesResponse response2 =
        serviceUsage.services().list(projectName).setFilter(ENABLED_FILTER).execute();
    List<String> services2 =
        response2.getServices().stream()
            .map(GoogleApiServiceusageV1Service::getName)
            .collect(Collectors.toList());
    assertThat(services2, Matchers.hasItem(storageServiceName));
  }

  @Test
  public void batchEnableSerialize() throws Exception {
    ServiceUsageCow.Services.BatchEnable batchEnable =
        defaultServiceUsage()
            .services()
            .batchEnable(
                "projects/my-project",
                new BatchEnableServicesRequest()
                    .setServiceIds(ImmutableList.of(STORAGE_SERVICE_ID)));
    assertEquals(
        "{\"parent\":\"projects/my-project\","
            + "\"content\":{\"serviceIds\":[\"storage-api.googleapis.com\"]}}",
        batchEnable.serialize().toString());
  }

  @Test
  public void listSerialize() throws Exception {
    ServiceUsageCow.Services.List list =
        defaultServiceUsage()
            .services()
            .list("projects/my-project")
            .setFilter(ENABLED_FILTER)
            .setFields("my-fields");
    assertEquals(
        "{\"parent\":\"projects/my-project\","
            + "\"filter\":\"state:ENABLED\",\"fields\":\"my-fields\"}",
        list.serialize().toString());
  }

  private static String projectIdToName(String projectId) {
    return "projects/" + projectId;
  }

  /**
   * Create a string matching the service name on {@link GoogleApiServiceusageV1Service#getName()},
   * e.g. projects/123/services/serviceusage.googleapis.com.
   */
  private static String serviceName(Project project, String apiId) {
    return String.format("projects/%d/services/%s", project.getProjectNumber(), apiId);
  }
}
