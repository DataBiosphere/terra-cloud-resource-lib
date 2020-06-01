package bio.terra.cloudres.google.crm;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.CowOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Cloud Object Wrapper(COW) for Google API Client Library: {@link ResourceManager}
 *
 * <p>Eventually there might be multiple COW classes for each resource type, e.g. ProjectCow.
 */
public class ResourceManagerCow {
  private final Logger logger = LoggerFactory.getLogger(ResourceManagerCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final ResourceManagerOptions resourceManagerOptions;
  private final ResourceManager resourceManager;

  public ResourceManagerCow(
      ClientConfig clientConfig, ResourceManagerOptions resourceManagerOptions) {
    this.clientConfig = clientConfig;
    this.resourceManagerOptions = resourceManagerOptions;
    this.operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.resourceManager = resourceManagerOptions.getService();
  }

  /**
   * Creates a Google Project.
   *
   * @param projectInfo The {@link ProjectInfo} of the project to create
   * @return the project being created
   */
  public Project createProject(ProjectInfo projectInfo) {
    return operationAnnotator.executeCowOperation(
        new CowOperation<Project>() {
          @Override
          public CloudOperation getCloudOperation() {
            return CloudOperation.GOOGLE_CREATE_PROJECT;
          }

          @Override
          public Project execute() {
            return resourceManager.create(projectInfo);
          }

          @Override
          public String serializeRequest() {
            return convert(projectInfo);
          }
        });
  }

  /**
   * Converts {@link ProjectInfo} to Json formatted String
   *
   * @param projectInfo: the projectInfo to convert
   * @return the formatted Json in String
   */
  private static String convert(ProjectInfo projectInfo) {
    Gson gson = new Gson();
    return gson.toJson(projectInfo, ProjectInfo.class);
  }
}
