package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Empty;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link CloudResourceManager} */
public class CloudResourceManagerCow {
  private final Logger logger = LoggerFactory.getLogger(CloudResourceManagerCow.class);

  private final OperationAnnotator operationAnnotator;
  private final CloudResourceManager manager;

  public CloudResourceManagerCow(
      ClientConfig clientConfig, CloudResourceManager.Builder managerBuilder) {
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    manager = managerBuilder.build();
  }

  public Projects projects() {
    return new Projects(manager.projects());
  }

  public class Projects {
    private final CloudResourceManager.Projects projects;

    private Projects(CloudResourceManager.Projects projects) {
      this.projects = projects;
    }

    public Create create(Project project) throws IOException {
      return new Create(projects.create(project), project);
    }

    public class Create {
      private final CloudResourceManager.Projects.Create create;
      private final Project project;

      public Create(CloudResourceManager.Projects.Create create, Project project) {
        this.create = create;
        this.project = project;
      }

      public Operation execute() throws IOException {
        return operationAnnotator.executeCheckedCowOperation(
            CloudOperation.GOOGLE_CREATE_PROJECT,
            create::execute,
            () -> new Gson().toJsonTree(project).getAsJsonObject());
      }
    }

    public Delete delete(String projectId) throws IOException {
      return new Delete(projects.delete(projectId));
    }

    // TODO use AbstractRequestCow for both Create & DELETE or neither.
    public class Delete extends AbstractRequestCow<Empty> {
      private final CloudResourceManager.Projects.Delete delete;

      public Delete(CloudResourceManager.Projects.Delete delete) {
        super(CloudOperation.GOOGLE_DELETE_PROJECT, delete, operationAnnotator);
        this.delete = delete;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", delete.getProjectId());
        return result;
      }
    }
  }
}
