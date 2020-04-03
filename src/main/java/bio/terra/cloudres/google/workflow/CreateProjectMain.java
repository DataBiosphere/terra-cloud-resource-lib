package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.google.billing.GoogleCloudBilling;
import bio.terra.cloudres.google.billing.GoogleCloudBillingActivitiesImpl;
import bio.terra.cloudres.google.cloudresourcemanager.GoogleCloudResourceManager;
import bio.terra.cloudres.google.cloudresourcemanager.GoogleCloudResourceManagerActivitiesImpl;
import bio.terra.cloudres.google.serviceusage.GoogleServiceUsage;
import bio.terra.cloudres.google.serviceusage.GoogleServiceUsageActivitiesImpl;
import bio.terra.cloudres.google.storage.GoogleCloudStorage;
import bio.terra.cloudres.google.storage.GoogleCloudStorageActivitiesImpl;
import com.google.api.services.cloudresourcemanager.model.ResourceId;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.worker.Worker;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.*;

public class CreateProjectMain {
    public static final String DOMAIN = "sample";
    static final String TASK_LIST = "CreateProject";

    static final String saKeyFile = "/Users/dvoet/projects/rawls/config/rawls-account.json";

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // Start a worker that hosts both workflow and activity implementations.
        Worker.Factory factory = new Worker.Factory(DOMAIN);
        Worker worker = factory.newWorker(TASK_LIST);

        // Workflows are stateful. So you need a type to create instances.
        //TODO need to remember to add these
        worker.registerWorkflowImplementationTypes(
                CreateProjectWorkflowImpl.class,
                CloudResourceOpPollingWorkflowImpl.class,
                ServiceUsageOpPollingWorkflowImpl.class);

        // Activities are stateless and thread safe. So a shared instance is used.
        GoogleCredentials credentials = ServiceAccountCredentials.fromStream(new FileInputStream(saKeyFile)).createScoped("https://www.googleapis.com/auth/cloud-platform");

        //TODO need to remember to add these
        worker.registerActivitiesImplementations(
                new GoogleCloudResourceManagerActivitiesImpl(new GoogleCloudResourceManager(credentials)),
                new GoogleCloudBillingActivitiesImpl(new GoogleCloudBilling((credentials))),
                new GoogleServiceUsageActivitiesImpl(new GoogleServiceUsage(credentials)),
                new GoogleCloudStorageActivitiesImpl(new GoogleCloudStorage(credentials)));

        // Start listening to the workflow and activity task lists.
        factory.start();

        // Start a workflow execution. Usually this is done from another program.
        WorkflowClient workflowClient = WorkflowClient.newInstance(DOMAIN);
        // Get a workflow stub using the same task list the worker uses.
        WorkflowOptions workflowOptions =
                new WorkflowOptions.Builder()
                        .setTaskList(TASK_LIST)
                        .setExecutionStartToCloseTimeout(Duration.ofMinutes(20))
                        .build();
        CreateProjectWorkflow workflow =
                workflowClient.newWorkflowStub(CreateProjectWorkflow.class, workflowOptions);
        // Execute a workflow waiting for it to complete.
        Map<String, Set<String>> policies = Collections.singletonMap("roles/storage.admin", Collections.singleton("user:dvoettest@gmail.com"));
        workflow.createProject(new CreateProjectArguments(
                "dvoet-test-cadence-9",
                new ResourceId().setType("folder").setId("848728541543"),
                "billingAccounts/00708C-45D19D-27AAFA",
                policies));
        System.exit(0);
    }
}
