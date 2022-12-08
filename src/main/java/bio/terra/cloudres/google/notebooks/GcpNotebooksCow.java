package bio.terra.cloudres.google.notebooks;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleAiNotebookInstanceUid;
import com.google.api.services.notebooks.v1.AIPlatformNotebooks;
import com.google.api.services.notebooks.v1.AIPlatformNotebooks.Projects.Locations.Instances.UpdateMetadataItems;
import com.google.api.services.notebooks.v1.AIPlatformNotebooksScopes;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.ListInstancesResponse;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.api.services.notebooks.v1.model.Policy;
import com.google.api.services.notebooks.v1.model.SetIamPolicyRequest;
import com.google.api.services.notebooks.v1.model.StartInstanceRequest;
import com.google.api.services.notebooks.v1.model.StopInstanceRequest;
import com.google.api.services.notebooks.v1.model.TestIamPermissionsRequest;
import com.google.api.services.notebooks.v1.model.TestIamPermissionsResponse;
import com.google.api.services.notebooks.v1.model.UpdateInstanceMetadataItemsRequest;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import bio.terra.cloudres.common.notebooks.NotebooksCow;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link AIPlatformNotebooks} */
public class GcpNotebooksCow implements NotebooksCow {
  private final Logger logger = LoggerFactory.getLogger(GcpNotebooksCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final AIPlatformNotebooks notebooks;

  public GcpNotebooksCow(
      ClientConfig clientConfig, AIPlatformNotebooks.Builder notebooksBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    notebooks = notebooksBuilder.build();
  }

  /** Create a {@link GcpNotebooksCow} with some default configurations for convenience. */
  public static GcpNotebooksCow create(
      ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new GcpNotebooksCow(
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

    /** Create an instance with the {@link GcpNotebookInstanceName}. See {@link #create(String, Instance)}. */
    public Create create(GcpNotebookInstanceName gcpNotebookInstanceName, Instance instance) throws IOException {
      Preconditions.checkArgument(
          instance.getName() == null || instance.getName().equals(gcpNotebookInstanceName.formatName()),
          "The instance name and it's desired name should be the same if set.");
      return create(gcpNotebookInstanceName.formatParent(), instance).setInstanceId(gcpNotebookInstanceName.instanceId());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Create}. */
    public class Create extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Create create;
      private final Instance instance;

      private Create(
          AIPlatformNotebooks.Projects.Locations.Instances.Create create, Instance instance) {
        super(
            GcpNotebooksOperation.GOOGLE_CREATE_NOTEBOOKS_INSTANCE,
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
        GcpNotebookInstanceName gcpNotebookInstanceName =
            GcpNotebookInstanceName.fromParentAndId(create.getParent(), create.getInstanceId());
        return Optional.of(
            new CloudResourceUid()
                .googleAiNotebookInstanceUid(
                    new GoogleAiNotebookInstanceUid()
                        .projectId(gcpNotebookInstanceName.projectId())
                        .location(gcpNotebookInstanceName.location())
                        .instanceId(gcpNotebookInstanceName.instanceId())));
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        GcpNotebookInstanceName.fromParentAndId(create.getParent(), create.getInstanceId())
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
    public Delete delete(GcpNotebookInstanceName gcpNotebookInstanceName) throws IOException {
      return delete(gcpNotebookInstanceName.formatName());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Delete}. */
    public class Delete extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Delete delete;

      private Delete(AIPlatformNotebooks.Projects.Locations.Instances.Delete delete) {
        super(
            GcpNotebooksOperation.GOOGLE_DELETE_NOTEBOOKS_INSTANCE,
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
        GcpNotebookInstanceName.fromNameFormat(delete.getName()).addProperties(result);
        return result;
      }
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#get(String)}. */
    public Get get(String name) throws IOException {
      return new Get(instances.get(name));
    }

    /** Get override for InstanceName. See {@link #get(String)}. */
    public Get get(GcpNotebookInstanceName gcpNotebookInstanceName) throws IOException {
      return get(gcpNotebookInstanceName.formatName());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Get}. */
    public class Get extends AbstractRequestCow<Instance> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Get get;

      private Get(AIPlatformNotebooks.Projects.Locations.Instances.Get get) {
        super(
            GcpNotebooksOperation.GOOGLE_GET_NOTEBOOKS_INSTANCE,
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
        GcpNotebookInstanceName.fromNameFormat(get.getName()).addProperties(result);
        return result;
      }
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#list(String)}. */
    public List list(String parent) throws IOException {
      return new List(instances.list(parent));
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.List}. */
    public class List extends AbstractRequestCow<ListInstancesResponse> {

      private final AIPlatformNotebooks.Projects.Locations.Instances.List list;

      private List(AIPlatformNotebooks.Projects.Locations.Instances.List list) {
        super(
            GcpNotebooksOperation.GOOGLE_LIST_NOTEBOOKS_INSTANCE,
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

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#getIamPolicy(String)} */
    public GetIamPolicy getIamPolicy(String resource) throws IOException {
      return new GetIamPolicy(instances.getIamPolicy(resource));
    }

    /** {@link #getIamPolicy(String)} override for {@link GcpNotebookInstanceName}. */
    public GetIamPolicy getIamPolicy(GcpNotebookInstanceName gcpNotebookInstanceName) throws IOException {
      return getIamPolicy(gcpNotebookInstanceName.formatName());
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.GetIamPolicy}. */
    public class GetIamPolicy extends AbstractRequestCow<Policy> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.GetIamPolicy getIamPolicy;

      private GetIamPolicy(
          AIPlatformNotebooks.Projects.Locations.Instances.GetIamPolicy getIamPolicy) {
        super(
            GcpNotebooksOperation.GOOGLE_GET_IAM_POLICY_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            getIamPolicy);
        this.getIamPolicy = getIamPolicy;
      }

      /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.GetIamPolicy#getResource()} */
      public String getResource() {
        return getIamPolicy.getResource();
      }

      public GetIamPolicy setResource(String resource) {
        getIamPolicy.setResource(resource);
        return this;
      }

      /**
       * See {@link
       * AIPlatformNotebooks.Projects.Locations.Instances.GetIamPolicy#getOptionsRequestedPolicyVersion()}
       */
      public Integer getOptionsRequestedPolicyVersion() {
        return getIamPolicy.getOptionsRequestedPolicyVersion();
      }

      public GetIamPolicy setOptionsRequestedPolicyVersion(Integer version) {
        getIamPolicy.setOptionsRequestedPolicyVersion(version);
        return this;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        GcpNotebookInstanceName.fromNameFormat(getResource()).addProperties(result);
        result.addProperty(
            "options_requested_policy_version", getIamPolicy.getOptionsRequestedPolicyVersion());
        return result;
      }
    }

    /**
     * See {@link AIPlatformNotebooks.Projects.Locations.Instances#setIamPolicy(String,
     * SetIamPolicyRequest)}
     */
    public SetIamPolicy setIamPolicy(String resource, SetIamPolicyRequest content)
        throws IOException {
      return new SetIamPolicy(instances.setIamPolicy(resource, content));
    }

    /** {@link #setIamPolicy(String, SetIamPolicyRequest)} override for {@link GcpNotebookInstanceName}. */
    public SetIamPolicy setIamPolicy(GcpNotebookInstanceName gcpNotebookInstanceName, SetIamPolicyRequest content)
        throws IOException {
      return setIamPolicy(gcpNotebookInstanceName.formatName(), content);
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.SetIamPolicy}. */
    public class SetIamPolicy extends AbstractRequestCow<Policy> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.SetIamPolicy setIamPolicy;

      private SetIamPolicy(
          AIPlatformNotebooks.Projects.Locations.Instances.SetIamPolicy setIamPolicy) {
        super(
            GcpNotebooksOperation.GOOGLE_SET_IAM_POLICY_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            setIamPolicy);
        this.setIamPolicy = setIamPolicy;
      }

      /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.SetIamPolicy#getResource()} */
      public String getResource() {
        return setIamPolicy.getResource();
      }

      public SetIamPolicy setResource(String resource) {
        setIamPolicy.setResource(resource);
        return this;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        GcpNotebookInstanceName.fromNameFormat(getResource()).addProperties(result);
        result.add(
            "content", new Gson().toJsonTree(setIamPolicy.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /**
     * See {@link AIPlatformNotebooks.Projects.Locations.Instances#start(String,
     * StartInstanceRequest)}.
     */
    public Start start(String name) throws IOException {
      // StartRequestInstance is defined to always be empty for the API request. It's existence is a
      // quirk of Google's auto code generation.
      return new Start(instances.start(name, new StartInstanceRequest()));
    }

    /** {@link #start(String)} override for {@link GcpNotebookInstanceName}. */
    public Start start(GcpNotebookInstanceName gcpNotebookInstanceName) throws IOException {
      return start(gcpNotebookInstanceName.formatName());
    }

    public class Start extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Start start;

      private Start(AIPlatformNotebooks.Projects.Locations.Instances.Start start) {
        super(
            GcpNotebooksOperation.GOOGLE_START_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            start);
        this.start = start;
      }

      public String getName() {
        return start.getName();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        GcpNotebookInstanceName.fromNameFormat(getName()).addProperties(result);
        return result;
      }
    }

    /**
     * See {@link AIPlatformNotebooks.Projects.Locations.Instances#stop(String,
     * StopInstanceRequest)}.
     */
    public Stop stop(String name) throws IOException {
      // StopRequestInstance is defined to always be empty for the API request. It's existence is a
      // quirk of Google's auto code generation.
      return new Stop(instances.stop(name, new StopInstanceRequest()));
    }

    /** {@link #stop(String)} override for {@link GcpNotebookInstanceName}. */
    public Stop stop(GcpNotebookInstanceName gcpNotebookInstanceName) throws IOException {
      return stop(gcpNotebookInstanceName.formatName());
    }

    public class Stop extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Stop stop;

      private Stop(AIPlatformNotebooks.Projects.Locations.Instances.Stop stop) {
        super(
            GcpNotebooksOperation.GOOGLE_STOP_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            stop);
        this.stop = stop;
      }

      public String getName() {
        return stop.getName();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        GcpNotebookInstanceName.fromNameFormat(getName()).addProperties(result);
        return result;
      }
    }

    /**
     * See {@link AIPlatformNotebooks.Projects.Locations.Instances#updateMetadataItems(String,
     * UpdateInstanceMetadataItemsRequest)}.
     */
    public UpdateMetadataItems updateMetadataItems(String name, Map<String, String> metadata)
        throws IOException {
      return instances.updateMetadataItems(
          name, new UpdateInstanceMetadataItemsRequest().setItems(metadata));
    }

    /**
     * See {@link AIPlatformNotebooks.Projects.Locations.Instances#updateMetadataItems(String,
     * UpdateInstanceMetadataItemsRequest)}.
     */
    public UpdateMetadataItems updateMetadataItems(
        GcpNotebookInstanceName gcpNotebookInstanceName, Map<String, String> metadata) throws IOException {
      return instances.updateMetadataItems(
          gcpNotebookInstanceName.formatName(), new UpdateInstanceMetadataItemsRequest().setItems(metadata));
    }

    /**
     * See {@link AIPlatformNotebooks.Projects.Locations.Instances#testIamPermissions(String,
     * TestIamPermissionsRequest)}.
     */
    public TestIamPermissions testIamPermissions(String resource, TestIamPermissionsRequest content)
        throws IOException {
      return new TestIamPermissions(instances.testIamPermissions(resource, content));
    }

    /**
     * Test the IAM permissoins of a service account with the {@link GcpNotebookInstanceName}. See {@link
     * #testIamPermissions(String, TestIamPermissionsRequest)}.
     */
    public TestIamPermissions testIamPermissions(
        GcpNotebookInstanceName name, TestIamPermissionsRequest content) throws IOException {
      return testIamPermissions(name.formatName(), content);
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.TestIamPermissions}. */
    public class TestIamPermissions extends AbstractRequestCow<TestIamPermissionsResponse> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.TestIamPermissions
          testIamPermissions;

      private TestIamPermissions(
          AIPlatformNotebooks.Projects.Locations.Instances.TestIamPermissions testIamPermissions) {
        super(
            GcpNotebooksOperation.GOOGLE_TEST_IAM_PERMISSIONS_NOTEBOOKS_INSTANCE,
            clientConfig,
            operationAnnotator,
            testIamPermissions);
        this.testIamPermissions = testIamPermissions;
      }

      /**
       * See {@link
       * AIPlatformNotebooks.Projects.Locations.Instances.TestIamPermissions#getResource()}
       */
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
        GcpNotebookInstanceName.fromNameFormat(getResource()).addProperties(result);
        result.add(
            "content",
            new Gson().toJsonTree(testIamPermissions.getJsonContent()).getAsJsonObject());
        return result;
      }
    }
  }

  /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#operations()}. */
  public GcpNotebooksCow.Operations operations() {
    return new GcpNotebooksCow.Operations(notebooks.projects().locations().operations());
  }

  /** See {@link AIPlatformNotebooks.Projects.Locations.Operations}. */
  public class Operations {
    private final AIPlatformNotebooks.Projects.Locations.Operations operations;

    private Operations(AIPlatformNotebooks.Projects.Locations.Operations operations) {
      this.operations = operations;
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Operations#get(String)} */
    public GcpNotebooksCow.Operations.Get get(String name) throws IOException {
      return new GcpNotebooksCow.Operations.Get(operations.get(name));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Operations.Get get;

      public Get(AIPlatformNotebooks.Projects.Locations.Operations.Get get) {
        super(
            GcpNotebooksOperation.GOOGLE_NOTEBOOKS_OPERATION_GET,
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
      return new OperationCow<>(operation, GcpNotebooksOperationAdapter::new, op -> get(op.getName()));
    }
  }
}
