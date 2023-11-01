package bio.terra.cloudres.aws.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
public class TagUtilsTest {

  private static List<GenericTag> genericTags =
      List.of(
          GenericTag.builder().key("WithValue").value("MyValue").build(),
          GenericTag.builder().key("NoValue").build());

  @Test
  public void equalsAndHashCode() {

    GenericTag refTag = genericTags.get(0);

    // Compare to self
    assertEquals(refTag, refTag);

    GenericTag copyTag = GenericTag.builder().key(refTag.getKey()).value(refTag.getValue()).build();

    // Equal objects
    assertEquals(refTag, copyTag);
    assertEquals(refTag.hashCode(), copyTag.hashCode());

    // Compare to null
    Assertions.assertNotEquals(refTag, null);
    Assertions.assertNotEquals((Object) refTag, (Object) this);
  }

  @Test
  public void tosServicesAndBack() {

    // S3
    var s3Tags = TagUtils.toS3Tags(genericTags);
    Collection<GenericTag> fromS3Tags = TagUtils.fromS3Tags(s3Tags);
    assertTrue(fromS3Tags.containsAll(genericTags));
    assertTrue(genericTags.containsAll(fromS3Tags));

    // EC2
    var ec2Tags = TagUtils.toEc2Tags(genericTags);
    Collection<GenericTag> fromEc2Tags = TagUtils.fromEc2Tags(ec2Tags);
    assertTrue(fromEc2Tags.containsAll(genericTags));
    assertTrue(genericTags.containsAll(fromEc2Tags));

    // SageMaker
    var sageMakerTags = TagUtils.toSageMakerTags(genericTags);
    Collection<GenericTag> fromSageMakerTags = TagUtils.fromSageMakerTags(sageMakerTags);
    assertTrue(fromSageMakerTags.containsAll(genericTags));
    assertTrue(genericTags.containsAll(fromSageMakerTags));

    // STS
    var stsTags = TagUtils.toStsTags(genericTags);
    Collection<GenericTag> fromStsTags = TagUtils.fromStsTags(stsTags);
    assertTrue(fromStsTags.containsAll(genericTags));
    assertTrue(genericTags.containsAll(fromStsTags));
  }
}
