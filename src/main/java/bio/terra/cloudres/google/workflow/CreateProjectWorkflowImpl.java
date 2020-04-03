package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.google.billing.GoogleCloudBillingActivities;
import bio.terra.cloudres.google.cloudresourcemanager.GoogleCloudResourceManagerActivities;
import bio.terra.cloudres.google.serviceusage.GoogleServiceUsageActivities;
import bio.terra.cloudres.google.storage.GoogleCloudStorageActivities;
import com.uber.cadence.workflow.Saga;
import com.uber.cadence.workflow.Workflow;

import java.util.Arrays;

public class CreateProjectWorkflowImpl implements CreateProjectWorkflow {
    private final GoogleCloudResourceManagerActivities cloudResActivities =
            Workflow.newActivityStub(GoogleCloudResourceManagerActivities.class, WorkflowSupport.standardRetryOptions());

    private final GoogleCloudBillingActivities cloudBillingActivities =
            Workflow.newActivityStub(GoogleCloudBillingActivities.class, WorkflowSupport.standardRetryOptions());

    private final GoogleServiceUsageActivities cloudServiceUsageActivities =
            Workflow.newActivityStub(GoogleServiceUsageActivities.class, WorkflowSupport.standardRetryOptions());

    private final GoogleCloudStorageActivities cloudStorageActivities =
            Workflow.newActivityStub(GoogleCloudStorageActivities.class, WorkflowSupport.standardRetryOptions());

    @Override
    public void createProject(CreateProjectArguments arguments) {
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());
        try {
            String createOperationName = this.cloudResActivities.createProject(arguments.projectId, arguments.parent);
            saga.addCompensation(this.cloudResActivities::deleteProject, arguments.projectId);
            CloudResourceOpPollingWorkflow projectPoller = Workflow.newChildWorkflowStub(CloudResourceOpPollingWorkflow.class);
            projectPoller.pollUntilDone(createOperationName);

            this.cloudResActivities.addPolicyBindings(arguments.projectId, arguments.policies);

            cloudBillingActivities.setBilling(arguments.projectId, arguments.billingAccount);

            String enableServicesOperationName = cloudServiceUsageActivities.batchEnable(arguments.projectId, Arrays.asList("bigquery-json.googleapis.com","compute.googleapis.com","clouderrorreporting.googleapis.com","cloudkms.googleapis.com","cloudtrace.googleapis.com","containerregistry.googleapis.com","dataflow.googleapis.com","dataproc.googleapis.com","genomics.googleapis.com","logging.googleapis.com","monitoring.googleapis.com","storage-api.googleapis.com","storage-component.googleapis.com"));
            ServiceUsageOpPollingWorkflow servicePoller = Workflow.newChildWorkflowStub(ServiceUsageOpPollingWorkflow.class);
            servicePoller.pollUntilDone(enableServicesOperationName);

            cloudStorageActivities.createBucket("storage-logs-" + arguments.projectId, arguments.projectId);

        } catch (RuntimeException e) {
            saga.compensate();
            throw e;
        }

    }
}
