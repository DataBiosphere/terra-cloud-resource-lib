package bio.terra.cloudres.google.dataproc;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.dataproc.Dataproc;
import com.google.api.services.dataproc.Dataproc.Projects.Regions;
import com.google.api.services.dataproc.DataprocScopes;
import com.google.api.services.dataproc.model.Cluster;
import com.google.api.services.dataproc.model.GetIamPolicyRequest;
import com.google.api.services.dataproc.model.ListClustersResponse;
import com.google.api.services.dataproc.model.Operation;
import com.google.api.services.dataproc.model.Policy;
import com.google.api.services.dataproc.model.SetIamPolicyRequest;
import com.google.api.services.dataproc.model.StartClusterRequest;
import com.google.api.services.dataproc.model.StopClusterRequest;
import com.google.api.services.dataproc.model.TestIamPermissionsRequest;
import com.google.api.services.dataproc.model.TestIamPermissionsResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link DataprocCow} */
public class DataprocCow {
  private final Logger logger = LoggerFactory.getLogger(DataprocCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Dataproc dataproc;

  public DataprocCow(ClientConfig clientConfig, Dataproc.Builder dataprocBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.dataproc = dataprocBuilder.build();
  }

  /** Create a {@link DataprocCow} with some default configurations for convenience. */
  public static DataprocCow create(ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new DataprocCow(
        clientConfig,
        new Dataproc.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(googleCredentials.createScoped(DataprocScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  public Clusters clusters() {
    return new Clusters(dataproc.projects().regions().clusters());
  }

  /** See {@link Dataproc.Projects.Regions.Clusters}. */
  public class Clusters {
    private final Dataproc.Projects.Regions.Clusters clusters;

    private Clusters(Dataproc.Projects.Regions.Clusters clusters) {
      this.clusters = clusters;
    }

    public Create create(ClusterName clusterName, Cluster cluster) throws IOException {
      Preconditions.checkArgument(
          cluster.getClusterName() == null || cluster.getClusterName().equals(clusterName.name()),
          "The cluster name the cluster object must match the name set in clusterName");
      return new Create(
          clusters.create(clusterName.projectId(), clusterName.region(), cluster), cluster);
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.Create}. */
    public class Create extends AbstractRequestCow<Operation> {
      private final Dataproc.Projects.Regions.Clusters.Create create;
      private final Cluster cluster;

      private Create(Dataproc.Projects.Regions.Clusters.Create create, Cluster cluster) {
        super(DataprocOperation.GOOGLE_CREATE_CLUSTER, clientConfig, operationAnnotator, create);
        this.cluster = cluster;
        this.create = create;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", create.getProjectId());
        result.addProperty("region", create.getRegion());
        result.add("cluster", new Gson().toJsonTree(cluster));
        return result;
      }
    }

    public Patch patch(
        String project,
        String region,
        Cluster cluster,
        @Nullable String updateMask,
        @Nullable String gracefulDecommissionTimeout)
        throws IOException {
      return new Patch(
          clusters
              .patch(project, region, cluster.getClusterName(), cluster)
              .setUpdateMask(updateMask)
              .setGracefulDecommissionTimeout(gracefulDecommissionTimeout));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.Patch}. */
    public class Patch extends AbstractRequestCow<Operation> {
      private final Dataproc.Projects.Regions.Clusters.Patch patch;

      private Patch(Dataproc.Projects.Regions.Clusters.Patch patch) {
        super(DataprocOperation.GOOGLE_CREATE_CLUSTER, clientConfig, operationAnnotator, patch);
        this.patch = patch;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", patch.getProjectId());
        result.addProperty("region", patch.getRegion());
        result.addProperty("clusterName", patch.getClusterName());
        result.add("updateMask", new Gson().toJsonTree(patch.getUpdateMask()));
        return result;
      }
    }

    /** See {@link Dataproc.Projects.Regions.Clusters#delete(String, String, String)}. */
    public Delete delete(ClusterName clusterName) throws IOException {
      return new Delete(
          clusters.delete(clusterName.projectId(), clusterName.region(), clusterName.name()));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.Delete}. */
    public class Delete extends AbstractRequestCow<Operation> {
      private final Dataproc.Projects.Regions.Clusters.Delete delete;

      private Delete(Dataproc.Projects.Regions.Clusters.Delete delete) {
        super(DataprocOperation.GOOGLE_DELETE_CLUSTER, clientConfig, operationAnnotator, delete);
        this.delete = delete;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", delete.getProjectId());
        result.addProperty("region", delete.getRegion());
        result.addProperty("clusterName", delete.getClusterName());
        return result;
      }
    }

    /** See {@link Dataproc.Projects.Regions.Clusters#get(String, String, String)}. */
    public Get get(ClusterName clusterName) throws IOException {
      return new Get(
          clusters.get(clusterName.projectId(), clusterName.region(), clusterName.name()));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.Get}. */
    public class Get extends AbstractRequestCow<Cluster> {
      private final Dataproc.Projects.Regions.Clusters.Get get;

      private Get(Dataproc.Projects.Regions.Clusters.Get get) {
        super(DataprocOperation.GOOGLE_GET_CLUSTER, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", get.getProjectId());
        result.addProperty("region", get.getRegion());
        result.addProperty("cluster", get.getClusterName());
        return result;
      }
    }

    /** See {@link Dataproc.Projects.Regions.Clusters#list(String, String)}. */
    public List list(String project, String region) throws IOException {
      return new List(clusters.list(project, region));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.List}. */
    public class List extends AbstractRequestCow<ListClustersResponse> {

      private final Dataproc.Projects.Regions.Clusters.List list;

      private List(Dataproc.Projects.Regions.Clusters.List list) {
        super(DataprocOperation.GOOGLE_LIST_CLUSTER, clientConfig, operationAnnotator, list);
        this.list = list;
      }

      /** See {@link Dataproc.Projects.Regions.Clusters.List#getProjectId()}. */
      public String getProjectId() {
        return list.getProjectId();
      }

      public List setProjectId(String project) {
        list.setProjectId(project);
        return this;
      }

      /** See {@link Dataproc.Projects.Regions.Clusters.List#getRegion()}. */
      public String getRegion() {
        return list.getRegion();
      }

      public List setRegion(String region) {
        list.setRegion(region);
        return this;
      }

      /** See {@link Dataproc.Projects.Regions.Clusters.List#getFilter()}. */
      public String getFilter() {
        return list.getFilter();
      }

      public List setFilter(String filter) {
        list.setFilter(filter);
        return this;
      }

      /** See {@link Regions.Clusters.List#getPageSize()} ()}. */
      public int getPageSize() {
        return list.getPageSize();
      }

      public List setPageSize(int pageSize) {
        list.setPageSize(pageSize);
        return this;
      }

      /** See {@link Dataproc.Projects.Regions.Clusters.List#getPageToken()}. */
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
        result.addProperty("project", list.getProjectId());
        result.addProperty("region", list.getRegion());
        result.addProperty("filter", list.getFilter());
        result.addProperty("page_size", list.getPageSize());
        result.addProperty("page_token", list.getPageToken());
        return result;
      }
    }

    /**
     * See {@link Dataproc.Projects.Regions.Clusters.Start#start(String, String, String,
     * StartClusterRequest)}.
     */
    public Start start(ClusterName clusterName) throws IOException {
      return new Start(
          clusters.start(
              clusterName.projectId(),
              clusterName.region(),
              clusterName.name(),
              new StartClusterRequest()));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.Start}. */
    public class Start extends AbstractRequestCow<Operation> {
      private final Dataproc.Projects.Regions.Clusters.Start start;

      private Start(Dataproc.Projects.Regions.Clusters.Start start) {
        super(DataprocOperation.GOOGLE_START_CLUSTER, clientConfig, operationAnnotator, start);
        this.start = start;
      }

      public String getClusterName() {
        return start.getClusterName();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", start.getProjectId());
        result.addProperty("region", start.getRegion());
        result.addProperty("clusterName", start.getClusterName());
        return result;
      }
    }

    /**
     * See {@link Dataproc.Projects.Regions.Clusters.Stop#stop(String, String, String,
     * StopClusterRequest)}.
     */
    public Stop stop(ClusterName clusterName) throws IOException {
      return new Stop(
          clusters.stop(
              clusterName.projectId(),
              clusterName.region(),
              clusterName.name(),
              new StopClusterRequest()));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.Stop}. */
    public class Stop extends AbstractRequestCow<Operation> {
      private final Dataproc.Projects.Regions.Clusters.Stop stop;

      private Stop(Dataproc.Projects.Regions.Clusters.Stop stop) {
        super(DataprocOperation.GOOGLE_STOP_CLUSTER, clientConfig, operationAnnotator, stop);
        this.stop = stop;
      }

      public String getClusterName() {
        return stop.getClusterName();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("projectId", stop.getProjectId());
        result.addProperty("region", stop.getRegion());
        result.addProperty("cluster", stop.getClusterName());
        return result;
      }
    }

    /** See {@link Dataproc.Projects.Regions.Clusters#getIamPolicy(String, GetIamPolicyRequest)} */
    public GetIamPolicy getIamPolicy(ClusterName clusterName) throws IOException {
      return new GetIamPolicy(
          clusters.getIamPolicy(clusterName.formatName(), new GetIamPolicyRequest()));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.GetIamPolicy}. */
    public class GetIamPolicy extends AbstractRequestCow<Policy> {
      private final Dataproc.Projects.Regions.Clusters.GetIamPolicy getIamPolicy;

      private GetIamPolicy(Dataproc.Projects.Regions.Clusters.GetIamPolicy getIamPolicy) {
        super(
            DataprocOperation.GOOGLE_GET_IAM_POLICY_CLUSTER,
            clientConfig,
            operationAnnotator,
            getIamPolicy);
        this.getIamPolicy = getIamPolicy;
      }

      public String getResource() {
        return getIamPolicy.getResource();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("resource", getIamPolicy.getResource());
        result.add(
            "content", new Gson().toJsonTree(getIamPolicy.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /**
     * See {@link Dataproc.Projects.Regions.Clusters.SetIamPolicy#setIamPolicy(String,
     * SetIamPolicyRequest)}
     */
    public SetIamPolicy setIamPolicy(ClusterName clusterName, SetIamPolicyRequest content)
        throws IOException {
      return new SetIamPolicy(clusters.setIamPolicy(clusterName.formatName(), content));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.SetIamPolicy}. */
    public class SetIamPolicy extends AbstractRequestCow<Policy> {
      private final Dataproc.Projects.Regions.Clusters.SetIamPolicy setIamPolicy;

      private SetIamPolicy(Dataproc.Projects.Regions.Clusters.SetIamPolicy setIamPolicy) {
        super(
            DataprocOperation.GOOGLE_SET_IAM_POLICY_CLUSTER,
            clientConfig,
            operationAnnotator,
            setIamPolicy);
        this.setIamPolicy = setIamPolicy;
      }

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
        result.addProperty("resource", setIamPolicy.getResource());
        result.add(
            "content", new Gson().toJsonTree(setIamPolicy.getJsonContent()).getAsJsonObject());
        return result;
      }
    }

    /**
     * See {@link Dataproc.Projects.Regions.Clusters.TestIamPermissions#testIamPermissions(String,
     * TestIamPermissionsRequest)}.
     */
    public TestIamPermissions testIamPermissions(
        ClusterName clusterName, TestIamPermissionsRequest content) throws IOException {
      return new TestIamPermissions(clusters.testIamPermissions(clusterName.formatName(), content));
    }

    /** See {@link Dataproc.Projects.Regions.Clusters.TestIamPermissions}. */
    public class TestIamPermissions extends AbstractRequestCow<TestIamPermissionsResponse> {
      private final Dataproc.Projects.Regions.Clusters.TestIamPermissions testIamPermissions;

      private TestIamPermissions(
          Dataproc.Projects.Regions.Clusters.TestIamPermissions testIamPermissions) {
        super(
            DataprocOperation.GOOGLE_TEST_IAM_PERMISSIONS_CLUSTER,
            clientConfig,
            operationAnnotator,
            testIamPermissions);
        this.testIamPermissions = testIamPermissions;
      }

      public String getResource() {
        return testIamPermissions.getResource();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("resource", testIamPermissions.getResource());
        result.add(
            "content",
            new Gson().toJsonTree(testIamPermissions.getJsonContent()).getAsJsonObject());
        return result;
      }
    }
  }

  /**
   * See {@link Dataproc.Projects.Regions.Operations#operations()}.
   *
   * <p>Dataproc operations has separate apis per region and location. This class wraps the region
   * operations api.
   *
   * @see <a
   *     href="https://cloud.google.com/dataproc/docs/reference/rest/v1/projects.locations.operations">Location
   *     Operations</a>
   * @see <a
   *     href="https://cloud.google.com/dataproc/docs/reference/rest/v1/projects.regions.operations">Region
   *     Operations</a>
   */
  public RegionOperations regionOperations() {
    return new RegionOperations(dataproc.projects().regions().operations());
  }

  public class RegionOperations {
    private final Dataproc.Projects.Regions.Operations regionOperations;

    private RegionOperations(Dataproc.Projects.Regions.Operations regionOperations) {
      this.regionOperations = regionOperations;
    }

    /** See {@link Dataproc.Projects.Regions.Operations#get(String)} */
    public Get get(String name) throws IOException {
      return new Get(regionOperations.get(name));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final Dataproc.Projects.Regions.Operations.Get get;

      public Get(Dataproc.Projects.Regions.Operations.Get get) {
        super(
            DataprocOperation.GOOGLE_DATAPROC_OPERATION_GET, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("name", get.getName());
        return result;
      }
    }

    public OperationCow<Operation> operationCow(Operation operation) {
      return new OperationCow<>(operation, DataprocOperationAdapter::new, op -> get(op.getName()));
    }
  }
}
