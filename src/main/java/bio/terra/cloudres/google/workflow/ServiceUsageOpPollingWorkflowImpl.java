package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.google.serviceusage.GoogleServiceUsageActivities;
import com.google.api.services.serviceusage.v1beta1.model.Operation;
import com.uber.cadence.workflow.Workflow;
import java.time.Duration;

public class ServiceUsageOpPollingWorkflowImpl implements ServiceUsageOpPollingWorkflow {
  private final GoogleServiceUsageActivities cloudServiceUsageActivities =
      Workflow.newActivityStub(
          GoogleServiceUsageActivities.class, WorkflowSupport.standardRetryOptions());

  private final ServiceUsageOpPollingWorkflow continueAsNew =
      Workflow.newContinueAsNewStub(ServiceUsageOpPollingWorkflow.class);

  @Override
  public void pollUntilDone(String operationName) {
    Operation operation = cloudServiceUsageActivities.getOperation(operationName);
    if (operation.getDone() == null || !operation.getDone()) {
      Workflow.sleep(Duration.ofSeconds(30));
      continueAsNew.pollUntilDone(operationName);
    }
    if (operation.getError() != null) {
      throw new RuntimeException("operation failed, details: " + operation.toString());
    }
  }
}
