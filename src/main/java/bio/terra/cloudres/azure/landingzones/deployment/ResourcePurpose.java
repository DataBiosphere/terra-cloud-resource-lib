package bio.terra.cloudres.azure.landingzones.deployment;

import com.azure.core.util.ExpandableStringEnum;

/** Enum that indicates the purpose of the resource in the landing zone. */
public final class ResourcePurpose extends ExpandableStringEnum<ResourcePurpose> {

  public static final ResourcePurpose SHARED_RESOURCE = fromString("SHARED_RESOURCE");
  public static final ResourcePurpose WLZ_RESOURCE = fromString("WLZ_RESOURCE");

  /**
   * Creates or finds a {@link ResourcePurpose} from its string representation.
   *
   * @param name a name to look for
   * @return the corresponding {@link ResourcePurpose}
   */
  public static ResourcePurpose fromString(String name) {
    return fromString(name, ResourcePurpose.class);
  }
}
