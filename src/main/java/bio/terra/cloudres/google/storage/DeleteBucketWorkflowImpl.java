package bio.terra.cloudres.google.storage;

import bio.terra.cloudres.DoNotRetrySupport;
import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.common.RetryOptions;
import com.uber.cadence.workflow.Workflow;

import java.time.Duration;

public class DeleteBucketWorkflowImpl implements DeleteBucketWorkflow {
    private final GoogleCloudStorageActivities activities =
            Workflow.newActivityStub(GoogleCloudStorageActivities.class, standardRetryOptions());

    private ActivityOptions standardRetryOptions() {
        return new ActivityOptions.Builder()
                .setScheduleToCloseTimeout(Duration.ofSeconds(10))
                .setRetryOptions(
                        new RetryOptions.Builder()
                                .setInitialInterval(Duration.ofSeconds(1))
                                .setExpiration(Duration.ofMinutes(1))
                                .setDoNotRetry(DoNotRetrySupport.DoNotRetryException.class)
                                .build())
                .build();
    }

    @Override
    public void deleteBucket(String bucketName) {
        activities.setDeleteLifecycleRaw(bucketName, 0);

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
