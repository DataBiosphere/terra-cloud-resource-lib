package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleProjectUid;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.CloudResourceManagerScopes;
import com.google.api.services.cloudresourcemanager.model.Empty;
import com.google.api.services.cloudresourcemanager.model.Operation;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
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

  /** Create a {@link CloudResourceManagerCow} with some default configurations for convenience. */
  public static CloudResourceManagerCow create(
      ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new CloudResourceManagerCow(
        clientConfig,
        new CloudResourceManager.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(
                    googleCredentials.createScoped(CloudResourceManagerScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  public Projects projects() {
    return new Projects(manager.projects());
  }

  /** See {@link CloudResourceManager.Projects}. */
  public class Projects {
    private final CloudResourceManager.Projects projects;

    private Projects(CloudResourceManager.Projects projects) {
      this.projects = projects;
    }

    /** See {@link CloudResourceManager.Projects#create(Project)}. */
    public Create create(Project project) throws IOException {
      return new Create(projects.create(project), project);
    }

    /** See {@link CloudResourceManager.Projects.Create}. */
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

    /** See {@link CloudResourceManager.Projects.Delete}. */
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
}
