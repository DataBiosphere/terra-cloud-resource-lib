package bio.terra.cloudres.google.notebooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class InstanceNameTest {

  @Test
  public void name() {
    String name = "projects/my-project/location/uswest1-b/instances/my-id";
    InstanceName instanceName = InstanceName.fromNameFormat(name);

    assertEquals("my-project", instanceName.projectId());
    assertEquals("uswest1-b", instanceName.location());
    assertEquals("my-id", instanceName.instanceId());
    assertEquals(name, instanceName.formatName());
  }

  @Test
  public void invalidName() {
    assertThrows(IllegalArgumentException.class, () -> InstanceName.fromNameFormat("foo"));
  }

  @Test
  public void parent() {
    String parent = "projects/my-project/location/uswest1-b";
    InstanceName instanceName = InstanceName.fromParentAndId(parent, "my-id");

    assertEquals("my-project", instanceName.projectId());
    assertEquals("uswest1-b", instanceName.location());
    assertEquals("my-id", instanceName.instanceId());
    assertEquals(parent, instanceName.formatParent());
  }

  @Test
  public void invalidParent() {
    assertThrows(
        IllegalArgumentException.class, () -> InstanceName.fromParentAndId("foo", "my-id"));
  }

  @Test
  public void addProperties() {
    InstanceName instanceName = InstanceName.builder().projectId("my-project").location("my-location").instanceId("my-id").build();
    JsonObject jsonObject = new JsonObject();
    instanceName.addProperties(jsonObject);
    assertEquals("{\"projectId\":\"my-project\",\"location\":\"my-location\",\"instanceId\":\"my-id\"}", jsonObject.toString());
  }
}
