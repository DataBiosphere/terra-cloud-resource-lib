package bio.terra.cloudres.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("integration")
public class IntegrationCredentialsTest {
  @Test
  public void googleCredentialsAreAccessible() {
    // Check that this does not throw an exception.
    IntegrationCredentials.getGoogleCredentialsOrDie();
  }
}
