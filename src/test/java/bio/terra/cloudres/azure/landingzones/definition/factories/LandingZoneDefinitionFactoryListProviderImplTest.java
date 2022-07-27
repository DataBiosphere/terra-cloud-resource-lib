package bio.terra.cloudres.azure.landingzones.definition.factories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryDefinitionInfo;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class LandingZoneDefinitionFactoryListProviderImplTest {

  @Test
  void listFactories_listContainsTestLandingZoneDefinitionFactory() {
    var provider = new LandingZoneDefinitionFactoryListProviderImpl();

    assertThat(
        provider.listFactories(),
        hasItem(
            new FactoryDefinitionInfo(
                TestLandingZoneFactory.LZ_NAME,
                TestLandingZoneFactory.LZ_DESC,
                TestLandingZoneFactory.class.getName(),
                List.of(DefinitionVersion.V1))));
  }

  @Test
  void listFactoriesClasses_listContainsTestLandingZoneDefinitionFactory() {
    var provider = new LandingZoneDefinitionFactoryListProviderImpl();

    assertThat(provider.listFactoriesClasses(), hasItem(TestLandingZoneFactory.class));
  }
}
