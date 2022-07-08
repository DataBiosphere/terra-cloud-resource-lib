package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.core.util.ExpandableStringEnum;

public final class DefinitionVersion extends ExpandableStringEnum<DefinitionVersion> {
    public static final DefinitionVersion V1 = fromString("v1");
    public static final DefinitionVersion V2 = fromString("v2");
    public static final DefinitionVersion V3 = fromString("v3");
    public static final DefinitionVersion V4 = fromString("v4");
    public static final DefinitionVersion V5 = fromString("v5");

    /**
     * Creates or finds a {@link DefinitionVersion} from its string representation.
     *
     * @param version a version to look for
     * @return the corresponding {@link DefinitionVersion}
     */
    public static DefinitionVersion fromString(String version) {
        return fromString(version, DefinitionVersion.class);
    }
}
