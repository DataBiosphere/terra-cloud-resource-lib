package bio.terra.cloudres.google.iam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class ServiceAccountNameTest {
  @Test
  public void name() {
    String name = "projects/my-project/serviceAccounts/foo@bar.com";
    ServiceAccountName serviceAccountName = ServiceAccountName.fromNameFormat(name);

    assertEquals("my-project", serviceAccountName.projectId());
    assertEquals("foo@bar.com", serviceAccountName.email());
    assertEquals(name, serviceAccountName.formatName());
  }

  @Test
  public void invalidName() {
    assertThrows(IllegalArgumentException.class, () -> ServiceAccountName.fromNameFormat("foo"));
  }

  @Test
  public void emailAccountId() {
    assertEquals(
        "foo@my-project.iam.gserviceaccount.com",
        ServiceAccountName.emailFromAccountId("foo", "my-project"));
  }
}
