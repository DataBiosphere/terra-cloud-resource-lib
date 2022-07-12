package bio.terra.cloudres.azure.landingzones.definition.factories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("unit")
class LandingZoneDefinitionProviderImplTest {
  private LandingZoneDefinitionProviderImpl provider;

  private ArmManagers armManagers;

  @BeforeEach
  void setUp() {
    armManagers = new ArmManagers(null, null);
    provider = new LandingZoneDefinitionProviderImpl(armManagers);
  }

  @Test
  void factories_containsTestFactory() {
    var factories = provider.factories();
    FactoryInfo testFactory =
        new FactoryInfo(TestLandingZoneFactory.class, List.of(DefinitionVersion.V1));

    assertThat(factories, hasItem(testFactory));
  }

  @Test
  void createDefinitionFactory_providerCreatesTestFactory() {
    var factory = provider.createDefinitionFactory(TestLandingZoneFactory.class);

    assertThat(factory, instanceOf(TestLandingZoneFactory.class));
  }
}
