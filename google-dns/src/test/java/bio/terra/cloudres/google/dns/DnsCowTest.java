package bio.terra.cloudres.google.dns;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.cloudres.google.billing.testing.CloudBillingUtils;
import bio.terra.cloudres.google.cloudresourcemanager.testing.ProjectUtils;
import bio.terra.cloudres.google.serviceusage.testing.ServiceUsageUtils;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import com.google.api.services.dns.model.ManagedZone;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class DnsCowTest {
  private static final List<String> SERVICE_IDS = ImmutableList.of("dns.googleapis.com");

  // TODO(PF-67): Find solution for piping configs and secrets.
  private static final String BILLING_ACCOUNT_NAME = "billingAccounts/01A82E-CA8A14-367457";

  private static DnsCow defaultDns() throws GeneralSecurityException, IOException {
    return DnsCow.create(
        IntegrationUtils.DEFAULT_CLIENT_CONFIG,
        IntegrationCredentials.getAdminGoogleCredentialsOrDie());
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
}
