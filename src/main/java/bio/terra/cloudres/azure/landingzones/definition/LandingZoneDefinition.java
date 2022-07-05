package bio.terra.cloudres.azure.landingzones.definition;

public abstract class LandingZoneDefinition  {

     abstract LandingZoneDefinable createDefinition(int version);
}
