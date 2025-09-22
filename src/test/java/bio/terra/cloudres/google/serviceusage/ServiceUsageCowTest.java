package bio.terra.cloudres.google.serviceusage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.testing.OperationTestUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.v3.model.Project;
import com.google.api.services.serviceusage.v1beta1.model.BatchEnableServicesRequest;
import com.google.api.services.serviceusage.v1beta1.model.GoogleApiServiceusageV1Service;
import com.google.api.services.serviceusage.v1beta1.model.ListConsumerOverridesResponse;
import com.google.api.services.serviceusage.v1beta1.model.ListServicesResponse;
import com.google.api.services.serviceusage.v1beta1.model.Operation;
import com.google.api.services.serviceusage.v1beta1.model.QuotaOverride;
import com.google.api.services.serviceusage.v1beta1.model.Service;
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

  public static final long OVERRIDE_VALUE_BYTES = 209_715_199L; // previously 1_099_511_627_776L;


  private static final String STORAGE_SERVICE_ID = "storage-api.googleapis.com";
  private static final String ENABLED_FILTER = "state:ENABLED";
  public static final String QUOTA_METRIC = "bigquery.googleapis.com/quota/query/usage";
  public static final String QUOTA_UNIT = "1/d/{project}"; // no substitution - literal {}s

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
        response2.getServices().stream().map(Service::getName).collect(Collectors.toList());
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

  @Test
  public void createListConsumerQuotaOverride() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    // name is of the form `projects/415104041262`
    long projectNumber = Long.parseLong(project.getName().substring("projects/".length()));

    String parent =
        String.format(
            "projects/%d/services/bigquery.googleapis.com/consumerQuotaMetrics/"
                + "bigquery.googleapis.com%%2Fquota%%2Fquery%%2Fusage/limits/%%2Fd%%2Fproject",
            projectNumber);

    QuotaOverride quotaOverrideForRequest = buildQuotaOverride(projectNumber);
    ServiceUsageCow.Services.ConsumerQuotaMetrics.Limits.ConsumerOverrides.Create create =
        defaultServiceUsage()
            .services()
            .consumerQuotaMetrics()
            .limits()
            .consumerOverrides()
            .create(parent, quotaOverrideForRequest)
            .setForce(true);
    assertNotNull(create);

    OperationTestUtils.pollAndAssertSuccess(
        defaultServiceUsage().operations().operationCow(create.execute()),
        Duration.ofSeconds(5),
        Duration.ofSeconds(60));

    ServiceUsageCow.Services.ConsumerQuotaMetrics.Limits.ConsumerOverrides.List list =
        defaultServiceUsage()
            .services()
            .consumerQuotaMetrics()
            .limits()
            .consumerOverrides()
            .list(parent);
    ListConsumerOverridesResponse response = list.execute();
    assertNotNull(response);
    assertEquals(1, response.getOverrides().size());

    QuotaOverride quotaOverrideRetrieved = response.getOverrides().get(0);
    // returned name is of the form
    // "projects/847486415500/services/bigquery.googleapis.com/consumerQuotaMetrics/bigquery.googleapis.com%2Fquota%2Fquery%2Fusage/limits/%2Fd%2Fproject/consumerOverrides/Cg1RdW90YU92ZXJyaWRl"
    // content name is shorter:
    // "projects/847486415500/services/bigquery.googleapis.com/consumerQuotaMetrics/bigquery.googleapis.com%2Fquota%2Fquery%2Fusage"
    // Both of these are opaque and not to be relied on, but I can't help but think the fact that
    // one is a prefix of the other is invariant.
    assertTrue(quotaOverrideRetrieved.getName().contains(quotaOverrideForRequest.getName()));
    assertEquals(OVERRIDE_VALUE_BYTES, quotaOverrideRetrieved.getOverrideValue());
    assertTrue(
        null == quotaOverrideRetrieved.getDimensions()
            || quotaOverrideRetrieved.getDimensions().isEmpty());
    assertNull(quotaOverrideRetrieved.getMetric()); // not returned for some reason
    assertNull(quotaOverrideRetrieved.getUnit()); // not returned apparently
  }

  private QuotaOverride buildQuotaOverride(Long projectNumber) {
    var result = new QuotaOverride();
    result.setMetric(QUOTA_METRIC);
    // fill in the project number for the quota limit name
    result.setName(
        String.format(
            "projects/%d/services/bigquery.googleapis.com/"
                + "consumerQuotaMetrics/bigquery.googleapis.com%%2Fquota%%2Fquery%%2Fusage",
            projectNumber));
    result.setOverrideValue(OVERRIDE_VALUE_BYTES);
    result.setUnit(QUOTA_UNIT);
    return result;
  }

  private static String projectIdToName(String projectId) {
    return "projects/" + projectId;
  }

  /**
   * Create a string matching the service name on {@link GoogleApiServiceusageV1Service#getName()},
   * e.g. projects/123/services/serviceusage.googleapis.com.
   */
  private static String serviceName(Project project, String apiId) {
    return String.format("%s/services/%s", project.getName(), apiId);
  }
}
