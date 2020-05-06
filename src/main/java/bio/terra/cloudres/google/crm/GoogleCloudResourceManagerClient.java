package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.util.CloudResourceException;
import com.google.api.gax.retrying.RetrySettings;
import com.google.auth.Credentials;
import com.google.cloud.resourcemanager.*;
import com.google.cloud.resourcemanager.ProjectInfo.ResourceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Duration;

import java.io.IOException;

/** Google Resource Manager Related APIs using Google API Client*/
public class GoogleCloudResourceManagerClient {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManagerClient");

    private ResourceManager resourceManager;

    public GoogleCloudResourceManagerClient(Credentials credentials) {
        // Sample code if we want to customize our own retry setting:
        RetrySettings retrySettings = RetrySettings.newBuilder().setMaxAttempts(6).
                setTotalTimeout(Duration.ofMillis(50_000L)).build();
        ResourceManagerOptions options = ResourceManagerOptions.newBuilder().setCredentials(credentials).setRetrySettings(retrySettings).build();

        resourceManager = options.getService();
    }

    public GoogleCloudResourceManagerClient() {
        // See https://github.com/googleapis/google-cloud-java#authentication:
        // When using google-cloud libraries from within Compute/App Engine, no additional authentication steps are
        // necessary.
        resourceManager = ResourceManagerOptions.getDefaultInstance().getService();
    }

    /**
     *  Creates Google Project.
     *
     * @param projectId The project id to be create
     * @param parentResourceId The resource id to create
     * @return the project being created.
     */
    public Project createProjectRaw(String projectId, ResourceId parentResourceId) throws CloudResourceException {
        logger.debug("Creating Google project: projectId = {}, resourceId = {} " + projectId, parentResourceId);
        try {
            return resourceManager.create(resourceManager.create(ProjectInfo.newBuilder(projectId).setParent(parentResourceId).build()));
        } catch(ResourceManagerException e) {
            logger.error("Failed to create Google project: projectId = {}, resourceId = {} " + projectId, parentResourceId);
            throw new CloudResourceException("Failed to create Google Project", e);
        }

    }
}
