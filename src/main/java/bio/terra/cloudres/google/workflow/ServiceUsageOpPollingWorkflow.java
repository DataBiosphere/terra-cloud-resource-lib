package bio.terra.cloudres.google.workflow;

import com.uber.cadence.workflow.WorkflowMethod;

public interface ServiceUsageOpPollingWorkflow {
  @WorkflowMethod
  void pollUntilDone(String operationName);
}
