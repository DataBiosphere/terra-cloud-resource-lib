package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.google.storage.GoogleCloudStorage;
import bio.terra.cloudres.google.storage.GoogleCloudStorageActivitiesImpl;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.worker.Worker;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;

public class DeleteBucketMain {
  public static final String DOMAIN = "sample";
  static final String TASK_LIST = "DeleteWorkflow";

  static final String saKeyFile = "/Users/dvoet/projects/rawls/config/rawls-account.json";
  static final String bucketName = "aaaaa-test-iam-dvoet";

  public static void main(String[] args) throws IOException {
    // Start a worker that hosts both workflow and activity implementations.
    Worker.Factory factory = new Worker.Factory(DOMAIN);
    Worker worker = factory.newWorker(TASK_LIST);
    // Workflows are stateful. So you need a type to create instances.
    worker.registerWorkflowImplementationTypes(DeleteBucketWorkflowImpl.class);
    // Activities are stateless and thread safe. So a shared instance is used.
    worker.registerActivitiesImplementations(
        new GoogleCloudStorageActivitiesImpl(
            new GoogleCloudStorage(
                ServiceAccountCredentials.fromStream(new FileInputStream(saKeyFile)))));
    // Start listening to the workflow and activity task lists.
    factory.start();

    // Start a workflow execution. Usually this is done from another program.
    WorkflowClient workflowClient = WorkflowClient.newInstance(DOMAIN);
    // Get a workflow stub using the same task list the worker uses.
    WorkflowOptions workflowOptions =
        new WorkflowOptions.Builder()
            .setTaskList(TASK_LIST)
            .setExecutionStartToCloseTimeout(Duration.ofDays(3))
            .build();
    DeleteBucketWorkflow workflow =
        workflowClient.newWorkflowStub(DeleteBucketWorkflow.class, workflowOptions);
    // Execute a workflow waiting for it to complete.
    workflow.deleteBucket(bucketName);
    System.exit(0);
  }
}
