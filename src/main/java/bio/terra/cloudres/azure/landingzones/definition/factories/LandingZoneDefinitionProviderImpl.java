package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import com.azure.core.util.logging.ClientLogger;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LandingZoneDefinitionProviderImpl implements LandingZoneDefinitionProvider {

    public ClientLogger logger = new ClientLogger(LandingZoneDefinitionProviderImpl.class);

    @Override
    public Set<FactoryInfo> factories() {
        try {
            String packageName = this.getClass().getPackageName();
            return ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getTopLevelClasses(packageName)
                    .stream()
                    .filter(this::isLandingZoneFactory)
                    .map(c -> toFactoryInfo((Class<? extends LandingZoneDefinitionFactory>) c.load()))
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    private boolean isLandingZoneFactory(ClassInfo classInfo) {
        return !classInfo.load().isInterface()
                &&
                LandingZoneDefinitionFactory.class.isAssignableFrom(classInfo.load());
    }

    private <T extends LandingZoneDefinitionFactory> FactoryInfo toFactoryInfo(Class<T> factoryClass) {
        List<DefinitionVersion> versions;
        try {
            versions = (factoryClass.getDeclaredConstructor().newInstance())
                    .availableVersions();
        } catch (Exception e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
        return new FactoryInfo(factoryClass, versions);
    }


    @Override
    public <T extends LandingZoneDefinitionFactory> LandingZoneDefinitionFactory createDefinitionFactory(Class<T> factory) {
        return createNewFactoryInstance(factory);
    }

    private <T extends LandingZoneDefinitionFactory> T createNewFactoryInstance(Class<T> factoryClass) {
        try {
            return factoryClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }
}
