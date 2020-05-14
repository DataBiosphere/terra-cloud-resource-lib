package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.google.common.GoogleResourceClientHelper;
import bio.terra.cloudres.google.common.GoogleResourceClientOptions;
import bio.terra.cloudres.util.CloudApiMethod;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Wrapper for Google API Client Library: {@link ResourceManager}
 */
public class GoogleCloudResourceManager {
    private final Logger logger =
            LoggerFactory.getLogger(GoogleCloudResourceManager.class);

    private final GoogleResourceClientOptions options;
    private final GoogleResourceClientHelper helper;
    private final ResourceManagerOptions resourceManagerOptions;
    private final ResourceManager resourceManager;

    public GoogleCloudResourceManager(GoogleResourceClientOptions options) {
        this(options, ResourceManagerOptions.newBuilder().setCredentials(options.getCredential()).setRetrySettings(options.getRetrySettings()).build());
    }

    @VisibleForTesting
    GoogleCloudResourceManager(GoogleResourceClientOptions options, ResourceManagerOptions resourceManagerOptions) {
        this.options = options;
        this.resourceManagerOptions = resourceManagerOptions;
        this.helper = new GoogleResourceClientHelper(options);
        this.resourceManager = resourceManagerOptions.getService();
    }

    /**
     * Creates a Google Project.
     *
     * @param projectInfo The {@link ProjectInfo} of the project to create
     * @return the project being created.
     */
    public Project createProject(ProjectInfo projectInfo) throws Exception {
        logger.debug("Creating Google project: projectInfo = " + projectInfo);
        return helper.executeGoogleCloudCall(() -> resourceManager.create(projectInfo), CloudApiMethod.GOOGLE_CREATE_PROJECT);
    }
}
