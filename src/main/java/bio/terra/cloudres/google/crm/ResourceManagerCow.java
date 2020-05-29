package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;

/**
 * A Cloud Object Wrapper(COW) for Google API Client Library: {@link ResourceManager}
 *
 * <p>Eventually there might be multiple COW classes for each resource type, e.g. ProjectCow.
 */
public class ResourceManagerCow {
  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final ResourceManagerOptions resourceManagerOptions;
  private final ResourceManager resourceManager;

  public ResourceManagerCow(
      ClientConfig clientConfig, ResourceManagerOptions resourceManagerOptions) {
    this.clientConfig = clientConfig;
    this.resourceManagerOptions = resourceManagerOptions;
    this.operationAnnotator = new OperationAnnotator(clientConfig);
    this.resourceManager = resourceManagerOptions.getService();
  }

  /**
   * Creates a Google Project.
   *
   * @param projectInfo The {@link ProjectInfo} of the project to create
   * @return the project being created.
   */
  public Project createProject(ProjectInfo projectInfo) {
    return operationAnnotator.executeGoogleCall(
        () -> resourceManager.create(projectInfo),
        CloudOperation.GOOGLE_CREATE_PROJECT,
        projectInfo);
  }
}
