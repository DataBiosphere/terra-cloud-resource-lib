package bio.terra.cloudres;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class HelloTest {
  @Test
  public void hello() {
    assertEquals(new Hello().hello(), "hello");
  }
}
