package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.google.storage.GoogleCloudStorageActivities;
import com.uber.cadence.workflow.Workflow;
import java.time.Duration;

public class DeleteBucketWorkflowImpl implements DeleteBucketWorkflow {
  private final GoogleCloudStorageActivities activities =
      Workflow.newActivityStub(
          GoogleCloudStorageActivities.class, WorkflowSupport.standardRetryOptions());

  @Override
  public void deleteBucket(String bucketName) {
    activities.setDeleteLifecycle(bucketName, 0);

    boolean successful = false;
    while (!successful) {
      try {
        activities.deleteBucket(bucketName);
        successful = true;
      } catch (Throwable e) {
        Workflow.sleep(Duration.ofHours(4));
      }
    }
  }
}
