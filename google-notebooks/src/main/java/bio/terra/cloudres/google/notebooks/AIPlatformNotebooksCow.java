package bio.terra.cloudres.google.notebooks;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleAiNotebookInstanceUid;
import com.google.api.services.notebooks.v1.AIPlatformNotebooks;
import com.google.api.services.notebooks.v1.AIPlatformNotebooksScopes;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.ListInstancesResponse;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link AIPlatformNotebooks} */
public class AIPlatformNotebooksCow {
  private final Logger logger = LoggerFactory.getLogger(AIPlatformNotebooksCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final AIPlatformNotebooks notebooks;

  public AIPlatformNotebooksCow(
      ClientConfig clientConfig, AIPlatformNotebooks.Builder notebooksBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    notebooks = notebooksBuilder.build();
  }

  /** Create a {@link AIPlatformNotebooksCow} with some default configurations for convenience. */
  public static AIPlatformNotebooksCow create(
      ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new AIPlatformNotebooksCow(
        clientConfig,
        new AIPlatformNotebooks.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(
                    googleCredentials.createScoped(AIPlatformNotebooksScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  public Instances instances() {
    return new Instances(notebooks.projects().locations().instances());
  }

  /** See {@link AIPlatformNotebooks.Projects.Locations.Instances}. */
  public class Instances {
    private final AIPlatformNotebooks.Projects.Locations.Instances instances;

    private Instances(AIPlatformNotebooks.Projects.Locations.Instances instances) {
      this.instances = instances;
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#create(String, Instance)}. */
    public Create create(String parent, Instance instance) throws IOException {
      return new Create(instances.create(parent, instance), instance);
    }

    /** Create an instance with the {@link InstanceName}. See {@link #create(String, Instance)}. */
    public Create create(InstanceName instanceName, Instance instance) throws IOException {
      Preconditions.checkArgument(
          instance.getName() == null || instance.getName().equals(instanceName.formatName()),
          "The instance name and it's desired name should be the same if set.");
      return create(instanceName.formatParent(), instance).setInstanceId(instanceName.instanceId());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Create}. */
    public class Create extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Create create;
      private final Instance instance;

      private Create(
          AIPlatformNotebooks.Projects.Locations.Instances.Create create, Instance instance) {
        super(
            AIPlatformNotebooksOperation.GOOGLE_CREATE_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            create);
        this.instance = instance;
        this.create = create;
      }

      /**
       * Required. User-defined unique ID of this instance. See {@link
       * AIPlatformNotebooks.Projects.Locations.Instances.Create#getInstanceId()}
       */
      public String getInstanceId() {
        return create.getInstanceId();
      }

      /**
       * Required. User-defined unique ID of this instance. See {@link
       * AIPlatformNotebooks.Projects.Locations.Instances.Create#setInstanceId(String)} ()}
       */
      public Create setInstanceId(java.lang.String instanceId) {
        create.setInstanceId(instanceId);
        return this;
      }

      @Override
      protected Optional<CloudResourceUid> resourceUidCreation() {
        InstanceName instanceName =
            InstanceName.fromParentAndId(create.getParent(), create.getInstanceId());
        return Optional.of(
            new CloudResourceUid()
                .googleAiNotebookInstanceUid(
                    new GoogleAiNotebookInstanceUid()
                        .projectId(instanceName.projectId())
                        .location(instanceName.location())
                        .instanceId(instanceName.instanceId())));
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        InstanceName.fromParentAndId(create.getParent(), create.getInstanceId())
            .addProperties(result);
        result.add("instance", new Gson().toJsonTree(instance));
        return result;
      }
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#delete(String)}. */
    public Delete delete(String name) throws IOException {
      return new Delete(instances.delete(name));
    }

    /** Delete override for InstanceName. See {@link #delete(String)}. */
    public Delete delete(InstanceName instanceName) throws IOException {
      return delete(instanceName.formatName());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Delete}. */
    public class Delete extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Delete delete;

      private Delete(AIPlatformNotebooks.Projects.Locations.Instances.Delete delete) {
        super(
            AIPlatformNotebooksOperation.GOOGLE_DELETE_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            delete);
        this.delete = delete;
      }

      public String getName() {
        return delete.getName();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        InstanceName.fromNameFormat(delete.getName()).addProperties(result);
        return result;
      }
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#get(String)}. */
    public Get get(String name) throws IOException {
      return new Get(instances.get(name));
    }

    /** Get override for InstanceName. See {@link #get(String)}. */
    public Get get(InstanceName instanceName) throws IOException {
      return get(instanceName.formatName());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Get}. */
    public class Get extends AbstractRequestCow<Instance> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Get get;

      private Get(AIPlatformNotebooks.Projects.Locations.Instances.Get get) {
        super(
            AIPlatformNotebooksOperation.GOOGLE_GET_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            get);
        this.get = get;
      }

      public String getName() {
        return get.getName();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        InstanceName.fromNameFormat(get.getName()).addProperties(result);
        return result;
      }
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#list(String). } */
    public List list(String parent) throws IOException {
      return new List(instances.list(parent));
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.List}. */
    public class List extends AbstractRequestCow<ListInstancesResponse> {

      private final AIPlatformNotebooks.Projects.Locations.Instances.List list;

      private List(AIPlatformNotebooks.Projects.Locations.Instances.List list) {
        super(
            AIPlatformNotebooksOperation.GOOGLE_LIST_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            list);
        this.list = list;
      }

      /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.List#getParent()}. */
      public String getParent() {
        return list.getParent();
      }

      public List setParent(String parent) {
        list.setParent(parent);
        return this;
      }

      /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.List#getPageSize()}. */
      public Integer getPageSize() {
        return list.getPageSize();
      }

      public List setPageSize(Integer pageSize) {
        list.setPageSize(pageSize);
        return this;
      }

      /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.List#getPageToken()}. */
      public String getPageToken() {
        return list.getPageToken();
      }

      public List setPageToken(String pageToken) {
        list.setPageToken(pageToken);
        return this;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("parent", list.getParent());
        result.addProperty("page_size", list.getPageSize());
        result.addProperty("page_token", list.getPageToken());
        return result;
      }
    }
  }

  /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#operations()}. */
  public AIPlatformNotebooksCow.Operations operations() {
    return new AIPlatformNotebooksCow.Operations(notebooks.projects().locations().operations());
  }

  /** See {@link AIPlatformNotebooks.Projects.Locations.Operations}. */
  public class Operations {
    private final AIPlatformNotebooks.Projects.Locations.Operations operations;

    private Operations(AIPlatformNotebooks.Projects.Locations.Operations operations) {
      this.operations = operations;
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Operations#get(String)} */
    public AIPlatformNotebooksCow.Operations.Get get(String name) throws IOException {
      return new AIPlatformNotebooksCow.Operations.Get(operations.get(name));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Operations.Get get;

      public Get(AIPlatformNotebooks.Projects.Locations.Operations.Get get) {
        super(
            AIPlatformNotebooksOperation.GOOGLE_NOTEBOOKS_OPERATION_GET,
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
      return new OperationCow<>(operation, NotebooksOperationAdapter::new, op -> get(op.getName()));
    }
  }
}
