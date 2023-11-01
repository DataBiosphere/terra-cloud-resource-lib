package bio.terra.cloudres.aws.ec2;

import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag("unit")
public class EC2UtilsTest {

  private static final Logger logger = LoggerFactory.getLogger(EC2UtilsTest.class);

  private class FakeResponse {
    private final List<Integer> list;

    public FakeResponse(List<Integer> list) {
      this.list = list;
    }

    public boolean hasFoo() {
      return !list.isEmpty();
    }

    public List<Integer> getFoos() {
      return list;
    }
  }

  @Test
  void hasFalse() {
    FakeResponse fakeResponse = new FakeResponse(List.of());
    assertThrows(
        NoSuchElementException.class,
        () ->
            EC2Utils.extractSingleValue(fakeResponse, FakeResponse::hasFoo, FakeResponse::getFoos));
  }

  @Test
  void listTooBig() {
    FakeResponse fakeResponse = new FakeResponse(List.of(1, 2, 3));
    assertThrows(
        AssertionError.class,
        () ->
            EC2Utils.extractSingleValue(fakeResponse, FakeResponse::hasFoo, FakeResponse::getFoos));
  }
}
