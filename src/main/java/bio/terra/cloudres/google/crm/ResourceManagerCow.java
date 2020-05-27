package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.util.JsonConverter;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Wrapper for Google API Client Library: {@link ResourceManager} */
public class ResourceManagerCow {
  private final Logger logger = LoggerFactory.getLogger(ResourceManagerCow.class);

  private final ClientConfig options;
  private final OperationAnnotator helper;
  private final ResourceManagerOptions resourceManagerOptions;
  private final ResourceManager resourceManager;

  public ResourceManagerCow(ClientConfig options, ResourceManagerOptions resourceManagerOptions) {
    this.options = options;
    this.resourceManagerOptions = resourceManagerOptions;
    this.helper = new OperationAnnotator(options);
    this.resourceManager = resourceManagerOptions.getService();
  }

  /**
   * Creates a Google Project.
   *
   * @param projectInfo The {@link ProjectInfo} of the project to create
   * @return the project being created.
   */
  public Project createProject(ProjectInfo projectInfo) throws Exception {
    // TODO(yonghao): Add identity in logs.
    logger.info("Creating Google project: projectInfo = " + JsonConverter.convert(projectInfo));
    Project project =
        helper.executeGoogleCall(
            () -> resourceManager.create(projectInfo), CloudOperation.GOOGLE_CREATE_PROJECT);
    logger.info("Created Google Project: " + JsonConverter.convert(project));
    return project;
  }
}