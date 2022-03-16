package bio.terra.cloudres.azure.resourcemanager.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.common.OperationData;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Integration test which verifies invocation of {@link AzureResponseLogger} from real Azure cloud
 * calls.
 */
@Tag("integration")
public class AzureResponseLoggerTest {

  @Mock private OperationAnnotator mockOperationAnnotator = Mockito.mock(OperationAnnotator.class);

  private ArgumentCaptor<OperationData> operationDataCaptor =
      ArgumentCaptor.forClass(OperationData.class);

  @Test
  public void testRecordEvent() {
    // Send an Azure request to list resource groups
    List<ResourceGroup> resourceGroups =
        setUpResourceManager().resourceGroups().list().stream().collect(Collectors.toList());

    // Verify AzureResponseLogger was invoked
    Mockito.verify(mockOperationAnnotator).recordOperation(operationDataCaptor.capture());

    // Verify OperationData values
    OperationData operationData = operationDataCaptor.getValue();
    assertThat(operationData.duration().toMillis(), Matchers.greaterThan(0L));
    assertTrue(operationData.tryCount().isPresent());
    assertEquals(OptionalInt.of(200), operationData.httpStatusCode());
    assertEquals(
        ResourceManagerOperation.AZURE_RESOURCE_MANAGER_UNKNOWN_OPERATION,
        operationData.cloudOperation());
    assertThat(
        operationData.requestData().entrySet(),
        Matchers.hasItem(Map.entry("requestMethod", new JsonPrimitive("GET"))));
    assertThat(operationData.requestData().keySet(), Matchers.hasItem("requestUrl"));

    // For good measure verify the Azure response contains our test resource group
    MatcherAssert.assertThat(
        resourceGroups.stream().map(ResourceGroup::name).collect(Collectors.toList()),
        Matchers.hasItem(AzureIntegrationUtils.DEFAULT_AZURE_RESOURCE_GROUP));
  }

  private ResourceManager setUpResourceManager() {
    AzureProfile profile = AzureIntegrationUtils.DEFAULT_AZURE_PROFILE;
    return ResourceManager.configure()
        .withLogOptions(
            new HttpLogOptions()
                .setResponseLogger(new AzureResponseLogger(mockOperationAnnotator))
                .setLogLevel(HttpLogDetailLevel.BASIC))
        .authenticate(AzureIntegrationUtils.getAdminAzureCredentialsOrDie(), profile)
        .withSubscription(profile.getSubscriptionId());
  }
}
