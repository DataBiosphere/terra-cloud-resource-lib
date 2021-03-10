package bio.terra.cloudres.google.notebooks;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.janitor.model.CloudResourceUid;
import com.google.api.services.notebooks.v1.AIPlatformNotebooks;
import com.google.api.services.notebooks.v1.AIPlatformNotebooksScopes;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
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

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Create}. */
    public class Create extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Create create;
      private final Instance instance;

      public Create(
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
        // TODO DO NOT SUBMIT: update and fix me with new Janitor client.
        //                return Optional.of(
        //                        new CloudResourceUid()
        //                                .googleAiNotebookUid(new
        // GoogleAiNotebookUid().location().instanceId());
        return Optional.empty();
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("parent", create.getParent());
        result.addProperty("instanceId", create.getInstanceId());
        result.add("instance", new Gson().toJsonTree(instance));
        return result;
      }
    }


    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances#create(String, Instance)}. */
    public Delete delete(String name) throws IOException {
      return new Delete(instances.delete(name));
    }

    /** See {@link AIPlatformNotebooks.Projects.Locations.Instances.Create}. */
    public class Delete extends AbstractRequestCow<Operation> {
      private final AIPlatformNotebooks.Projects.Locations.Instances.Delete delete;

      public Delete(AIPlatformNotebooks.Projects.Locations.Instances.Delete delete) {
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
        result.addProperty("name", delete.getName());
        return result;
      }
    }
  }
}
