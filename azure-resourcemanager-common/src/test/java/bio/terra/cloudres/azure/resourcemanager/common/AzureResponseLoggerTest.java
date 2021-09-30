package bio.terra.cloudres.azure.resourcemanager.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 * Integration test which verifies invocation of {@link AzureResponseLogger} from real Azure cloud
 * calls.
 */
@Tag("integration")
// Note: temporarily disabled because we have not yet added Azure test environment setup to vault.
@Disabled
public class AzureResponseLoggerTest {

  @Mock private OperationAnnotator mockOperationAnnotator = mock(OperationAnnotator.class);

  private ArgumentCaptor<OperationData> operationDataCaptor =
      ArgumentCaptor.forClass(OperationData.class);

  @Test
  public void testRecordEvent() {
    // Send an Azure request to list resource groups
    List<ResourceGroup> resourceGroups =
        setUpResourceManager().resourceGroups().list().stream().collect(Collectors.toList());

    // Verify AzureResponseLogger was invoked
    verify(mockOperationAnnotator).recordOperation(operationDataCaptor.capture());

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
    assertThat(
        resourceGroups.stream().map(ResourceGroup::name).collect(Collectors.toList()),
        Matchers.hasItem(AzureIntegrationUtils.getResuableResourceGroup()));
  }

  private ResourceManager setUpResourceManager() {
    AzureProfile profile = AzureIntegrationUtils.getUserAzureProfileOrDie();
    return ResourceManager.configure()
        .withLogOptions(
            new HttpLogOptions()
                .setResponseLogger(new AzureResponseLogger(mockOperationAnnotator))
                .setLogLevel(HttpLogDetailLevel.BASIC))
        .authenticate(AzureIntegrationUtils.getAdminAzureCredentialsOrDie(), profile)
        .withSubscription(profile.getSubscriptionId());
  }
}
