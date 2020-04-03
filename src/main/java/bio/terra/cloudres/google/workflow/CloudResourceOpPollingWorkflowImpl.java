package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.google.cloudresourcemanager.GoogleCloudResourceManagerActivities;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.uber.cadence.workflow.Workflow;

import java.time.Duration;

public class CloudResourceOpPollingWorkflowImpl implements CloudResourceOpPollingWorkflow {
    private final GoogleCloudResourceManagerActivities cloudResActivities =
            Workflow.newActivityStub(GoogleCloudResourceManagerActivities.class, WorkflowSupport.standardRetryOptions());

    private final CloudResourceOpPollingWorkflow continueAsNew =
            Workflow.newContinueAsNewStub(CloudResourceOpPollingWorkflow.class);

    @Override
    public void pollUntilDone(String operationName) {
        Operation operation = cloudResActivities.getOperation(operationName);
        if (operation.getDone() == null || !operation.getDone()) {
            Workflow.sleep(Duration.ofSeconds(10));
            // continue as a new workflow to keep the workflow history from building up
            continueAsNew.pollUntilDone(operationName);
        }
        if (operation.getError() != null) {
            throw new RuntimeException("operation failed, details: " + operation.toString());
        }
    }
}
