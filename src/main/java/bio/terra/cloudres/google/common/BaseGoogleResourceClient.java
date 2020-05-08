package bio.terra.cloudres.google.common;

import bio.terra.cloudres.util.CloudApiMethod;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.Service;
import com.google.cloud.ServiceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public abstract class BaseGoogleResourceClient<SO extends ServiceOptions<S, SO>, S extends Service<SO>>{
    protected final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManagerClient");

    protected static final Tracer tracer = Tracing.getTracer();

    protected final SO googleServiceOptions;
    protected final S googleService;
    protected final GoogleResourceClientOptions options;

    public BaseGoogleResourceClient(GoogleResourceClientOptions options) {
        this.options = options;
        this.googleServiceOptions = initializeServiceOptions(options);
        this.googleService = googleServiceOptions.getService();
    }

    /**
     * Creates a {@link ServiceOptions}. The base ServiceOptions constructor is not public, so each subclass needs to
     * implement this methods.
     * */
    protected abstract SO initializeServiceOptions(GoogleResourceClientOptions options);

    protected void recordCloudApiCount(CloudApiMethod cloudApiName) {
         // TODO(yonghao): All Gloud API name would Enum value in a central place.
         MetricsHelper.recordCloudApiCount(options.getClient(), cloudApiName);
     }

    protected void recordCloudErrors(String errorCode, CloudApiMethod cloudApiName) {
        MetricsHelper.recordCloudError(options.getClient(), cloudApiName，errorCode);
    }

    protected void recordCloudApiLatency(Duration duration, CloudApiMethod cloudApiName) {
        MetricsHelper.recordCloudApiLatency(options.getClient(), cloudApiName, duration);
    }

    protected void addTracerAnnotation(String annotation) {
        tracer.getCurrentSpan().addAnnotation(annotation);
    }
}
