package bio.terra.cloudres.google.dns;

import static bio.terra.cloudres.testing.IntegrationUtils.setHttpTimeout;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.api.services.common.Defaults;
import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.dns.Dns;
import com.google.api.services.dns.DnsScopes;
import com.google.api.services.dns.model.Change;
import com.google.api.services.dns.model.ManagedZone;
import com.google.api.services.dns.model.ResourceRecordSet;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class DnsCowTest {
  private static final List<String> SERVICE_IDS = ImmutableList.of("dns.googleapis.com");

  // TODO(PF-67): Find solution for piping configs and secrets.
  private static final String BILLING_ACCOUNT_NAME = "billingAccounts/01A82E-CA8A14-367457";

  private static DnsCow defaultDns() throws GeneralSecurityException, IOException {
    return new DnsCow(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        new Dns.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                Defaults.jsonFactory(),
                setHttpTimeout(
                    new HttpCredentialsAdapter(
                        IntegrationCredentials.getAdminGoogleCredentialsOrDie().createScoped(DnsScopes.all()))))
            .setApplicationName(IntegrationUtils.DEFAULT_CLIENT_NAME));
  }

  @Test
  public void createAndGetZone() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    String projectId = project.getProjectId();
    CloudBillingUtils.setProjectBillingInfo(projectId, BILLING_ACCOUNT_NAME);
    ServiceUsageUtils.enableServices(projectId, SERVICE_IDS);

    DnsCow dnsCow = defaultDns();
    String zoneName = "zone-name";
    String dnsName = "googleapis.com.";
    String visibility = "private";
    ManagedZone managedZone =
        new ManagedZone()
            .setName(zoneName)
            .setDnsName(dnsName)
            .setVisibility(visibility)
            .setDescription("description");
    dnsCow.managedZones().create(project.getProjectId(), managedZone).execute();
    ManagedZone createdManagedZone = dnsCow.managedZones().get(projectId, zoneName).execute();

    assertEquals(dnsName, createdManagedZone.getDnsName());
    assertEquals(zoneName, createdManagedZone.getName());
    assertEquals(visibility, createdManagedZone.getVisibility());
  }

  @Test
  public void createAndGetChange() throws Exception {
    Project project = ProjectUtils.executeCreateProject();
    String projectId = project.getProjectId();
    CloudBillingUtils.setProjectBillingInfo(projectId, BILLING_ACCOUNT_NAME);
    ServiceUsageUtils.enableServices(projectId, SERVICE_IDS);
    DnsCow dnsCow = defaultDns();
    ManagedZone managedZone =
        new ManagedZone()
            .setName("zone-name")
            .setDnsName("googleapis.com.")
            .setVisibility("private")
            .setDescription("description");
    dnsCow.managedZones().create(project.getProjectId(), managedZone).execute();

    ResourceRecordSet resourceRecordSet =
        new ResourceRecordSet()
            .setType("A")
            .setName("restricted.googleapis.com.")
            .setRrdatas(ImmutableList.of("199.36.153.4"))
            .setTtl(300);

    Change createdChange =
        dnsCow
            .changes()
            .create(
                project.getProjectId(),
                managedZone.getName(),
                new Change().setAdditions(ImmutableList.of(resourceRecordSet)))
            .execute();

    List<ResourceRecordSet> actualRecordSet =
        dnsCow
            .changes()
            .get(projectId, managedZone.getName(), createdChange.getId())
            .execute()
            .getAdditions();
    assertEquals(1, actualRecordSet.size());
    assertEquals("A", actualRecordSet.get(0).getType());
    assertEquals("restricted.googleapis.com.", actualRecordSet.get(0).getName());
    assertEquals("199.36.153.4", actualRecordSet.get(0).getRrdatas().get(0));
    assertEquals(300, actualRecordSet.get(0).getTtl());

    assertThat(
        dnsCow.resourceRecordSets().list(projectId, managedZone.getName()).execute().getRrsets(),
        Matchers.hasItem(actualRecordSet.get(0)));
  }

  @Test
  public void zoneCreateSerialize() throws Exception {
    ManagedZone managedZone = new ManagedZone().setName("zone-name");
    DnsCow.ManagedZones.Create create =
        defaultDns().managedZones().create("project-id", managedZone);

    assertEquals(
        "{\"project_id\":\"project-id\",\"managed_zone\":{\"name\":\"zone-name\"}}",
        create.serialize().toString());
  }

  @Test
  public void zoneGetSerialize() throws Exception {
    DnsCow.ManagedZones.Get get = defaultDns().managedZones().get("project-id", "zone-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"managed_zone\":\"zone-name\"}",
        get.serialize().toString());
  }

  @Test
  public void changeCreateSerialize() throws Exception {
    Change change = new Change().setId("change_id");
    DnsCow.Changes.Create create = defaultDns().changes().create("project-id", "zone_name", change);

    assertEquals(
        "{\"project_id\":\"project-id\",\"managed_zone_name\":\"zone_name\",\"change\":{\"id\":\"change_id\"}}",
        create.serialize().toString());
  }

  @Test
  public void changeGetSerialize() throws Exception {
    DnsCow.Changes.Get get = defaultDns().changes().get("project-id", "zone_name", "change-name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"managed_zone_name\":\"zone_name\",\"change_id\":\"change-name\"}",
        get.serialize().toString());
  }

  @Test
  public void recordSetListSerialize() throws Exception {
    DnsCow.ResourceRecordSets.List list =
        defaultDns().resourceRecordSets().list("project-id", "zone_name");

    assertEquals(
        "{\"project_id\":\"project-id\",\"managed_zone_name\":\"zone_name\"}",
        list.serialize().toString());
  }
}
