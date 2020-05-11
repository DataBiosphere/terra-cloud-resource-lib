package bio.terra.cloudres.google.common;

import bio.terra.cloudres.util.CloudApiMethod;
import bio.terra.cloudres.util.CloudResourceException;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.http.BaseHttpServiceException;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;

/** Helper class to use Google Resource clients. */
public class GoogleResourceClientHelper {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.common.GoogleResourceClientHelper");

    private static final Tracer tracer = Tracing.getTracer();

    private final GoogleResourceClientOptions options;

    public GoogleResourceClientHelper(GoogleResourceClientOptions options) {
        this.options = options;
    }

    public <R> R executeGoogleCloudCall(Callable<R> googleCall, CloudApiMethod cloudApiMethod) throws Exception {
        try(Scope ss = tracer.spanBuilder(cloudApiMethod.name()).startScopedSpan()) {
            // Record the Cloud API usage.
            recordCloudApiCount(cloudApiMethod);
            addTracerAnnotation("Starting Google Call.");
            try {
                return googleCall.call();
            } catch(BaseHttpServiceException e) {
                logger.error("Failed to execute Google Call: " + googleCall.toString());
                recordCloudErrors(String.valueOf(e.getCode()), cloudApiMethod);
                throw new CloudResourceException("Failed on " + cloudApiMethod.name(), e);
            } finally {
                addTracerAnnotation("Finishing Google Call.");
            }
        }
    }

    private void recordCloudApiCount(CloudApiMethod cloudApiName) {
        // TODO(yonghao): All Gloud API name would Enum value in a central place.
        MetricsHelper.recordCloudApiCount(options.getClient(), cloudApiName);
    }

    private void recordCloudErrors(String errorCode, CloudApiMethod cloudApiName) {
        MetricsHelper.recordCloudError(options.getClient(), cloudApiName，errorCode);
    }

    private void recordCloudApiLatency(Duration duration, CloudApiMethod cloudApiName) {
        MetricsHelper.recordCloudApiLatency(options.getClient(), cloudApiName, duration);
    }

    private void addTracerAnnotation(String annotation) {
        tracer.getCurrentSpan().addAnnotation(annotation);
    }
}
