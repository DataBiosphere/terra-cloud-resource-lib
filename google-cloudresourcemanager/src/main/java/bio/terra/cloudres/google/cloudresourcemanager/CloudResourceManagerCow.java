package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.cloudresourcemanager.operation.OperationCow;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleProjectUid;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.model.Empty;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link CloudResourceManager} */
public class CloudResourceManagerCow {
  private final Logger logger = LoggerFactory.getLogger(CloudResourceManagerCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final CloudResourceManager manager;

  public CloudResourceManagerCow(
      ClientConfig clientConfig, CloudResourceManager.Builder managerBuilder) {
    this.clientConfig = clientConfig;
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

    /** See {@link CloudResourceManager.Projects#create(Project)}. */
    public Create create(Project project) throws IOException {
      return new Create(projects.create(project), project);
    }

    public class Create extends AbstractRequestCow<Operation> {
      private final Project project;

      public Create(CloudResourceManager.Projects.Create create, Project project) {
        super(CloudOperation.GOOGLE_CREATE_PROJECT, clientConfig, operationAnnotator, create);
        this.project = project;
      }

      @Override
      protected Optional<CloudResourceUid> resourceUidCreation() {
        return Optional.of(
            new CloudResourceUid()
                .googleProjectUid(new GoogleProjectUid().projectId(project.getProjectId())));
      }

      @Override
      protected JsonObject serialize() {
        return new Gson().toJsonTree(project).getAsJsonObject();
      }
    }

    /** See {@link CloudResourceManager.Projects#delete(String)}. */
    public Delete delete(String projectId) throws IOException {
      return new Delete(projects.delete(projectId));
    }

    public class Delete extends AbstractRequestCow<Empty> {
      private final CloudResourceManager.Projects.Delete delete;

      private Delete(CloudResourceManager.Projects.Delete delete) {
        super(CloudOperation.GOOGLE_DELETE_PROJECT, clientConfig, operationAnnotator, delete);
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

  public Operations operations() {
    return new Operations(manager.operations());
  }

  public class Operations {
    private final CloudResourceManager.Operations operations;

    private Operations(CloudResourceManager.Operations operations) {
      this.operations = operations;
    }

    /** See {@link CloudResourceManager.Operations#get(String)} */
    public Get get(String name) throws IOException {
      return new Get(manager.operations().get(name));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final CloudResourceManager.Operations.Get get;

      public Get(CloudResourceManager.Operations.Get get) {
        super(
            CloudOperation.GOOGLE_RESOURCE_MANAGER_OPERATION_GET,
            clientConfig,
            operationAnnotator,
            get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("operation_name", get.getName());
        return result;
      }
    }

    public OperationCow<Operation> operationCow(Operation operation) {
      return new OperationCow<>(
          operation, ResourceManagerOperationAdapter.FACTORY, op -> get(op.getName()));
    }
  }
}
