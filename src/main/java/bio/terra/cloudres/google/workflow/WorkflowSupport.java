package bio.terra.cloudres.google.workflow;

import bio.terra.cloudres.DoNotRetrySupport;
import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.common.RetryOptions;

import java.time.Duration;

public class WorkflowSupport {
    public static ActivityOptions standardRetryOptions() {
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
}
