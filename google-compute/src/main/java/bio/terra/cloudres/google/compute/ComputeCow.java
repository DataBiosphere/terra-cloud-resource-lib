package bio.terra.cloudres.google.compute;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Network;
import com.google.api.services.compute.model.Operation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link ComputeCow} */
public class ComputeCow {
  private final Logger logger = LoggerFactory.getLogger(ComputeCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Compute compute;

  public ComputeCow(ClientConfig clientConfig, Compute.Builder computeBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    this.compute = computeBuilder.build();
  }

  /** Create a {@link ComputeCow} with some default configurations for convenience. */
  public static ComputeCow create(ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new ComputeCow(
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
        super(CloudOperation.GOOGLE_CREATE_NETWORK, clientConfig, operationAnnotator, insert);
        this.network = network;
        this.projectId = projectId;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project", projectId);
        result.add("network", new Gson().toJsonTree(network).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Compute.Networks#insert(String, Network)}. */
    public Get get(String projectId, String networkName) throws IOException {
      return new Get(networks.get(projectId, networkName));
    }

    /** See {@link Compute.Networks.Insert}. */
    public class Get extends AbstractRequestCow<Network> {
      private final Compute.Networks.Get get;

      public Get(Compute.Networks.Get get) {
        super(CloudOperation.GOOGLE_GET_NETWORK, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project", get.getProject());
        result.addProperty("network_name", get.getNetwork());
        return result;
      }
    }
  }

  /**
   * See {@link Compute#GlobalOperations()}.
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
        super(CloudOperation.GOOGLE_COMPUTE_OPERATION_GET, clientConfig, operationAnnotator, get);
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
}
