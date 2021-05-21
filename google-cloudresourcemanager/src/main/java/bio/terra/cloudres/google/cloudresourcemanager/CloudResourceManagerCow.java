package bio.terra.cloudres.google.cloudresourcemanager;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleProjectUid;
import bio.terra.janitor.model.ResourceMetadata;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManager;
import com.google.api.services.cloudresourcemanager.v3.CloudResourceManagerScopes;
import com.google.api.services.cloudresourcemanager.v3.model.*;
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

  public Folders folders() {
    return new Folders(manager.folders());
  }

  /** See {@link CloudResourceManager.Folders}. */
  public class Folders {
    private final CloudResourceManager.Folders folders;

    private Folders(CloudResourceManager.Folders folders) {
      this.folders = folders;
    }

    /**
     * See {@link CloudResourceManager.Folders#testIamPermissions(String,
     * TestIamPermissionsRequest)}.
     */
    public TestIamPermissions testIamPermissions(String resource, TestIamPermissionsRequest request)
        throws IOException {
      return new TestIamPermissions(folders.testIamPermissions(resource, request));
    }

    /** See {@link CloudResourceManager.Folders.TestIamPermissions}. */
    public class TestIamPermissions extends AbstractRequestCow<TestIamPermissionsResponse> {
      private final CloudResourceManager.Folders.TestIamPermissions testIamPermissions;

      private TestIamPermissions(
          CloudResourceManager.Folders.TestIamPermissions testIamPermissions) {
        super(
            CloudResourceManagerOperation.GOOGLE_TEST_IAM_PERMISSIONS_FOLDER,
            clientConfig,
            operationAnnotator,
            testIamPermissions);
        this.testIamPermissions = testIamPermissions;
      }

      /** See {@link CloudResourceManager.Folders.TestIamPermissions#getResource()} */
      public String getResource() {
        return testIamPermissions.getResource();
      }

      public TestIamPermissions setResource(String resource) {
        testIamPermissions.setResource(resource);
        return this;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("resource", getResource());
        result.add(
            "content",
            new Gson().toJsonTree(testIamPermissions.getJsonContent()).getAsJsonObject());
        return result;
      }
    }
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
        super(
            CloudResourceManagerOperation.GOOGLE_CREATE_PROJECT,
            clientConfig,
            operationAnnotator,
            create);
        this.project = project;
      }

      @Override
      protected Optional<CloudResourceUid> resourceUidCreation() {
        return Optional.of(
            new CloudResourceUid()
                .googleProjectUid(new GoogleProjectUid().projectId(project.getProjectId())));
      }

      @Override
      protected Optional<ResourceMetadata> resourceCreationMetadata() {
        return Optional.of(new ResourceMetadata().googleProjectParent(project.getParent()));
      }

      @Override
      protected JsonObject serialize() {
        return new Gson().toJsonTree(project).getAsJsonObject();
      }
    }

    /**
     * See {@link CloudResourceManager.Projects#delete(String)}.
     *
     * <p>CRL will add the required 'projects/` prefix if not included in {@code name}.
     */
    public Delete delete(String name) throws IOException {
      return new Delete(projects.delete(prefixProjects(name)));
    }

    /** See {@link CloudResourceManager.Projects.Delete}. */
    public class Delete extends AbstractRequestCow<Operation> {
      private final CloudResourceManager.Projects.Delete delete;

      private Delete(CloudResourceManager.Projects.Delete delete) {
        super(
            CloudResourceManagerOperation.GOOGLE_DELETE_PROJECT,
            clientConfig,
            operationAnnotator,
            delete);
        this.delete = delete;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectName(delete.getName());
      }
    }

    /**
     * See {@link CloudResourceManager.Projects#get(String)}.
     *
     * <p>CRL will add the required 'projects/` prefix if not included in {@code name}.
     */
    public Get get(String name) throws IOException {
      return new Get(projects.get(prefixProjects(name)));
    }

    /** See {@link CloudResourceManager.Projects.Get} */
    public class Get extends AbstractRequestCow<Project> {
      private final CloudResourceManager.Projects.Get get;

      private Get(CloudResourceManager.Projects.Get get) {
        super(
            CloudResourceManagerOperation.GOOGLE_GET_PROJECT,
            clientConfig,
            operationAnnotator,
            get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectName(get.getName());
      }
    }

    /**
     * See {@link CloudResourceManager.Projects#getIamPolicy(String, GetIamPolicyRequest)}.
     *
     * <p>CRL will add the required 'projects/` prefix if not included in the name.
     */
    public GetIamPolicy getIamPolicy(String resource, GetIamPolicyRequest content)
        throws IOException {
      return new GetIamPolicy(projects.getIamPolicy(prefixProjects(resource), content));
    }

    /** See {@link CloudResourceManager.Projects.GetIamPolicy}. */
    public class GetIamPolicy extends AbstractRequestCow<Policy> {
      private final CloudResourceManager.Projects.GetIamPolicy getIamPolicy;

      private GetIamPolicy(CloudResourceManager.Projects.GetIamPolicy getIamPolicy) {
        super(
            CloudResourceManagerOperation.GOOGLE_GET_IAM_POLICY_PROJECT,
            clientConfig,
            operationAnnotator,
            getIamPolicy);
        this.getIamPolicy = getIamPolicy;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectName(getIamPolicy.getResource());
      }
    }

    /**
     * See {@link CloudResourceManager.Projects#setIamPolicy(String, SetIamPolicyRequest)} )}.
     *
     * <p>CRL will add the required 'projects/` prefix if not included in {@code resource}.
     */
    public SetIamPolicy setIamPolicy(String resource, SetIamPolicyRequest content)
        throws IOException {
      return new SetIamPolicy(projects.setIamPolicy(prefixProjects(resource), content));
    }

    /** See {@link CloudResourceManager.Projects.SetIamPolicy}. */
    public class SetIamPolicy extends AbstractRequestCow<Policy> {
      private final CloudResourceManager.Projects.SetIamPolicy setIamPolicy;

      private SetIamPolicy(CloudResourceManager.Projects.SetIamPolicy setIamPolicy) {
        super(
            CloudResourceManagerOperation.GOOGLE_SET_IAM_POLICY_PROJECT,
            clientConfig,
            operationAnnotator,
            setIamPolicy);
        this.setIamPolicy = setIamPolicy;
      }

      @Override
      protected JsonObject serialize() {
        return serializeProjectName(setIamPolicy.getResource());
      }
    }

    private JsonObject serializeProjectName(String projectId) {
      JsonObject result = new JsonObject();
      result.addProperty("project_name", projectId);
      return result;
    }
  }

  /**
   * Helper function to prefix Project operation arguments with the required "projects/" prefix.
   *
   * <p>This is used on {@link Projects} to allow CRL clients to directly pass project ids or
   * numbers, or the already prefixed name. It can also be called explicitly.
   */
  public static String prefixProjects(String name) {
    if (name.startsWith("projects/")) {
      return name;
    }
    return "projects/" + name;
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
            CloudResourceManagerOperation.GOOGLE_RESOURCE_MANAGER_OPERATION_GET,
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
}
