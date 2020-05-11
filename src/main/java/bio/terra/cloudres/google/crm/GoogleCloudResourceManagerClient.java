package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.google.common.GoogleResourceClientHelper;
import bio.terra.cloudres.google.common.GoogleResourceClientOptions;
import bio.terra.cloudres.util.CloudApiMethod;
import com.google.cloud.resourcemanager.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleCloudResourceManagerClient {
    private final Logger logger =
            LoggerFactory.getLogger("bio.terra.cloudres.google.crm.GoogleCloudResourceManager");

    private final GoogleResourceClientOptions options;
    private final GoogleResourceClientHelper helper;
    private final ResourceManagerOptions resourceManagerOptions;
    private final ResourceManager resourceManager;

    public GoogleCloudResourceManagerClient(GoogleResourceClientOptions options) {
        this.options = options;
        this.resourceManagerOptions =  ResourceManagerOptions.newBuilder().setCredentials(options.getCredential()).setRetrySettings(options.getRetrySettings()).build();
        this.helper = new GoogleResourceClientHelper(options);
        this.resourceManager = resourceManagerOptions.getService();
    }

    /**
     *  Creates Google Project.
     *
     * @param projectInfo The {@link ProjectInfo} of the project to create
     * @return the project being created.
     */
    public Project createProject(ProjectInfo projectInfo) throws Exception {
        logger.debug("Creating Google project: projectInfo = " + projectInfo);
        return helper.executeGoogleCloudCall(() -> resourceManager.create(projectInfo), CloudApiMethod.GOOGLE_CREATE_PROJECT);
    }
}
