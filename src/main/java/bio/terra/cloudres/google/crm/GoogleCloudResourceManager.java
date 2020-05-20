package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.google.common.GoogleResourceClientHelper;
import bio.terra.cloudres.google.common.GoogleClientConfig;
import bio.terra.cloudres.util.CloudOperation;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static bio.terra.cloudres.util.JsonConverter.convertGoogleProjectInfoToJson;
import static bio.terra.cloudres.util.JsonConverter.convertGoogleProjectToJson;

/**
 * A Wrapper for Google API Client Library: {@link ResourceManager}
 */
public class GoogleCloudResourceManager {
    private final Logger logger =
            LoggerFactory.getLogger(GoogleCloudResourceManager.class);

    private final GoogleClientConfig options;
    private final GoogleResourceClientHelper helper;
    private final ResourceManagerOptions resourceManagerOptions;
    private final ResourceManager resourceManager;

    public GoogleCloudResourceManager(GoogleClientConfig options, ResourceManagerOptions resourceManagerOptions) {
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
        logger.debug("Creating Google project: projectInfo = " + convertGoogleProjectInfoToJson(projectInfo));
        Project project = helper.executeGoogleCloudCall(() -> resourceManager.create(projectInfo), CloudOperation.GOOGLE_CREATE_PROJECT);
        logger.debug("Created Google Project: " + convertGoogleProjectToJson(project));
        return project;
    }
}
