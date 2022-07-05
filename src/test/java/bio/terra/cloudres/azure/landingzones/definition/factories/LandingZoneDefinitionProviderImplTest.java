package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;

@Tag("unit")
class LandingZoneDefinitionProviderImplTest {
    private LandingZoneDefinitionProviderImpl provider;

    @BeforeEach
    void setUp(){
        provider = new LandingZoneDefinitionProviderImpl();
    }


    @Test
    void factories_containsTestFactory() {
        var factories = provider.factories();
        FactoryInfo testFactory = new FactoryInfo(TestLandingZoneFactory.class, List.of(DefinitionVersion.V1));

        assertThat(factories, contains(hasToString(equalTo(testFactory.toString()))));
    }

    @Test
    void createDefinitionFactory_providerCreatesTestFactory(){
       var factory =  provider.createDefinitionFactory(TestLandingZoneFactory.class);

        assertThat(factory, instanceOf(TestLandingZoneFactory.class));
    }
}