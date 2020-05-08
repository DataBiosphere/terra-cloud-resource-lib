package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.google.common.BaseGoogleResourceClient;
import bio.terra.cloudres.google.common.GoogleResourceClientOptions;
import bio.terra.cloudres.util.CloudApiMethod;
import bio.terra.cloudres.util.CloudResourceException;
import bio.terra.cloudres.util.MetricsHelper;
import com.google.cloud.ServiceOptions;
import com.google.cloud.resourcemanager.*;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCloudResourceManagerClient extends BaseGoogleResourceClient<ResourceManagerOptions, ResourceManager> {
    private static final String CLOUD_RESOURCE_MANAGER_PREFIX = "GoogleCloudResourceManager";

    public GoogleCloudResourceManagerClient(GoogleResourceClientOptions options) {
        super(options);
    }

    /**
     *  Creates Google Project.
     *
     * @param projectInfo The {@link ProjectInfo} of the project to create
     * @return the project being created.
     */
    public Project createProject(ProjectInfo projectInfo) throws CloudResourceException {
        logger.debug("Creating Google project: projectInfo = " + projectInfo);

        try(Scope ss = tracer.spanBuilder("GoogleCloudResourceManagerClient.createProject").startScopedSpan()) {
            // Record the Cloud API usage.
            recordCloudApiCount(CloudApiMethod.GOOGLE_CREATE_PROJECT);
            tracer.getCurrentSpan().addAnnotation("Starting the work.");
            try {
                return googleService.create(googleService.create(projectInfo));
            } catch(ResourceManagerException e) {
                logger.error("Failed to create Google project: projectInfo = " + projectInfo);
                recordCloudErrors(String.valueOf(e.getCode()), CloudApiMethod.GOOGLE_CREATE_PROJECT,);
                throw new CloudResourceException("Failed to create Google Project", e);
            } finally {
                tracer.getCurrentSpan().addAnnotation("Finished working.");
            }
        }
    }


    @Override
    protected ResourceManagerOptions initializeServiceOptions(GoogleResourceClientOptions options) {
        return ResourceManagerOptions.newBuilder().setCredentials(options.getCredential()).setRetrySettings(options.getRetrySettings()).build();;
    }
}
