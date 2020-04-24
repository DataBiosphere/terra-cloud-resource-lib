package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.DoNotRetrySupport;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.ResourceId;
import com.uber.cadence.workflow.Workflow;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class GoogleCloudResourceManagerActivitiesImpl
    implements GoogleCloudResourceManagerActivities {
  private final GoogleCloudResourceManager googleCloudResourceManager;

  public GoogleCloudResourceManagerActivitiesImpl(
      GoogleCloudResourceManager googleCloudResourceManager) {
    this.googleCloudResourceManager = googleCloudResourceManager;
  }

  @Override
  public String createProject(String projectId, ResourceId parent) {
    return DoNotRetrySupport.wrapDoNotRetry(
        () -> {
          try {
            return googleCloudResourceManager.createProjectRaw(projectId, parent).getName();
          } catch (IOException ioe) {
            throw Workflow.wrap(ioe);
          }
        });
  }

  @Override
  public void deleteProject(String projectId) {
    try {
      googleCloudResourceManager.deleteProjectRaw(projectId);
    } catch (IOException e) {
      throw Workflow.wrap(e);
    }
  }

  @Override
  public Operation getOperation(String operationName) {
    try {
      return googleCloudResourceManager.checkOperationRaw(operationName);
    } catch (IOException ioe) {
      throw Workflow.wrap(ioe);
    }
  }

  @Override
  public boolean addPolicyBindings(String projectId, Map<String, Set<String>> policiesToAdd) {
    try {
      return googleCloudResourceManager.addPolicyBindingsRaw(projectId, policiesToAdd);
    } catch (IOException ioe) {
      throw Workflow.wrap(ioe);
    }
  }
}
