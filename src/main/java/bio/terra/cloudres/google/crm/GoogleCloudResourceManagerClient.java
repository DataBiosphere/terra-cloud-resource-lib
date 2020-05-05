package bio.terra.cloudres.google.crm;

import com.google.auth.Credentials;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ProjectInfo.ResourceId;
import com.google.cloud.resourcemanager.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/** Google Resource Manager Related APIs using Google API Client*/
public class GoogleCloudResourceManagerClient {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManager");

    private ResourceManager resourceManager;

    public GoogleCloudResourceManagerClient(Credentials credentials) {
        // See https://github.com/googleapis/google-cloud-java#authentication:
        // When using google-cloud libraries from within Compute/App Engine, no additional authentication steps are
        // necessary.
        // resourceManager = ResourceManagerOptions.getDefaultInstance().getService();

        // hen using google-cloud libraries elsewhere:
        resourceManager = ResourceManagerOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    /**
     *  Creates Google Project.
     *
     * @param projectId The project id to be create
     * @param parentResourceId The resource id to create
     */
    public Project createProjectRaw(String projectId, ResourceId parentResourceId) throws IOException {
        logger.debug("Creating Google project: projectId = {}, resourceId = {} " + projectId, parentResourceId);
        return resourceManager.create(resourceManager.create(ProjectInfo.newBuilder(projectId).setParent(parentResourceId).build()));
    }
}
