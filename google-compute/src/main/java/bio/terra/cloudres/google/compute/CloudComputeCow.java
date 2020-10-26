package bio.terra.cloudres.google.compute;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link CloudComputeCow} */
public class CloudComputeCow {
  private final Logger logger = LoggerFactory.getLogger(CloudComputeCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Compute compute;

  public CloudComputeCow(ClientConfig clientConfig, Compute.Builder computeBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.compute = computeBuilder.build();
  }

  /** Create a {@link CloudComputeCow} with some default configurations for convenience. */
  public static CloudComputeCow create(
      ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new CloudComputeCow(
        clientConfig,
        new Compute.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(googleCredentials.createScoped(ComputeScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  public Networks networks() {
    return new Networks(compute.networks());
  }

  /** See {@link Compute.Networks}. */
  public class Networks {
    private final Compute.Networks networks;

    private Networks(Compute.Networks networks) {
      this.networks = networks;
    }

    /** See {@link Compute.Networks#insert(String, Network)}. */
    public Insert insert(String projectId, Network network) throws IOException {
      return new Insert(networks.insert(projectId, network), projectId, network);
    }

    /** See {@link Compute.Networks.Insert}. */
    public class Insert extends AbstractRequestCow<Operation> {
      private final String projectId;
      private final Network network;

      public Insert(Compute.Networks.Insert insert, String projectId, Network network) {
        super(CloudOperation.GOOGLE_INSERT_NETWORK, clientConfig, operationAnnotator, insert);
        this.network = network;
        this.projectId = projectId;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", projectId);
        result.add("network", new Gson().toJsonTree(network).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Compute.Networks#get(String, String)}. */
    public Get get(String projectId, String networkName) throws IOException {
      return new Get(networks.get(projectId, networkName));
    }

    /** See {@link Compute.Networks.Get}. */
    public class Get extends AbstractRequestCow<Network> {
      private final Compute.Networks.Get get;

      public Get(Compute.Networks.Get get) {
        super(CloudOperation.GOOGLE_GET_NETWORK, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("network_name", get.getNetwork());
        return result;
      }
    }
  }

  public Subnetworks subnetworks() {
    return new Subnetworks(compute.subnetworks());
  }

  /** See {@link Compute.Subnetworks}. */
  public class Subnetworks {
    private final Compute.Subnetworks subnetworks;

    private Subnetworks(Compute.Subnetworks subnetworks) {
      this.subnetworks = subnetworks;
    }

    /** See {@link Compute.Subnetworks#insert(String, String, Subnetwork)}. */
    public Insert insert(String projectId, String region, Subnetwork subnetwork)
        throws IOException {
      return new Insert(
          subnetworks.insert(projectId, region, subnetwork), projectId, region, subnetwork);
    }

    /** See {@link Compute.Subnetworks.Insert}. */
    public class Insert extends AbstractRequestCow<Operation> {
      private final String projectId;
      private final String region;
      private final Subnetwork subnetwork;

      public Insert(
          Compute.Subnetworks.Insert insert,
          String projectId,
          String region,
          Subnetwork subnetwork) {
        super(CloudOperation.GOOGLE_INSERT_SUBNETWORK, clientConfig, operationAnnotator, insert);
        this.subnetwork = subnetwork;
        this.region = region;
        this.projectId = projectId;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", projectId);
        result.addProperty("region", region);
        result.add("subnetwork", new Gson().toJsonTree(subnetwork).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Compute.Subnetworks#get(String, String, String)}. */
    public Get get(String projectId, String region, String networkName) throws IOException {
      return new Get(subnetworks.get(projectId, region, networkName));
    }

    /** See {@link Compute.Subnetworks.Get}. */
    public class Get extends AbstractRequestCow<Subnetwork> {
      private final Compute.Subnetworks.Get get;

      public Get(Compute.Subnetworks.Get get) {
        super(CloudOperation.GOOGLE_GET_SUBNETWORK, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("region", get.getRegion());
        result.addProperty("network_name", get.getSubnetwork());
        return result;
      }
    }
  }

  public Firewalls firewalls() {
    return new Firewalls(compute.firewalls());
  }

  /** See {@link Compute.Firewalls}. */
  public class Firewalls {
    private final Compute.Firewalls firewalls;

    private Firewalls(Compute.Firewalls firewalls) {
      this.firewalls = firewalls;
    }

    /** See {@link Compute.Firewalls#insert(String, Firewall)}. */
    public Insert insert(String projectId, Firewall firewall) throws IOException {
      return new Insert(firewalls.insert(projectId, firewall), projectId, firewall);
    }

    /** See {@link Compute.Firewalls.Insert}. */
    public class Insert extends AbstractRequestCow<Operation> {
      private final String projectId;
      private final Firewall firewall;

      public Insert(Compute.Firewalls.Insert insert, String projectId, Firewall firewall) {
        super(CloudOperation.GOOGLE_INSERT_FIREWALL, clientConfig, operationAnnotator, insert);
        this.firewall = firewall;
        this.projectId = projectId;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", projectId);
        result.add("firewall", new Gson().toJsonTree(firewall).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Compute.Firewalls#get(String, String)}. */
    public Get get(String projectId, String firewallName) throws IOException {
      return new Get(firewalls.get(projectId, firewallName));
    }

    /** See {@link Compute.Firewalls.Get}. */
    public class Get extends AbstractRequestCow<Firewall> {
      private final Compute.Firewalls.Get get;

      public Get(Compute.Firewalls.Get get) {
        super(CloudOperation.GOOGLE_GET_FIREWAL, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("firewall_name", get.getFirewall());
        return result;
      }
    }
  }

  public Routes routes() {
    return new Routes(compute.routes());
  }

  /** See {@link Compute.Routes}. */
  public class Routes {
    private final Compute.Routes routes;

    private Routes(Compute.Routes routes) {
      this.routes = routes;
    }

    /** See {@link Compute.Routes#insert(String, Route)}. */
    public Insert insert(String projectId, Route route) throws IOException {
      return new Insert(routes.insert(projectId, route), projectId, route);
    }

    /** See {@link Compute.Routes.Insert}. */
    public class Insert extends AbstractRequestCow<Operation> {
      private final String projectId;
      private final Route route;

      public Insert(Compute.Routes.Insert insert, String projectId, Route route) {
        super(CloudOperation.GOOGLE_INSERT_ROUTE, clientConfig, operationAnnotator, insert);
        this.route = route;
        this.projectId = projectId;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", projectId);
        result.add("route", new Gson().toJsonTree(route).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Compute.Routes#get(String, String)}. */
    public Get get(String projectId, String routeName) throws IOException {
      return new Get(routes.get(projectId, routeName));
    }

    /** See {@link Compute.Routes.Get}. */
    public class Get extends AbstractRequestCow<Route> {
      private final Compute.Routes.Get get;

      public Get(Compute.Routes.Get get) {
        super(CloudOperation.GOOGLE_GET_ROUTE, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("route_name", get.getRoute());
        return result;
      }
    }
  }
  
  /**
   * See {@link Compute#globalOperations()}.
   *
   * <p>Operations can be global, regional or zonal
   *
   * @see <a
   *     href="https://cloud.google.com/compute/docs/regions-zones/global-regional-zonal-resources">Global,
   *     Regional, and Zonal Resources</a>
   */
  public GlobalOperations globalOperations() {
    return new GlobalOperations(compute.globalOperations());
  }

  public class GlobalOperations {
    private final Compute.GlobalOperations operations;

    private GlobalOperations(Compute.GlobalOperations operations) {
      this.operations = operations;
    }

    /** See {@link Compute.GlobalOperations#get(String, String)} */
    public Get get(String projectId, String operationName) throws IOException {
      return new Get(operations.get(projectId, operationName));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final Compute.GlobalOperations.Get get;

      public Get(Compute.GlobalOperations.Get get) {
        super(
            CloudOperation.GOOGLE_COMPUTE_GLOBAL_OPERATION_GET,
            clientConfig,
            operationAnnotator,
            get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("operation_name", get.getOperation());
        return result;
      }
    }

    public OperationCow<Operation> operationCow(String projectId, Operation operation) {
      return new OperationCow<>(
          operation, ComputeOperationAdapter::new, op -> get(projectId, op.getName()));
    }
  }

  /**
   * See {@link Compute#regionOperations()}.
   *
   * <p>Operations can be global, regional or zonal
   *
   * @see <a
   *     href="https://cloud.google.com/compute/docs/regions-zones/global-regional-zonal-resources">Global,
   *     Regional, and Zonal Resources</a>
   */
  public RegionOperations regionalOperations() {
    return new RegionOperations(compute.regionOperations());
  }

  public class RegionOperations {
    private final Compute.RegionOperations operations;

    private RegionOperations(Compute.RegionOperations operations) {
      this.operations = operations;
    }

    /** See {@link Compute.RegionOperations#get(String, String, String)} */
    public Get get(String projectId, String region, String operationName) throws IOException {
      return new Get(operations.get(projectId, region, operationName));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final Compute.RegionOperations.Get get;

      public Get(Compute.RegionOperations.Get get) {
        super(
            CloudOperation.GOOGLE_COMPUTE_REGION_OPERATION_GET,
            clientConfig,
            operationAnnotator,
            get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("region", get.getRegion());
        result.addProperty("operation_name", get.getOperation());
        return result;
      }
    }

    public OperationCow<Operation> operationCow(
        String projectId, String region, Operation operation) {
      return new OperationCow<>(
          operation, ComputeOperationAdapter::new, op -> get(projectId, region, op.getName()));
    }
  }
}
