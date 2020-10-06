package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleProjectUid;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.cloudresourcemanager.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.CloudResourceManagerScopes;
import com.google.api.services.cloudresourcemanager.model.*;
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
                Defaults.jsonFactory(), setHttpTimeout(new HttpCredentialsAdapter(
                googleCredentials.createScoped(CloudResourceManagerScopes.all())))
                )
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
        return serializeProjectId(delete.getProjectId());
      }
    }

    /** See {@link CloudResourceManager.Projects#get(String)}. */
    public Get get(String projectId) throws IOException {
      return new Get(projects.get(projectId));
    }

    /** See {@link CloudResourceManager.Projects.Get} */
    public class Get extends AbstractRequestCow<Project> {
      private final CloudResourceManager.Projects.Get get;

      private Get(CloudResourceManager.Projects.Get get) {
        super(CloudOperation.GOOGLE_GET_PROJECT, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectId(get.getProjectId());
      }
    }

    /** See {@link CloudResourceManager.Projects#getIamPolicy(String, GetIamPolicyRequest)}. */
    public GetIamPolicy getIamPolicy(String resource, GetIamPolicyRequest content)
        throws IOException {
      return new GetIamPolicy(projects.getIamPolicy(resource, content));
    }

    /** See {@link CloudResourceManager.Projects.GetIamPolicy}. */
    public class GetIamPolicy extends AbstractRequestCow<Policy> {
      private final CloudResourceManager.Projects.GetIamPolicy getIamPolicy;

      private GetIamPolicy(CloudResourceManager.Projects.GetIamPolicy getIamPolicy) {
        super(
            CloudOperation.GOOGLE_GET_IAM_POLICY_PROJECT,
            clientConfig,
            operationAnnotator,
            getIamPolicy);
        this.getIamPolicy = getIamPolicy;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectId(getIamPolicy.getResource());
      }
    }

    /** See {@link CloudResourceManager.Projects#setIamPolicy(String, SetIamPolicyRequest)} )}. */
    public SetIamPolicy setIamPolicy(String resource, SetIamPolicyRequest content)
        throws IOException {
      return new SetIamPolicy(projects.setIamPolicy(resource, content));
    }

    /** See {@link CloudResourceManager.Projects.SetIamPolicy}. */
    public class SetIamPolicy extends AbstractRequestCow<Policy> {
      private final CloudResourceManager.Projects.SetIamPolicy setIamPolicy;

      private SetIamPolicy(CloudResourceManager.Projects.SetIamPolicy setIamPolicy) {
        super(
            CloudOperation.GOOGLE_SET_IAM_POLICY_PROJECT,
            clientConfig,
            operationAnnotator,
            setIamPolicy);
        this.setIamPolicy = setIamPolicy;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectId(setIamPolicy.getResource());
      }
    }

    private JsonObject serializeProjectId(String projectId) {
      JsonObject result = new JsonObject();
      result.addProperty("project_id", projectId);
      return result;
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
      return new Get(operations.get(name));
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
          operation, ResourceManagerOperationAdapter::new, op -> get(op.getName()));
    }
  }

  private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer) {
    return new HttpRequestInitializer() {
      @Override
      public void initialize(HttpRequest httpRequest) throws IOException {
        requestInitializer.initialize(httpRequest);
        httpRequest.setConnectTimeout(3 * 60000);  // 3 minutes connect timeout
        httpRequest.setReadTimeout(3 * 60000);  // 3 minutes read timeout
      }
    };
  }
}
