package bio.terra.cloudres.google.dns;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.common.CloudOperation;
import bio.terra.cloudres.common.OperationAnnotator;
import bio.terra.cloudres.google.api.services.common.AbstractRequestCow;
import bio.terra.cloudres.google.api.services.common.Defaults;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.DnsScopes;
import com.google.api.services.dns.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Cloud Object Wrapper(COW) for Google API Client Library: {@link Dns}
 *
 * <p><a href="https://github.com/googleapis/java-dns">Cloud API Client Library</a> doesn't support
 * creating private DNS yet, so use Google API Service instead.
 */
public class DnsCow {
  private final Logger logger = LoggerFactory.getLogger(DnsCow.class);

  private final ClientConfig clientConfig;
  private final OperationAnnotator operationAnnotator;
  private final Dns dns;

  public DnsCow(ClientConfig clientConfig, Dns.Builder dnsBuilder) {
    this.clientConfig = clientConfig;
    operationAnnotator = new OperationAnnotator(clientConfig, logger);
    dns = dnsBuilder.build();
  }

  /** Create a {@link DnsCow} with some default configurations for convenience. */
  public static DnsCow create(ClientConfig clientConfig, GoogleCredentials googleCredentials)
      throws GeneralSecurityException, IOException {
    return new DnsCow(
        clientConfig,
        new Dns.Builder(
                Defaults.httpTransport(),
                Defaults.jsonFactory(),
                new HttpCredentialsAdapter(googleCredentials.createScoped(DnsScopes.all())))
            .setApplicationName(clientConfig.getClientName()));
  }

  public ManagedZones managedZones() {
    return new ManagedZones(dns.managedZones());
  }

  /** See {@link Dns.ManagedZones}. */
  public class ManagedZones {
    private final Dns.ManagedZones managedZones;

    private ManagedZones(Dns.ManagedZones managedZones) {
      this.managedZones = managedZones;
    }

    /** See {@link Dns.ManagedZones#create(String, ManagedZone)}. */
    public Create create(String projectId, ManagedZone managedZone) throws IOException {
      return new Create(managedZones.create(projectId, managedZone), projectId, managedZone);
    }

    /** See {@link Dns.ManagedZones.Create}. */
    public class Create extends AbstractRequestCow<ManagedZone> {
      private final String projectId;
      private final ManagedZone managedZone;

      public Create(Dns.ManagedZones.Create create, String projectId, ManagedZone managedZone) {
        super(CloudOperation.GOOGLE_DNS_CREATE_ZONE, clientConfig, operationAnnotator, create);
        this.projectId = projectId;
        this.managedZone = managedZone;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", projectId);
        result.add("managed_zone", new Gson().toJsonTree(managedZone).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Dns.ManagedZones#get(String, String)}. */
    public Get get(String projectId, String managedZoneId) throws IOException {
      return new Get(managedZones.get(projectId, managedZoneId));
    }

    /** See {@link Dns.ManagedZones.Get} */
    public class Get extends AbstractRequestCow<ManagedZone> {
      private final Dns.ManagedZones.Get get;

      private Get(Dns.ManagedZones.Get get) {
        super(CloudOperation.GOOGLE_DNS_GET_ZONE, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("managed_zone", get.getManagedZone());
        return result;
      }
    }
  }

  public Changes changes() {
    return new Changes(dns.changes());
  }

  /** See {@link Dns.Changes}. */
  public class Changes {
    private final Dns.Changes changes;

    private Changes(Dns.Changes changes) {
      this.changes = changes;
    }

    /** See {@link Dns.Changes#create(String, String, Change)}. */
    public Create create(String projectId, String managedZoneName, Change change)
        throws IOException {
      return new Create(
          changes.create(projectId, managedZoneName, change), projectId, managedZoneName, change);
    }

    /** See {@link Dns.Changes.Create}. */
    public class Create extends AbstractRequestCow<Change> {
      private final String projectId;
      private final String managedZoneName;
      private final Change change;

      public Create(
          Dns.Changes.Create create, String projectId, String managedZoneName, Change change) {
        super(CloudOperation.GOOGLE_DNS_CREATE_CHANGEE, clientConfig, operationAnnotator, create);
        this.projectId = projectId;
        this.managedZoneName = managedZoneName;
        this.change = change;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", projectId);
        result.addProperty("managed_zone_name", managedZoneName);
        result.add("change", new Gson().toJsonTree(change).getAsJsonObject());
        return result;
      }
    }

    /** See {@link Dns.Changes#get(String, String, String)}. */
    public Get get(String projectId, String managedZoneName, String changeId) throws IOException {
      return new Get(changes.get(projectId, managedZoneName, changeId));
    }

    /** See {@link Dns.Changes.Get} */
    public class Get extends AbstractRequestCow<Change> {
      private final Dns.Changes.Get get;

      private Get(Dns.Changes.Get get) {
        super(CloudOperation.GOOGLE_DNS_GET_CHANGE, clientConfig, operationAnnotator, get);
        this.get = get;
      }

      @Override
      protected JsonObject serialize() {
        JsonObject result = new JsonObject();
        result.addProperty("project_id", get.getProject());
        result.addProperty("managed_zone_name", get.getManagedZone());
        result.addProperty("change_id", get.getChangeId());
        return result;
      }
    }
  }
}
