package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.util.CloudResourceException;
import bio.terra.cloudres.util.StatsHelper;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.Credentials;
import com.google.cloud.resourcemanager.*;
import com.google.cloud.resourcemanager.ProjectInfo.ResourceId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;


/** Google Resource Manager Related APIs using Google API Client*/
public class GoogleCloudResourceManager {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManagerClient");

    private static final Tracer tracer = Tracing.getTracer();

    private static final String CLOUD_RESOURCE_MANAGER_PREFIX = "GoogleCloudResourceManager";

    private final ResourceManager resourceManager;
    private final String clientName;

    public GoogleCloudResourceManager(Credentials credentials, String clientName) {
        // Sample code if we want to customize our own retry setting:
        RetrySettings retrySettings = RetrySettings.newBuilder().setMaxAttempts(6).
                setTotalTimeout(Duration.ofMillis(50_000L)).build();
        ResourceManagerOptions options = ResourceManagerOptions.newBuilder().setCredentials(credentials).setRetrySettings(retrySettings).build();

        this.resourceManager = options.getService();
        this.clientName = clientName;
    }

    public GoogleCloudResourceManager(String clientName) {
        // See https://github.com/googleapis/google-cloud-java#authentication:
        // When using google-cloud libraries from within Compute/App Engine, no additional authentication steps are
        // necessary.
        this.resourceManager = ResourceManagerOptions.getDefaultInstance().getService();
        this.clientName = clientName;
    }

    /**
     *  Creates Google Project.
     *
     * @param projectInfo The {@link ProjectInfo} of the project to create
     * @return the project being created.
     */
    public Project createProject(ProjectInfo projectInfo) throws CloudResourceException {
        logger.debug("Creating Google project: projectInfo = " + projectInfo);

        // Record the method usage.
        StatsHelper.recordClientUsageCount(clientName, CLOUD_RESOURCE_MANAGER_PREFIX + "createProject");

        try(Scope ss = tracer.spanBuilder("GoogleCloudResourceManagerClient.createProject").startScopedSpan()) {
            // Record the Cloud API usage.
            // TODO(yonghao): All Gloud API name would Enum value in a central place.
            StatsHelper.recordCloudApiCount(clientName, "GoogleCreateProject");
            tracer.getCurrentSpan().addAnnotation("Starting the work.");
            try {
                return resourceManager.create(resourceManager.create(projectInfo));
            } catch(ResourceManagerException e) {
                logger.error("Failed to create Google project: projectInfo = " + projectInfo);

                // Record the error. For now use the error code.
                StatsHelper.recordCloudError(clientName, CLOUD_RESOURCE_MANAGER_PREFIX + "createProject", String.valueOf(e.getCode()));
                throw new CloudResourceException("Failed to create Google Project", e);
            } finally {
                tracer.getCurrentSpan().addAnnotation("Finished working.");
            }
        }
    }
}
