package bio.terra.cloudres.google.notebooks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class GcpNotebookInstanceNameTest {

  @Test
  public void name() {
    String name = "projects/my-project/locations/uswest1-b/instances/my-id";
    GcpNotebookInstanceName gcpNotebookInstanceName = GcpNotebookInstanceName.fromNameFormat(name);

    assertEquals("my-project", gcpNotebookInstanceName.projectId());
    assertEquals("uswest1-b", gcpNotebookInstanceName.location());
    assertEquals("my-id", gcpNotebookInstanceName.instanceId());
    assertEquals(name, gcpNotebookInstanceName.formatName());
  }

  @Test
  public void invalidName() {
    assertThrows(IllegalArgumentException.class, () -> GcpNotebookInstanceName.fromNameFormat("foo"));
  }

  @Test
  public void parent() {
    String parent = "projects/my-project/locations/uswest1-b";
    GcpNotebookInstanceName gcpNotebookInstanceName = GcpNotebookInstanceName.fromParentAndId(parent, "my-id");

    assertEquals("my-project", gcpNotebookInstanceName.projectId());
    assertEquals("uswest1-b", gcpNotebookInstanceName.location());
    assertEquals("my-id", gcpNotebookInstanceName.instanceId());
    assertEquals(parent, gcpNotebookInstanceName.formatParent());
  }

  @Test
  public void invalidParent() {
    assertThrows(
        IllegalArgumentException.class, () -> GcpNotebookInstanceName.fromParentAndId("foo", "my-id"));
  }

  @Test
  public void addProperties() {
    GcpNotebookInstanceName gcpNotebookInstanceName =
        GcpNotebookInstanceName.builder()
            .projectId("my-project")
            .location("my-location")
            .instanceId("my-id")
            .build();
    JsonObject jsonObject = new JsonObject();
    gcpNotebookInstanceName.addProperties(jsonObject);
    assertEquals(
        "{\"projectId\":\"my-project\",\"locations\":\"my-location\",\"instanceId\":\"my-id\"}",
        jsonObject.toString());
  }
}
