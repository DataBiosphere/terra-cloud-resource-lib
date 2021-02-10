package bio.terra.cloudres.google.iam;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.Iam.Projects;
import com.google.api.services.iam.v1.Iam.Projects.Roles;
import com.google.api.services.iam.v1.Iam.Projects.Roles.List;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** A Cloud Object Wrapper(COW) for Google API Client Library: {@link Iam} */
public class IamCow {
  private final Logger logger = LoggerFactory.getLogger(IamCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Iam iam;

  public IamCow(ClientConfig clientConfig, Iam.Builder iamBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    iam = iamBuilder.build();
  }

  /** Create a {@link IamCow} with some default configurations for convenience. */
  public static IamCow create(ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new IamCow(
        clientConfig,
        new Iam.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(googleCredentials.createScoped(IamScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  /** See {@link Iam#projects}. */
  public Projects projects() {
    return new Projects(iam.projects());
  }

  /** See {@link Iam.Projects}. */
  public class Projects {
    private final Iam.Projects projects;

    private Projects(Iam.Projects projects) {
      this.projects = projects;
    }

    /** See {@link Iam.Projects.ServiceAccounts}. */
    public ServiceAccounts serviceAccounts() {
      return new ServiceAccounts(this.projects.serviceAccounts());
    }

    public class ServiceAccounts {
      private final Iam.Projects.ServiceAccounts serviceAccounts;

      public ServiceAccounts(Iam.Projects.ServiceAccounts serviceAccounts) {
        this.serviceAccounts = serviceAccounts;
      }

      /** See {@link Iam.Projects.ServiceAccounts#create(String, CreateServiceAccountRequest)}. */
      public Create create(String name, CreateServiceAccountRequest content) throws IOException {
        return new Create(serviceAccounts.create(name, content), name, content);
      }

      public class Create extends AbstractRequestCow<ServiceAccount> {
        private final String name;
        private final CreateServiceAccountRequest content;

        public Create(
            Iam.Projects.ServiceAccounts.Create create,
            String name,
            CreateServiceAccountRequest content) {
          super(
              IamOperation.GOOGLE_CREATE_SERVICE_ACCOUNT, clientConfig, operationAnnotator, create);
          this.name = name;
          this.content = content;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("name", name);
          result.add("content", new Gson().toJsonTree(content).getAsJsonObject());
          return result;
        }
      }

      /** See {@link Iam.Projects.ServiceAccounts#list(String)}. */
      public List list(String name) throws IOException {
        return new List(serviceAccounts.list(name), name);
      }

      public class List extends AbstractRequestCow<ListServiceAccountsResponse> {
        private final String name;

        public List(Iam.Projects.ServiceAccounts.List list, String name) {
          super(IamOperation.GOOGLE_LIST_SERVICE_ACCOUNT, clientConfig, operationAnnotator, list);
          this.name = name;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("project_name", name);
          return result;
        }
      }

      /** See {@link Iam.Projects.ServiceAccounts#delete(String)}. */
      public Delete delete(String name) throws IOException {
        return new Delete(serviceAccounts.delete(name), name);
      }

      public class Delete extends AbstractRequestCow<Empty> {
        private final String name;

        public Delete(Iam.Projects.ServiceAccounts.Delete delete, String name) {
          super(
              IamOperation.GOOGLE_DELETE_SERVICE_ACCOUNT, clientConfig, operationAnnotator, delete);
          this.name = name;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("name", name);
          return result;
        }
      }
    }

    /** See {@link Iam.Projects.Roles}. */
    public Roles roles() {
      return new Roles(this.projects.roles());
    }

    public class Roles {
      private final Iam.Projects.Roles roles;

      public Roles(Iam.Projects.Roles roles) {
        this.roles = roles;
      }

      /** See {@link Iam.Projects.Roles#create(String, CreateRoleRequest)}. */
      public Roles.Create create(String parent, CreateRoleRequest content) throws IOException {
        return new Roles.Create(roles.create(parent, content), parent, content);
      }

      public class Create extends AbstractRequestCow<Role> {
        private final String parent;
        private final CreateRoleRequest content;

        public Create(Iam.Projects.Roles.Create create, String parent, CreateRoleRequest content) {
          super(IamOperation.GOOGLE_CREATE_ROLE, clientConfig, operationAnnotator, create);
          this.parent = parent;
          this.content = content;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("parent", parent);
          result.add("content", new Gson().toJsonTree(content).getAsJsonObject());
          return result;
        }
      }

      /** See {@link Iam.Projects.Roles#delete(String)}. */
      public Roles.Delete delete(String name) throws IOException {
        return new Roles.Delete(roles.delete(name), name);
      }

      public class Delete extends AbstractRequestCow<Role> {
        private final String name;

        public Delete(Iam.Projects.Roles.Delete delete, String name) {
          super(IamOperation.GOOGLE_DELETE_ROLE, clientConfig, operationAnnotator, delete);
          this.name = name;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("name", name);
          return result;
        }
      }

      /** See {@link Iam.Projects.Roles#get(String)}. */
      public Roles.Get get(String name) throws IOException {
        return new Roles.Get(roles.get(name), name);
      }

      public class Get extends AbstractRequestCow<Role> {
        private final String name;

        public Get(Iam.Projects.Roles.Get get, String name) {
          super(IamOperation.GOOGLE_GET_ROLE, clientConfig, operationAnnotator, get);
          this.name = name;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("name", name);
          return result;
        }
      }

      /** See {@link Iam.Projects.Roles#list(String)}. */
      public Roles.List list(String parent) throws IOException {
        return new Roles.List(roles.list(parent), parent);
      }

      public class List extends AbstractRequestCow<ListRolesResponse> {
        private final String parent;
        private String view;

        /**
         * This includes an optional view for the returned Role objects, see {@link
         * Iam.Projects.Roles.List#setView(String)}. When `FULL` is specified, the
         * `includedPermissions` field is returned, which includes a list of all permissions in the
         * role. The default value is `BASIC`, which does not return the `includedPermissions`
         * field.
         */
        public List(Iam.Projects.Roles.List list, String parent) {
          super(IamOperation.GOOGLE_LIST_ROLE, clientConfig, operationAnnotator, list);
          this.view = "BASIC";
          this.parent = parent;
        }

        public List setView(String view) {
          ((Iam.Projects.Roles.List) request()).setView(view);
          this.view = view;
          return this;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("parent", parent);
          result.addProperty("view", view);
          return result;
        }
      }

      /** See {@link Iam.Projects.Roles#patch(String, Role)}. */
      public Roles.Patch patch(String name, Role content) throws IOException {
        return new Roles.Patch(roles.patch(name, content), name, content);
      }

      public class Patch extends AbstractRequestCow<Role> {
        private final String name;
        private final Role content;

        public Patch(Iam.Projects.Roles.Patch patch, String name, Role content) {
          super(IamOperation.GOOGLE_PATCH_ROLE, clientConfig, operationAnnotator, patch);
          this.name = name;
          this.content = content;
        }

        @Override
        protected JsonObject serialize() {
          JsonObject result = new JsonObject();
          result.addProperty("name", name);
          result.add("content", new Gson().toJsonTree(content).getAsJsonObject());
          return result;
        }
      }
    }
  }
}
