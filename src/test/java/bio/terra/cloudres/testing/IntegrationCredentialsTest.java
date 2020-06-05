package bio.terra.cloudres.testing;

import static org.junit.Assert.fail;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class IntegrationCredentialsTest {
  @Test
  public void googleCredentialsAreAccessible_admin() {
    try {
      IntegrationCredentials.getAdminGoogleCredentialsOrDie();
    } catch (Exception e) {
      fail("Should not have thrown any exception getting credentials, but threw " + e);
    }
  }

  @Test
  public void googleCredentialsAreAccessible_user() {
    try {
      IntegrationCredentials.getUserGoogleCredentialsOrDie();
    } catch (Exception e) {
      fail("Should not have thrown any exception getting credentials, but threw " + e);
    }
  }
}
