package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;
import bio.terra.cloudres.azure.landingzones.definition.DefinitionVersion;
import bio.terra.cloudres.azure.landingzones.definition.FactoryInfo;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.relay.RelayManager;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
/** Implementation of {@link LandingZoneDefinitionProvider} */
public class LandingZoneDefinitionProviderImpl implements LandingZoneDefinitionProvider {

  private final ClientLogger logger = new ClientLogger(LandingZoneDefinitionProviderImpl.class);
  private final ArmManagers armManagers;

  public LandingZoneDefinitionProviderImpl(ArmManagers armManagers) {
    this.armManagers = armManagers;
  }

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
    // a factory is a non-abstract class that implements LandingZoneDefinitionFactory.
    return !classInfo.load().isInterface()
        && LandingZoneDefinitionFactory.class.isAssignableFrom(classInfo.load())
        && !Modifier.isAbstract(classInfo.load().getModifiers());
  }

  private <T extends LandingZoneDefinitionFactory> FactoryInfo toFactoryInfo(
      Class<T> factoryClass) {
    List<DefinitionVersion> versions;
    try {
      versions = createNewFactoryInstance(factoryClass).availableVersions();
    } catch (Exception e) {
      throw logger.logExceptionAsError(new RuntimeException(e));
    }
    return new FactoryInfo(factoryClass, versions);
  }

  @Override
  public <T extends LandingZoneDefinitionFactory>
      LandingZoneDefinitionFactory createDefinitionFactory(Class<T> factory) {
    return createNewFactoryInstance(factory);
  }

  private <T extends LandingZoneDefinitionFactory> T createNewFactoryInstance(
      Class<T> factoryClass) {
    try {
      return factoryClass
          .getDeclaredConstructor(AzureResourceManager.class, RelayManager.class)
          .newInstance(armManagers.azureResourceManager(), armManagers.relayManager());
    } catch (Exception e) {
      throw logger.logExceptionAsError(new RuntimeException(e));
    }
  }
}
