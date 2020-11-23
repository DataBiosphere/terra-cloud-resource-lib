package bio.terra.cloudres.google.iam;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.api.services.common.OperationCow;
import com.google.api.services.serviceusage.v1.ServiceUsage;
import com.google.api.services.serviceusage.v1.ServiceUsageScopes;
import com.google.api.services.serviceusage.v1.model.BatchEnableServicesRequest;
import com.google.api.services.serviceusage.v1.model.ListServicesResponse;
import com.google.api.services.serviceusage.v1.model.Operation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link ServiceUsage} */
public class ServiceUsageCow {
  private final Logger logger = LoggerFactory.getLogger(ServiceUsageCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final ServiceUsage serviceUsage;

  public ServiceUsageCow(ClientConfig clientConfig, ServiceUsage.Builder serviceUsageBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    serviceUsage = serviceUsageBuilder.build();
  }

  /** Create a {@link ServiceUsageCow} with some default configurations for convenience. */
  public static ServiceUsageCow create(
      ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new ServiceUsageCow(
        clientConfig,
        new ServiceUsage.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(
                    googleCredentials.createScoped(ServiceUsageScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  /** See {@link ServiceUsage#services}. */
  public Services services() {
    return new Services(serviceUsage.services());
  }

  /** See {@link ServiceUsage.Services}. */
  public class Services {
    private final ServiceUsage.Services services;

    private Services(ServiceUsage.Services services) {
      this.services = services;
    }

    /** See {@link ServiceUsage.Services#batchEnable(String, BatchEnableServicesRequest)}. */
    public BatchEnable batchEnable(String parent, BatchEnableServicesRequest content)
        throws IOException {
      return new BatchEnable(services.batchEnable(parent, content), content);
    }

    /** See {@link ServiceUsage.Services.BatchEnable}. */
    public class BatchEnable extends AbstractRequestCow<Operation> {
      private final ServiceUsage.Services.BatchEnable batchEnable;
      private final BatchEnableServicesRequest content;

      public BatchEnable(
          ServiceUsage.Services.BatchEnable batchEnable, BatchEnableServicesRequest content) {
        super(
            ServiceUsageOperation.GOOGLE_BATCH_ENABLE_SERVICES,
            clientConfig,
            operationAnnotator,
            batchEnable);
        this.batchEnable = batchEnable;
        this.content = content;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("parent", batchEnable.getParent());
        result.add("content", new Gson().toJsonTree(content));
        return result;
      }
    }

    /** See {@link ServiceUsage.Services#list}. */
    public List list(String parent) throws IOException {
      return new List(services.list(parent));
    }

    /** See {@link ServiceUsage.Services.List}. */
    public class List extends AbstractRequestCow<ListServicesResponse> {
      private final ServiceUsage.Services.List list;

      private List(ServiceUsage.Services.List list) {
        super(ServiceUsageOperation.GOOGLE_LIST_SERVICES, clientConfig, operationAnnotator, list);
        this.list = list;
      }

      /** See {@link ServiceUsage.Services.List#setFilter}. */
      public List setFilter(String filter) {
        list.setFilter(filter);
        return this;
      }

      /** See {@link ServiceUsage.Services.List#setFields}. */
      public List setFields(String fields) {
        list.setFields(fields);
        return this;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("parent", list.getParent());
        result.addProperty("filter", list.getFilter());
        result.addProperty("fields", list.getFields());
        return result;
      }
    }
  }

  /** See {@link ServiceUsage#operations()}. */
  public Operations operations() {
    return new Operations(serviceUsage.operations());
  }

  /** See {@link ServiceUsage.Operations}. */
  public class Operations {
    private final ServiceUsage.Operations operations;

    private Operations(ServiceUsage.Operations operations) {
      this.operations = operations;
    }

    /** See {@link ServiceUsage.Operations#get(String)} */
    public Get get(String name) throws IOException {
      return new Get(operations.get(name));
    }

    public class Get extends AbstractRequestCow<Operation> {
      private final ServiceUsage.Operations.Get get;

      public Get(ServiceUsage.Operations.Get get) {
        super(
            ServiceUsageOperation.GOOGLE_SERVICE_USAGE_OPERATION_GET,
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
          operation, ServiceUsageOperationAdapter::new, op -> get(op.getName()));
    }
  }
}
