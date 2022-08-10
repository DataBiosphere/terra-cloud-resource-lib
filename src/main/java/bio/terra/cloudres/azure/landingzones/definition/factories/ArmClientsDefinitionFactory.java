package bio.terra.cloudres.azure.landingzones.definition.factories;

import bio.terra.cloudres.azure.landingzones.definition.ArmManagers;

/** Base class that provides Arm clients for Landing Zone Definition factories. */
public abstract class ArmClientsDefinitionFactory implements LandingZoneDefinitionFactory {
  protected final ArmManagers armManagers;

  protected ArmClientsDefinitionFactory(ArmManagers armManagers) {
    this.armManagers = armManagers;
  }

  protected ArmClientsDefinitionFactory() {
    this.armManagers = null;
  }
}
