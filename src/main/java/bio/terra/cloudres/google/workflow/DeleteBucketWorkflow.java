package bio.terra.cloudres.google.workflow;

import com.uber.cadence.workflow.WorkflowMethod;

public interface DeleteBucketWorkflow {
  @WorkflowMethod
  void deleteBucket(String bucketName);
}
