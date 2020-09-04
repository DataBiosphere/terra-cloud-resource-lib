package bio.terra.cloudres.google.billing;

import bio.terra.cloudres.google.cloudresourcemanager.CloudResourceManagerCow;
import bio.terra.cloudres.testing.IntegrationCredentials;
import bio.terra.cloudres.testing.IntegrationUtils;
import com.google.api.services.cloudresourcemanager.model.Project;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class CloudBillingClientCowTest {
    @Test
    public void getSetProjectBillingInfo() throws Exception {
        CloudResourceManagerCow managerCow = CloudResourceManagerCow.create(IntegrationUtils.DEFAULT_CLIENT_CONFIG, IntegrationCredentials.getAdminGoogleCredentialsOrDie());
        managerCow.projects().create(IntegrationUtils.DEFAULT_CLIENT_CONFIG)
    }


    private static String randomProjectId() {
        // Project ids must start with a letter and be no more than 30 characters long.
        return "p" + IntegrationUtils.randomName().substring(0, 29);
    }
}
