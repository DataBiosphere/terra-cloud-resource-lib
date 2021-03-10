package bio.terra.cloudres.google.notebooks;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.resources.GoogleAiNotebookUid;
import bio.terra.janitor.model.CloudResourceUid;
import bio.terra.janitor.model.GoogleProjectUid;
import com.google.api.services.notebooks.v1.AIPlatformNotebooks;
import com.google.api.services.notebooks.v1.AIPlatformNotebooksScopes;
import com.google.api.services.notebooks.v1.model.Instance;
import com.google.api.services.notebooks.v1.model.Operation;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

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
            private final Instance instance;

            public Create(AIPlatformNotebooks.Projects.Locations.Instances.Create create, Instance instance) {
                super(
                        AIPlatformNotebooksOperation.GOOGLE_CREATE_NOTEBOOKS_INSTANCE,
                        clientConfig,
                        operationAnnotator,
                        create);
                this.instance = instance;
            }

            @Override
            protected Optional<CloudResourceUid> resourceUidCreation() {
                return Optional.of(
                        new CloudResourceUid()
                                .googleAiNotebookUid(new GoogleAiNotebookUid().location().instanceId());
            }

            @Override
            protected JsonObject serialize() {
               return new Gson().toJsonTree(project).getAsJsonObject();
            }
        }
    }
}
